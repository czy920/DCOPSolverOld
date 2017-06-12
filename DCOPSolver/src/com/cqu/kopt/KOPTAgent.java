package com.cqu.kopt;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;

import java.util.*;

/**
 * Created by dyc on 2017/6/5.
 */
public class KOPTAgent extends AgentCycle {

    private static final int K = 2;
    private static final int MAX_CYCLE = 100;

    private static final int MSG_TYPE_VALUE = 0;
    private static final int MSG_TYPE_INFORMATION = 1;
    private static final int MSG_TYPE_REWARD = 2;
    private static final int MSG_TYPE_NEXT_VALUE = 3;

    private static final int STATUS_VALUE = 0;
    private static final int STATUS_INFORMATION = 1;
    private static final int STATUS_REWARD = 2;
    private static final int STATUS_NEXT_VALUE = 3;

    private Map<Integer,Integer> valueView;
    private Map<Integer,Integer> localView;
    private Set<Integer> neighboursSet;
    private int status;
    private Map<Integer,int[]> domainView;
    private Map<Integer,Map<Integer,int[][]>> constraintView;
    private int informationCycle = 2;
    private int cycle;
    private Map<Integer,Integer> distanceView;
    private AbstractEnumerator enumerator;
    private Set<Assignment> assignmentView;
    private int rewardCycle = 2;
    private Assignment optimalAssignment;
    private int nextValue;
    private Map<Integer,Integer> nextValueView;
    private int nextValueCycle = 2;

    public KOPTAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueView = new HashMap<>();
        localView = new HashMap<>();
        neighboursSet = new HashSet<>();
        status = -1;
        domainView = new HashMap<>();
        constraintView = new HashMap<>();
        cycle = 1;
        distanceView = new HashMap<>();
        assignmentView = new HashSet<>();
        nextValueView = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        valueIndex = (int) (Math.random() * domain.length);
        valueView.put(id,valueIndex);
        domainView.put(this.id,this.domain);
        for (int neighbourId : neighbours){
            neighboursSet.add(neighbourId);
            distanceView.put(neighbourId,1);
            sendMessage(new Message(this.id,neighbourId,MSG_TYPE_VALUE,valueIndex));
        }
        constraintView.put(this.id,cloneMap(constraintCosts));
        distanceView.put(this.id,0);
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double sum = 0;
        for (Map<String, Object> result : results){
            sum += (int) result.get("cost");
        }
        ResultCycle resultCycle = new ResultCycle();
        resultCycle.totalCost = (int) (sum / 2);
        return resultCycle;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        switch (msg.getType()) {
            case MSG_TYPE_VALUE:
                status = STATUS_VALUE;
                valueView.put(msg.getIdSender(), (int) msg.getValue());
                if (neighboursSet.contains(msg.getIdSender())) {
                    localView.put(msg.getIdSender(), (int) msg.getValue());
                }
                break;
            case MSG_TYPE_INFORMATION:
                Map<String, Object> messageContent = (Map<String, Object>) msg.getValue();
                Map<Integer, Integer> receivedValueView = (Map<Integer, Integer>) messageContent.get("value");
                for (int id : receivedValueView.keySet()) {
                    valueView.put(id, receivedValueView.get(id));
                }
                if (cycle == 1) {
                    Map<Integer, Map<Integer, int[][]>> receivedConstraintView = (Map<Integer, Map<Integer, int[][]>>) messageContent.get("constraint");
                    for (int extId : receivedConstraintView.keySet()) {
                        for (int innerId : receivedConstraintView.get(extId).keySet()) {
                            Map<Integer, int[][]> innerView = constraintView.get(extId);
                            if (innerView == null) {
                                innerView = new HashMap<>();
                                constraintView.put(extId, innerView);
                            }
                            innerView.put(innerId, receivedConstraintView.get(extId).get(innerId));
                        }
                    }
                    Map<Integer, int[]> receivedDomainView = (Map) messageContent.get("domain");
                    for (int id : receivedDomainView.keySet()) {
                        domainView.put(id, receivedDomainView.get(id));
                    }
                    Map<Integer, Integer> receivedDistanceView = (Map) messageContent.get("distance");
                    for (int id : receivedDistanceView.keySet()) {
                        int dist = receivedDistanceView.get(id) + 1;
                        if (!distanceView.containsKey(id)) {
                            distanceView.put(id, dist);
                        } else {
                            if (distanceView.get(id) > dist) {
                                distanceView.put(id, dist);
                            }
                        }
                    }
                }
                break;
            case MSG_TYPE_REWARD:
                Set<Assignment> receivedAssignment = (Set<Assignment>) msg.getValue();
                status = STATUS_REWARD;
                for (Assignment assignment : receivedAssignment) {
                    assignmentView.add(assignment);
                }
                break;
            case MSG_TYPE_NEXT_VALUE:
                status = STATUS_NEXT_VALUE;
                Map<Integer,Integer> receivedNextValue = (Map) msg.getValue();
                for (int id : receivedNextValue.keySet()){
                    nextValueView.put(id,receivedNextValue.get(id));
                }
                break;
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        switch (status){
            case STATUS_VALUE: {
                Map<String, Object> messageContent = constructInformationMessageContent();
                status = STATUS_INFORMATION;
                for (int neighbourId : neighbours) {
                    sendMessage(new Message(this.id, neighbourId, MSG_TYPE_INFORMATION, messageContent));
                }
            }
                break;
            case STATUS_INFORMATION:
                if (informationCycle++ >= (K + 2) / 2){
                    informationCycle = 2;
                    if (cycle == 1) {
                        purifyConstraints();
                    }
                    List<Integer> activeAgents = selectActiveAgents();
                    if (activeAgents != null){
                        enumerator = new BruteForceEnumerator(constraintView,domainView,valueView,activeAgents,this.id);
                        Assignment assignment = enumerator.enumerate();
                        assignmentView.add(assignment);
                        Set<Assignment> clonedAssignment = cloneSet(assignmentView);
                        for (int neighbourId : neighbours){
                            sendMessage(new Message(this.id,neighbourId,MSG_TYPE_REWARD,clonedAssignment));
                        }
                    }
                }
                else {
                    Map<String,Object> messageContent = constructInformationMessageContent();
                    status = STATUS_INFORMATION;
                    for (int neighbourId : neighbours){
                        sendMessage(new Message(this.id,neighbourId,MSG_TYPE_INFORMATION,messageContent));
                    }
                }
                break;
            case STATUS_REWARD:
                if (rewardCycle++ >= (K + 2) / 2){
                    rewardCycle = 2;
                    for (Assignment assignment : assignmentView){
                        if (optimalAssignment == null){
                            optimalAssignment = assignment;
                        }
                        else {
                            if (optimalAssignment.getTotalCost() > assignment.getTotalCost()){
                                optimalAssignment = assignment;
                            }
                            else if (optimalAssignment.getTotalCost() == assignment.getTotalCost() && assignment.getCenter() < optimalAssignment.getCenter()){
                                optimalAssignment = assignment;
                            }
                        }
                    }
                    nextValue = !optimalAssignment.contains(id) ? valueIndex: optimalAssignment.get(this.id);
                    nextValueView.put(this.id,nextValue);
                    Map<Integer,Integer> clonedNextValueView = cloneMap(nextValueView);
                    for (int neighbourId : neighbours){
                        sendMessage(new Message(this.id,neighbourId,MSG_TYPE_NEXT_VALUE,clonedNextValueView));
                    }
                    nextValueCycle = 1;
                }
                else {
                    Set<Assignment> clonedAssignment = cloneSet(assignmentView);
                    for (int neighbourId : neighbours){
                        sendMessage(new Message(this.id,neighbourId,MSG_TYPE_REWARD,clonedAssignment));
                    }
                }
                break;
            case STATUS_NEXT_VALUE:
                if (nextValueCycle++ >= K - 1){
                    nextValueCycle = 2;
                    boolean canCommitted = true;
                    if (optimalAssignment.contains(id)) {
                        for (int id : optimalAssignment.idSet()) {
                            if (optimalAssignment.get(id) != nextValueView.get(id)) {
                                //system.out.println("id:" + id + " uncommitted,assignment:" + optimalAssignment +" since " + id + ":" +optimalAssignment.get(id) + "," + nextValueView.get(id));
                                canCommitted = false;
                                break;
                            }
                        }
                    }
                    else {
                        //system.out.println("id:" + id + " committed as static,assignment:" + optimalAssignment);
                    }
                    if (canCommitted){
                        valueIndex = nextValue;
                        //system.out.println("id:" + id + " commited,assignment:" + optimalAssignment);
                    }
                    cycle++;
                    if (cycle <= MAX_CYCLE) {
                        valueView.clear();
                        assignmentView.clear();
                        nextValueView.clear();
                        optimalAssignment = null;
                        valueView.put(id, valueIndex);
                        for (int neighbourId : neighbours) {
                            sendMessage(new Message(this.id, neighbourId, MSG_TYPE_VALUE, valueIndex));
                        }
                    }
                    else {
                        stopRunning();
                    }
                }
                else {
                    Map<Integer,Integer> clonedNextValueView = cloneMap(nextValueView);
                    for (int neighbourId : neighbours){
                        sendMessage(new Message(this.id,neighbourId,MSG_TYPE_NEXT_VALUE,clonedNextValueView));
                    }
                }
                break;
        }
    }

    private void purifyConstraints(){
        Map<Integer,Set<Integer>> duplicatedPairs = new HashMap<>();
        for (int extId : constraintView.keySet()){
            for (int innerId : constraintView.get(extId).keySet()){
                if (constraintView.containsKey(innerId) && constraintView.get(innerId).containsKey(extId)){
                    if (duplicatedPairs.get(innerId) == null || !duplicatedPairs.get(innerId).contains(extId)){
                        Set<Integer> extSet = duplicatedPairs.get(extId);
                        if (extSet == null){
                            extSet = new HashSet<>();
                            duplicatedPairs.put(extId,extSet);
                        }
                        extSet.add(innerId);
                    }
                }
            }
        }
        for (int key : duplicatedPairs.keySet()){
            for (int duplicatedId : duplicatedPairs.get(key)){
                constraintView.get(key).remove(duplicatedId);
            }
        }
    }

    private List<Integer> selectActiveAgents(){
        List<Integer> activeAgents = new LinkedList<>();
        TreeMap<Integer,List<Integer>> distances = new TreeMap<>();
        for (int id : valueView.keySet()){
            if (id == this.id || !domainView.containsKey(id)){
                continue;
            }
            int dist = distanceView.get(id);
            List<Integer> distList = distances.get(dist);
            if (distList == null){
                distList = new LinkedList<>();
                distances.put(dist,distList);
            }
            distList.add(id);
        }
        for (int dist : distances.keySet()){
            if (activeAgents.size() == K - 1){
                break;
            }
            if (activeAgents.size() + distances.get(dist).size() <= K - 1){
                activeAgents.addAll(distances.get(dist));
            }
            else {
                List<Integer> distList = distances.get(dist);
                while (activeAgents.size() < K - 1) {
                    int randomIndex = (int) (Math.random() * distList.size());
                    activeAgents.add(distList.remove(randomIndex));
                }
            }
        }
        if (activeAgents.size() < K - 1){
            return null;
        }
        activeAgents.add(id);
        return activeAgents;
    }

    private Map<String,Object> constructInformationMessageContent(){
        Map<String,Object> messageContent = new HashMap<>();
        messageContent.put("value",cloneMap(valueView));
        if (cycle == 1) {
            Map<Integer, Map<Integer, int[][]>> clonedConstraintView = new HashMap<>();
            for (int id : constraintView.keySet()) {
                clonedConstraintView.put(id, cloneMap(constraintView.get(id)));
            }
            messageContent.put("constraint", clonedConstraintView);
            messageContent.put("domain", cloneMap(domainView));
            messageContent.put("distance",cloneMap(distanceView));
        }
        return messageContent;
    }



    private Map cloneMap(Map map){
        Map clonedMap = new HashMap();
        for (Object key : map.keySet()){
            clonedMap.put(key,map.get(key));
        }
        return clonedMap;
    }

    private Set cloneSet(Set set){
        Set clonedSet = new HashSet();
        for (Object obj : set){
            clonedSet.add(obj);
        }
        return clonedSet;
    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        for (int id : localView.keySet()){
            sum += constraintCosts.get(id)[valueIndex][localView.get(id)];
        }
        return sum;
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        Map<String,Object> result = new HashMap<>();
        result.put("cost",getLocalCost());
        msgMailer.setResult(result);
    }

    @Override
    protected void messageLost(Message msg) {

    }
}
