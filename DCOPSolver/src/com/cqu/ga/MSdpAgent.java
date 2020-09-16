package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSdpAgent extends MALSAgentCycle {

    public static final int POPULATION = 10;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int CYCLE_COUNT_END = 1000;
    public final static String KEY_LOCALCOST="KEY_LOCALCOST";
    public final static double PA = 0.6;
    public final static double PB = 0.15;
    public final static double PC = 0.4;
    public final static double PD = 0.8;
    public final static int Round = 40;

    private int[] valueIndexList;
    private int[] localCostList;
    private int[] minLocalCostList;
    private HashMap<Integer,int[]> localViewGroup;
    private int cycleCount;
    private double changeP;
    private double changeQ;
    private int count =0 ;

    public MSdpAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new int[POPULATION];
        localCostList = new int[POPULATION];
        minLocalCostList = new int[POPULATION];
        localViewGroup = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = (int)(Math.random()*domain.length);
            localCostList[i] = Integer.MAX_VALUE;
            cycleCount = 0;
        }
        minLocalCostList = calMinLocalCost();
        sendValueMessage(valueIndexList);
    }

    private int[] calMinLocalCost() {
        int tempList[] = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            int minLocalCost = localCostList[i];
            for (int j = 0; j < domain.length; j++) {
                int tempLocalCost = 0;
                for (int neighborId : neighbours) {
                    int oneMinCost=constraintCosts.get(neighborId)[j][0];
                    for (int k = 1; k < neighbourDomains.get(neighborId).length; k++) {
                        int cost = constraintCosts.get(neighborId)[j][k];
                    }
                    tempLocalCost += oneMinCost;
                }
                if (tempLocalCost < localCostList[i]){
                    tempList[i] = tempLocalCost;
                }
            }
        }
        return tempList;
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
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
        }
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
            sdpWork();
            sendValueMessage(valueIndexList);
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
        }
    }

    private void sdpWork() {
        if (cycleCount%40!=0){
            for (int i = 0; i < POPULATION; i++) {
                int minCost = Integer.MAX_VALUE;
                int minCostIndex = -1;
                for (int j = 0; j < domain.length; j++) {
                    int cost = calCost(i,j);
                    if (cost < minCost){
                        minCost = cost;
                        minCostIndex = j;
                    }
                }

                if (minCost < localCostList[i]){
                    changeP = calchangeP(minLocalCostList[i],localCostList[i]);
                    if (Math.random() < changeP){
                        valueIndexList[i] = minCostIndex;
                    }
                }else {
                    changeQ = calchangeQ(minCost,localCostList[i]);
                    if (Math.random() < changeP){
                        valueIndexList[i] = minCostIndex;
                    }
                }
            }
        }else {
            for (int i = 0; i < POPULATION; i++) {
                valueIndexList[i] = (int)(Math.random()*domain.length);
            }
        }
    }

    private double calchangeQ(int newCost, int currentCost) {
        double temp = PA;
        currentCost += Integer.MIN_VALUE;
        double tail = (currentCost - newCost)/currentCost;
        temp += Math.min(PB,tail);
        return temp;
    }

    private double calchangeP(int newCost, int currentCost) {
        double temp = 0;
        currentCost += Integer.MIN_VALUE;
        double tail = (currentCost - newCost)/currentCost;
        if (tail <= 1){
            temp = Math.max(PC,PD - tail);
        }
        return temp;
    }

    private int calCost(int id, int value) {
        int cost = 0;
        for (int neighbourId : neighbours) {
            cost += constraintCosts.get(neighbourId)[value][localViewGroup.get(neighbourId)[id]];
        }
        return cost;
    }

    private int[] calLocalCost(int[] valueList) {
        int [] costList = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            costList[i] = 0;
            for (int neighborId :neighbours) {
                costList[i] += constraintCosts.get(neighborId)[valueList[i]][localViewGroup.get(neighborId)[i]];
            }
        }
        return  costList;
    }

    @Override
    protected void messageLost(Message msg) {

    }
}
