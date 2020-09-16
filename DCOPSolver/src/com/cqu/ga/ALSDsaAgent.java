package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
//import sun.java2d.opengl.OGLContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ALSDsaAgent extends AlsAgentCycle {

    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int CYCLE_COUNT_END = 800;
    public final static String KEY_LOCALCOST="KEY_LOCALCOST";
    public final static double P = 0.8;

    private int cycleCount;
    private Map<Integer,Integer> localView;

    public ALSDsaAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        localView = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        cycleCount = 0;
        valueIndex = (int)(Math.random()*domain.length);
        sendValueMessage(valueIndex);
    }

    private void sendValueMessage(int value) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_VALUE_MESSAGE,value);
            sendMessage(msg);
        }
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        HashMap<String, Object> result=new HashMap<String, Object>();
        result.put(KEY_ID, this.id);
        result.put(KEY_NAME, this.name);
        result.put(KEY_VALUE, this.domain[valueIndex]);
        result.put(KEY_LOCALCOST,this.calcuLocalcost(valueIndex));
        result.put(KEY_BESTCOST, this.bestCost);
        if (isRootAgent()){
            result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);
        }
        this.msgMailer.setResult(result);
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double totalCost = 0;
        double[] bestCostInCycle = null;
        for (Map<String,Object> result : results){
            if (result.containsKey(KEY_BESTCOSTINCYCLE)){
                bestCostInCycle = (double[]) result.get(KEY_BESTCOSTINCYCLE);
            }
            totalCost+=((double)((Integer)result.get(KEY_LOCALCOST)))/2;
        }
        ResultCycleAls resultCycle = new ResultCycleAls();
        resultCycle.bestCostInCycle = bestCostInCycle;
        System.out.println("cost: " + bestCostInCycle[bestCostInCycle.length - 1]);
//        System.out.println("cost: " + totalCost);
        return resultCycle;
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
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
        }
    }

    private void disposeValueMessage(Message msg) {
        localView.put(msg.getIdSender(),(int)(msg.getValue()));
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        cycleCount++;
        if (cycleCount < CYCLE_COUNT_END){
            localCost = calcuLocalcost(valueIndex);
            dsaWork();
            sendValueMessage(valueIndex);
            alsWork();
        }else {
            alsStopRunning();
//            stopRunning();
        }
    }

    private void dsaWork() {
        int minCost = Integer.MAX_VALUE;
        int minCostIndex = -1;
        for (int i = 0; i < domain.length; i++) {
            int cost = calcuLocalcost(i);
            if (cost < minCost){
                minCost = cost;
                minCostIndex = i;
            }
        }
        if (minCost < localCost && Math.random() < P ){
            valueIndex = minCostIndex;
        }
    }

    private int calcuLocalcost(int value) {
        int cost = 0;
        for (int neighborId : neighbours) {
            cost += constraintCosts.get(neighborId)[value][localView.get(neighborId)];
        }
        return cost;
    }

    @Override
    protected void messageLost(Message msg) {
    }
}
