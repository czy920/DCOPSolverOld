package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AlsAgentCycle extends AgentCycle {

    public static final int TYPE_ALSCOST_MESSAGE = 101;
    public static final int TYPE_BEST_MESSAGE = 102;
    public final static String KEY_BESTCOST="KEY_BESTCOST";
    public final static String KEY_BESTCOSTINCYCLE="KEY_BESTCOSTINCYCLE";

    private LinkedList<Integer> valueIndexList;
    private LinkedList<Integer> localCostList;
    private Map<Integer,LinkedList<Integer>> childrenMsgView;

    private int accCost;
    protected int bestCost;
    private boolean isChanged;
    protected int bestValue;
    protected double[] bestCostInCycle;
    private int alsCycleCount;

    public AlsAgentCycle(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new LinkedList<>();
        localCostList = new LinkedList<>();
        childrenMsgView = new HashMap<>();
        bestCostInCycle = new double[9999];
        
        accCost = 0;
        alsCycleCount = 0;
        bestCost = Integer.MAX_VALUE;
        isChanged = false;
        bestValue = -1;
    }

    protected void alsWork(){
        valueIndexList.add(valueIndex);
        if (!isLeafAgent()){
            localCostList.add(localCost);
        }else {
            accCost = localCost;
            sendAccCostMessage(accCost);
        }
    }

    private void sendAccCostMessage(int cost) {
        Message msg = new Message(this.id,this.parent,TYPE_ALSCOST_MESSAGE,cost);
        sendMessage(msg);
    }

    protected void disposeAccCostMessage(Message msg) {
        if (childrenMsgView.containsKey(msg.getIdSender())){
            childrenMsgView.get(msg.getIdSender()).add((int)msg.getValue());
        }else {
            LinkedList<Integer> tempList = new LinkedList<>();
            tempList.add((int)msg.getValue());
            childrenMsgView.put(msg.getIdSender(),tempList);
        }

        boolean isEnough = true;
        for (int childrenId : children){
            if (!childrenMsgView.containsKey(childrenId)){
                isEnough = false;
                break;
            }
        }

        if (isEnough){
            accCost = localCostList.removeFirst();
            for (int  childrenId : children) {
                LinkedList<Integer> tempList = childrenMsgView.remove(childrenId);
                accCost += tempList.removeFirst();
                if (!tempList.isEmpty()){
                    childrenMsgView.put(childrenId,tempList);
                }
            }
            isChanged = false;
            if (!isRootAgent()){
                sendAccCostMessage(accCost);
            }else {
                alsCycleCount ++;
                accCost = accCost/2;
                if (accCost < bestCost){
                    bestValue = valueIndexList.removeFirst();
                    isChanged = true;
                    bestCost = accCost;
                }else {
                    valueIndexList.removeFirst();
                    isChanged = false;
                }

                sendBestCostMessage(isChanged);
                bestCostInCycle[alsCycleCount] = bestCost;
                if (bestCostInCycle.length == alsCycleCount){
                    double[] temp = new double [bestCostInCycle.length * 2];
                    for (int i = 0; i <= bestCostInCycle.length; i++) {
                        temp[i] = bestCostInCycle[i];
                    }
                    bestCostInCycle = temp;
                }

            }
        }
    }

    protected void alsStopRunning(){
        if (valueIndexList.isEmpty()){
            if (isRootAgent()){
                double [] temp = new double [alsCycleCount];
                for (int i = 0; i < alsCycleCount; i++) {
                    temp[i] = bestCostInCycle[i];
                }
                bestCostInCycle = temp;
            }
            valueIndex = bestValue;
            stopRunning();
        }
    }

    protected void sendBestCostMessage(boolean isChanged) {
        for (int childrenId : children) {
            Message msg = new Message(this.id,childrenId,TYPE_BEST_MESSAGE,isChanged);
            sendMessage(msg);
        }
    }

    protected void disposeBestMessage(Message msg){
        if ((boolean)msg.getValue()){
            bestValue = valueIndexList.removeFirst();
            isChanged = true;
        }else {
            valueIndexList.removeFirst();
            isChanged = false;
        }
       if (!isLeafAgent()){
           sendBestCostMessage(isChanged);
       }
    }

    @Override
    protected void messageLost(Message msg) {

    }
}
