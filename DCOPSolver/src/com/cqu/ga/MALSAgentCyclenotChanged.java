package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class MALSAgentCyclenotChanged extends AgentCycle {

    public static final int TYPE_ALSCOST_MESSAGE = 101;
    public static final int TYPE_BEST_MESSAGE = 102;
    public static final int NONE = -1;
    public final static String KEY_BESTCOST="KEY_BESTCOST";
    public final static String KEY_BESTCOSTINCYCLE="KEY_BESTCOSTINCYCLE";

    public static final int POPULATION = 10;

    private int[] accCostList;
    private int[] bestCostList;
    private int[] bestValueList;
    private int[] valueIndexList;
    private LinkedList<int[]> valueIndexListGroup;
    private LinkedList<int[]> localCostListGroup;
    private HashMap<Integer,LinkedList<int[]>> childrenMsgView;

    protected int bestValue;
    protected int bestCost;
    protected int bestIndividualIndex;
    protected double[] bestCostInCycle;

    private int alsCycleCount;
    private boolean [] isChanged;
    public MALSAgentCyclenotChanged(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        accCostList = new int[POPULATION];
        bestCostList = new int[POPULATION];
        bestValueList = new int[POPULATION];
        valueIndexList = new int[POPULATION];
        bestCostInCycle = new double[10];
        valueIndexListGroup = new LinkedList<>();
        localCostListGroup = new LinkedList<>();
        childrenMsgView = new HashMap<>();

        bestValue = -1;
        bestIndividualIndex = -1;
        bestCost = Integer.MAX_VALUE;
        alsCycleCount = 0;
        isChanged = new boolean[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            bestCostList[i] = Integer.MAX_VALUE;
            bestValueList[i] = -1;
        }
    }

    protected void alsWork(int[] valueList, int[]localCostList){
        valueIndexListGroup.add(valueList);
        if (!isLeafAgent()){
            localCostListGroup.add(localCostList);
        }else {
            accCostList = localCostList;
            sendAccCostMessage(accCostList);
        }
    }

    protected void sendAccCostMessage(int[] costList) {
        Message msg = new Message(this.id,this.parent,TYPE_ALSCOST_MESSAGE,costList);
        sendMessage(msg);
    }

    protected void disposeBestMessage(Message msg) {
//        int [] tempList = valueIndexListGroup.removeFirst();
//        boolean [] tempchangedList = (boolean[])msg.getValue();
//        for (int i = 0; i < POPULATION; i++) {
//            if (isChanged[i]){
//                valueIndexList[i] = bestValueList[i];
//            }
//        }
//        sendBestMessage(tempchangedList);
        int index = (int)msg.getValue();
        if (index == NONE){
            valueIndexListGroup.removeFirst();
            bestIndividualIndex = index;
        }else {
            bestValue = valueIndexListGroup.removeFirst()[index];
        }
        if (!isLeafAgent()){
            sendBestMessage(bestIndividualIndex);
        }
    }

    protected void disposeAccCostMessage(Message msg) {
        if (childrenMsgView.containsKey(msg.getIdSender())){
            childrenMsgView.get(msg.getIdSender()).add((int[])msg.getValue());
        }else {
            LinkedList<int[]> tempList = new LinkedList<>();
            tempList.add((int[])msg.getValue());
            childrenMsgView.put(msg.getIdSender(),tempList);
        }

        boolean isEnough = true;
        for (int childrenId : children) {
            if (!childrenMsgView.containsKey(childrenId)){
                isEnough = false;
                break;
            }
        }

        if (isEnough){
            accCostList = localCostListGroup.removeFirst();
            int [] newTemp = new int[POPULATION];
            for (int childrenId : children) {
                LinkedList<int[]> tempList = childrenMsgView.remove(childrenId);
                newTemp = tempList.removeFirst();
                for (int i = 0; i < POPULATION; i++) {
                    accCostList[i] += newTemp[i];
                }
                if (!tempList.isEmpty()){
                    childrenMsgView.put(childrenId,tempList);
                }
            }
            if (isRootAgent()){
                int minIndividualIndex = 0;
                int tempBestCost = Integer.MAX_VALUE;
                for (int i = 0; i < POPULATION; i++) {
                    accCostList[i] /= 2;
                    if (accCostList[i] < tempBestCost){
                        tempBestCost = accCostList[i];
                        minIndividualIndex = i;
                    }
                }
                if (accCostList[minIndividualIndex] < bestCost){
                    bestValue = valueIndexListGroup.removeFirst()[minIndividualIndex];
                    bestCost = accCostList[minIndividualIndex];
                    bestIndividualIndex = minIndividualIndex;
                }else {
                    bestIndividualIndex = NONE;
                    bestCost = accCostList[minIndividualIndex];
                    valueIndexListGroup.removeFirst();
                }
                if (bestCostInCycle.length > alsCycleCount){
                    bestCostInCycle[alsCycleCount] = bestCost;
                }else {
                    double temp[] = new double[2*bestCostInCycle.length];
                    for (int i = 0; i < bestCostInCycle.length; i++) {
                        temp[i] = bestCostInCycle[i];
                    }
                    bestCostInCycle = temp;
                    bestCostInCycle[alsCycleCount] = bestCost;
                }
                alsCycleCount++;
                sendBestMessage(bestIndividualIndex);
            }else {
                sendAccCostMessage(accCostList);
            }
        }
    }

    protected void malsStopRunning(){
        if (valueIndexListGroup.isEmpty()){
            if (isRootAgent()){//恢复数组长度
                double [] temp = new double[alsCycleCount];
                for (int i = 0; i < alsCycleCount; i++) {
                    temp[i] = bestCostInCycle[i];
                }
                bestCostInCycle = temp;
            }
            valueIndexList = bestValueList;
            stopRunning();
        }
    }

    private void sendBestMessage(int individualIdx) {
        for (int childrenId : children) {
            Message msg = new Message(this.id,childrenId,TYPE_BEST_MESSAGE,individualIdx);
            sendMessage(msg );
        }
    }

}
