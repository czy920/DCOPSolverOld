package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMGMAgentCycle extends MALSAgentCycle {

    public static final int POPULATION = 13;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int TYPE_GAIN_MESSAGE = 202;
    public final static int CYCLE_COUNT_END = 2000;
    public final static String KEY_LOCALCOST="KEY_LOCALCOST";
    private int[] valueIndexList;
    private int[] localCostList;
    private int[] gainList;
    private int[] suggestValueList;
    private HashMap<Integer,int[]> localViewGroup;
    private HashMap<Integer,int[]> gainViewGroup;
    public static final int CYCLE_VALUE = 301;
    public static final int CYCLE_GAIN = 302;
    private int cycleCount;
    private int cycleTag;

    public MMGMAgentCycle(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new int[POPULATION];
        localCostList = new int[POPULATION];
        gainList = new int[POPULATION];
        suggestValueList = new int[POPULATION];
        localViewGroup = new HashMap<>();
        gainViewGroup = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        cycleTag = CYCLE_VALUE;
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = (int)(Math.random()*domain.length);
            localCostList[i] = Integer.MAX_VALUE;
            cycleCount = 0;
        }
        sendValueMessage(valueIndexList);
    }

    private void sendValueMessage(int[] valueList) {
        for (int neighorId : neighbours) {
            Message msg = new Message(this.id,neighorId,TYPE_VALUE_MESSAGE,valueList);
            sendMessage(msg);
        }
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        ResultCycleAls resultCycleAls = new ResultCycleAls();
        resultCycleAls.bestCostInCycle = bestCostInCycle;
        System.out.println("cost: " + bestCostInCycle[bestCostInCycle.length - 1]);
        return resultCycleAls;
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        HashMap<String, Object> result=new HashMap<String, Object>();
        result.put(KEY_ID, this.id);
        result.put(KEY_NAME, this.name);
        if (isRootAgent()){
            result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);
        }
        this.msgMailer.setResult(result);
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        switch (msg.getType()){
            case TYPE_VALUE_MESSAGE:
                disposeValueMessage(msg);
                break;
            case TYPE_GAIN_MESSAGE:
                disposeGainMessage(msg);
                break;
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
        }
    }

    private void disposeGainMessage(Message msg) {
        gainViewGroup.put(msg.getIdSender(),(int[])msg.getValue());
    }

    private void disposeValueMessage(Message msg) {
        localViewGroup.put(msg.getIdSender(),(int[])msg.getValue());
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        cycleCount ++;
        if (cycleCount < CYCLE_COUNT_END){
            localCostList = calLocalCost(valueIndexList);
            mgmWork();
        }else {
            malsStopRunning();
        }
    }

    private void mgmWork() {
        if (cycleTag == CYCLE_VALUE){
            cycleForValue();
            cycleTag = CYCLE_GAIN;
        }else {
            cycleForGain();
            cycleTag = CYCLE_VALUE;
        }
    }

    private void cycleForGain() {
        for (int i = 0; i < POPULATION; i++) {
            int maxGain = 0;
            for (int neighborId : neighbours) {
                if (gainViewGroup.get(neighborId)[i] > maxGain){
                    maxGain = gainViewGroup.get(neighborId)[i];
                }
            }
            if (gainList[i] > maxGain){
                valueIndexList[i] = suggestValueList[i];
            }
        }
        sendValueMessage(valueIndexList);
        alsWork(valueIndexList,localCostList);
    }

    private void cycleForValue() {
        gainList = calGainList();
        sendGainMessage(gainList);
    }

    private void sendGainMessage(int[] gain) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_GAIN_MESSAGE,gain);
            sendMessage(msg);
        }
    }

    private int[] calGainList() {
        int [] gain = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            int tempGain = 0;
            int tempSuggeseValue = valueIndexList[i];
            for (int j = 0; j < domain.length; j++) {
                int temp = localCostList[i] - calCost(i,j);
                if (temp > tempGain){
                    tempGain = temp;
                    tempSuggeseValue = j;
                }
            }
            gain[i] = tempGain;
            suggestValueList[i] = tempSuggeseValue;
        }
        return gain;
    }

    private int calCost(int id, int value) {
        int cost = 0;
        for (int neighbourId : neighbours) {
            cost += constraintCosts.get(neighbourId)[value][localViewGroup.get(neighbourId)[id]];
        }
        return cost;
    }

    private int[] calLocalCost(int[] valueIndexList) {
        int [] costList = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            costList[i] = 0;
            for (int neighborId :neighbours) {
                costList[i] += constraintCosts.get(neighborId)[valueIndexList[i]][localViewGroup.get(neighborId)[i]];
            }
        }
        return  costList;
    }

    @Override
    protected void messageLost(Message msg) {

    }
}
