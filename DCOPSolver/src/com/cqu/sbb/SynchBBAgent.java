package com.cqu.sbb;

import com.cqu.core.Message;
import com.cqu.cyclequeue.AgentCycle;

import java.util.*;

/**
 * Created by dyc on 2016/7/10.
 */
public class SynchBBAgent extends AgentCycle {
    private int precursor = -1;
    private int successor = -1;
    private List<Integer> precursorList;
    private double randomTag;
    private boolean[] selectFlag;

    private static final int MSG_TYPE_ORDER = 1;
    private static final int MSG_TYPE_BROADCAST_ORDER = 2;
    private static final int MSG_TYPE_DOWNSTREAM = 3;
    private static final int MSG_TYPE_UPSTREAM = 4;
    private static final int MSG_TYPE_TERMINATE = 5;

    public SynchBBAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        selectFlag = new boolean[domain.length];
        precursorList = new LinkedList<>();
        Arrays.fill(selectFlag,false);
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        return null;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        if (msg.getType() == MSG_TYPE_ORDER){
            Map<Integer,Double> ordering = (Map<Integer,Double>)msg.getValue();
            randomTag = calcuVariance();
            ordering.put(id,randomTag);
            int dest = -1;
            for (int neighbour : neighbours){
                if (!ordering.containsKey(neighbour)){
                    System.out.println("id:" + id + " put neighbour:" + neighbour);
                    ordering.put(neighbour,-1.0);
                    if (dest == -1)
                        dest = neighbour;
                }
                else if (ordering.get(neighbour) == -1.0){
                    if (dest == -1)
                        dest = neighbour;
                }
            }
            if (dest == -1){
                for (int key : ordering.keySet()){
                    if (ordering.get(key) == -1.0){
                        dest = key;
                    }
                }
            }
            if (dest != -1){
                System.out.println("id:" + id + " select neighbour:" + dest);
                sendMessage(new Message(id,dest,MSG_TYPE_ORDER,ordering));
            }
            else {
                List<Map.Entry<Integer,Double>> entries = new LinkedList<>(ordering.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<Integer, Double>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });
                List<Integer> sortedIndex = new LinkedList<>();
                for (int i = 0; i < entries.size(); i++){
                    sortedIndex.add(entries.get(i).getKey());
                }
                for (int destId : allNodes){
                    sendMessage(new Message(id,destId,MSG_TYPE_BROADCAST_ORDER,sortedIndex));
                }
            }
        }
        else if (msg.getType() == MSG_TYPE_BROADCAST_ORDER){
            List<Integer> sortedIndex = (List<Integer>)msg.getValue();
            for (int i = 0; i < sortedIndex.size(); i++){
                for (int neighbourId : neighbours){
                    if (neighbourId == sortedIndex.get(i)){
                        precursorList.add(neighbourId);
                        break;
                    }
                }
                if (id == sortedIndex.get(i)){
                    precursor = i == 0 ? -1 : sortedIndex.get(i - 1);
                    successor = i == sortedIndex.size() - 1 ? -1 : sortedIndex.get(i + 1);
                    break;
                }
            }
            System.out.println("id:" + id + " random tag:" + randomTag +" precursor:" + precursor + " successor:" + successor);
            if (precursor == -1){
                Context context = new Context();
                context.aggregateCost.put(id,0);
                valueIndex = 0;
                selectFlag[valueIndex] = true;
                context.assignment.put(id,valueIndex);
                sendMessage(new Message(id,successor,MSG_TYPE_DOWNSTREAM,context));
            }
        }
        else if (msg.getType() == MSG_TYPE_DOWNSTREAM){
            Context context = (Context)msg.getValue();
            //System.out.println(context.assignment);
            boolean valueSelected = false;
            for (int i = 0; i < domain.length; i++){
                if (selectFlag[i])
                    continue;
                int localCost = 0;
                for (int highPriorityId : precursorList){
                    localCost += constraintCosts.get(highPriorityId)[i][context.assignment.get(highPriorityId)];
                }
                if (localCost + context.aggregateCost.get(precursor) < context.upperBound) {
                    valueIndex = i;
                    context.assignment.put(id,valueIndex);
                    context.aggregateCost.put(id,localCost + context.aggregateCost.get(precursor));
                    selectFlag[i] = true;
                    valueSelected = true;
                    break;
                }
                else {
                    selectFlag[i] = true;
                }
            }
            if (successor != -1) {
                if (valueSelected)
                    sendMessage(new Message(id, successor, MSG_TYPE_DOWNSTREAM, context));
                else {
                    Arrays.fill(selectFlag,false);
                    sendMessage(new Message(id, precursor, MSG_TYPE_UPSTREAM, context));
                }
            }
            else {
                Arrays.fill(selectFlag,false);
                for (int i = 0; i < domain.length; i++){
                    if (selectFlag[i])
                        continue;
                    int localCost =  0;
                    for (int highPriorityId : precursorList){
                        localCost += constraintCosts.get(highPriorityId)[i][context.assignment.get(highPriorityId)];
                    }
                    if (localCost + context.aggregateCost.get(precursor) < context.upperBound) {
                        valueIndex = i;
                        context.assignment.put(id,valueIndex);
                        context.aggregateCost.put(id,localCost + context.aggregateCost.get(precursor));
                        selectFlag[i] = true;
                        context.upperBound = context.aggregateCost.get(id);
                        context.back();
                        //System.out.println("best:" + context.bestAssignment);
                    }
                    else {
                        selectFlag[i] = true;
                    }
                }
                System.out.println("upperBound modify:" + context.upperBound);
                Arrays.fill(selectFlag,false);
                context.assignment.remove(id);
                sendMessage(new Message(id,precursor,MSG_TYPE_UPSTREAM,context));
            }
        }
        else if (msg.getType() == MSG_TYPE_UPSTREAM){
            Context context = (Context)msg.getValue();
            boolean canBacktrack = true;

            for (int i = 0; i < domain.length; i++){
                if (selectFlag[i])
                    continue;
                canBacktrack = false;
                if (precursor == -1){
                    valueIndex = i;
                    selectFlag[i] = true;
                    context.assignment.put(id,valueIndex);
                    break;
                }
                int localCost = 0;
                for (int highPriorityId : precursorList){
                    localCost += constraintCosts.get(highPriorityId)[i][context.assignment.get(highPriorityId)];
                }
                if (localCost + context.aggregateCost.get(precursor) < context.upperBound) {
                    valueIndex = i;
                    context.assignment.put(id,valueIndex);
                    context.aggregateCost.put(id,localCost + context.aggregateCost.get(precursor));
                    selectFlag[i] = true;
                    break;
                }
                else {
                    selectFlag[i] = true;
                }
            }
            if (!canBacktrack)
                sendMessage(new Message(id,successor,MSG_TYPE_DOWNSTREAM,context));
            else {
                if (precursor == -1){
                    sendMessage(new Message(id,successor,MSG_TYPE_TERMINATE,null));
                    System.out.println(context.bestAssignment);
                    stopRunning();
                }
                else {
                    Arrays.fill(selectFlag, false);
                    context.assignment.remove(id);
//                    context.aggregateCost.remove(id);
                    sendMessage(new Message(id,precursor,MSG_TYPE_UPSTREAM,context));
                }

            }
        }
        else if (msg.getType() == MSG_TYPE_TERMINATE){
            if (successor != -1)
                sendMessage(new Message(id,successor,MSG_TYPE_TERMINATE,null));
            stopRunning();
        }

    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    public void setNeibours(int[] neighbours, int parent, int[] children, int[] allParents, int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, int[][]> constraintCosts, Map<Integer, Integer> neighbourLevels, int[] highPriorities, int[] lowPriorities, int priority, int[] allNodes, int maxPriority, int minPriority) {
        super.setNeibours(neighbours, parent, children, allParents, allChildren, neighbourDomains, constraintCosts, neighbourLevels, highPriorities, lowPriorities, priority, allNodes, maxPriority, minPriority);
    }

    @Override
    protected void initRun() {
        super.initRun();
        if (id == 1){
            Map<Integer,Double> ordering = new HashMap<>();
            randomTag = calcuVariance();
            ordering.put(id,randomTag);
            for (int neighbour : neighbours){
                System.out.println("id:" + id + " put neighbour:" + neighbour);
                ordering.put(neighbour,-1.0);
            }
            System.out.println("id:" + id + " select neighbour:" + neighbours[0]);
            sendMessage(new Message(id,neighbours[0],MSG_TYPE_ORDER,ordering));
        }
    }

    private class Context{
        Map<Integer,Integer> aggregateCost;
        Map<Integer,Integer> assignment;
        Map<Integer,Integer> bestAssignment;
        int upperBound;
        int currentCost;
        public Context(){
            aggregateCost = new HashMap<>();
            assignment = new HashMap<>();
            upperBound = Integer.MAX_VALUE;
            bestAssignment = new HashMap<>();
        }
        public void back(){
            bestAssignment.clear();
            for (int key : assignment.keySet()){
                bestAssignment.put(key,assignment.get(key));
            }
        }
    }

    private double calcuVariance(){
        double totalVariance = 0;
        for (int i = 0; i < domain.length; i++){
            double mean = 0;
            double variance = 0;
            int count = 0;
            for (int neighbour : neighbours){
                for (int j = 0; j < neighbourDomains.get(neighbour).length; j++){
                    mean += constraintCosts.get(neighbour)[i][j];
                    count++;
                }
            }
            mean /= count;
            for (int neighbour : neighbours){
                for (int j = 0; j < neighbourDomains.get(neighbour).length; j++){
                    double delta = constraintCosts.get(neighbour)[i][j] - mean;
                    variance += delta * delta;
                }
            }
            variance /= count;
            totalVariance += variance;
        }
        return -totalVariance;
    }

}
