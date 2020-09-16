package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DsaAgent extends AgentCycle {

    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int CYCLE_COUNT_END = 1000;
    public final static String KEY_LOCALCOST="KEY_LOCALCOST";
    public final static double P = 0.7;

    private int cycleCount;
    private Map<Integer,Integer> localView;

    public DsaAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        localView = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        cycleCount = 0;
        localCost = Integer.MAX_VALUE;
        valueIndex  = (int)(Math.random()*domain.length);
        sendValueMessage(valueIndex);
    }

    private void sendValueMessage(int value) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_VALUE_MESSAGE,value);
            sendMessage(msg);
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        cycleCount++;
        if (cycleCount < CYCLE_COUNT_END){
            localCost = calLocalCost(valueIndex);
            dsaWork();
            sendValueMessage(valueIndex);
        }else {
            stopRunning();
        }
    }

    private void dsaWork() {
        int minCost = Integer.MAX_VALUE;
        int minCostIndex = -1;
        int count = 0;
        for (int i = 0; i < domain.length; i++) {
            if (calLocalCost(i) < minCost){
                minCost = calLocalCost(i);
                minCostIndex = i;
            }
        }
        if (minCost < localCost && Math.random() < P){
            valueIndex = minCostIndex;
        }
    }

    private int calLocalCost(int value) {
        int cost = 0;
        for (int neighborId : neighbours) {
            cost += constraintCosts.get(neighborId)[value][localView.get(neighborId)];
        }
        return cost;
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double totalCost = 0;
        for (Map<String,Object> result : results) {
            totalCost += ((int)(result.get(KEY_LOCALCOST)))/2;
        }

        ResultCycle ret = new ResultCycle();
        ret.totalCost = (int)totalCost;
        System.out.println("cost: " + totalCost);
        return ret;
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
        }
    }

    private void disposeValueMessage(Message msg) {
        localView.put(msg.getIdSender(),(int)(msg.getValue()));
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        HashMap<String, Object> result = new HashMap<String,Object>();
        result.put(KEY_ID,this.id);
        result.put(KEY_NAME,this.name);
        result.put(KEY_VALUE,this.domain[valueIndex]);
        result.put(KEY_LOCALCOST,localCost);
        this.msgMailer.setResult(result);
    }

    @Override
    protected void messageLost(Message msg) {

    }
}
