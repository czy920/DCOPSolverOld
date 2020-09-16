package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LSGAchampionAgent extends MALSAgentCycle {

    public static final int POPULATION = 10;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int TYPE_LEVEL_MESSAGE = 202;
    public final static int TYPE_CROSS_MESSAGE = 203;
    public final static int CYCLE_COUNT_END = 800;
    public final static int CROSS_CYCLE = 10;
    public final static double MUTATION_P = 0.001;
    public final static double CROSS_P = 0.1;
    public final static String KEY_LOCALCOST = "KEY_LOCALCOST";
    public final static double P = 0.4;
    private int[] valueIndexList;
    private int[] localCostList;
    private boolean[] championP;
    private HashMap<Integer,int[]> localViewGroup;
    private int cycleCount;
    private int minLocalCost;
    private int levelCount;
    private boolean isCenter;
    private boolean isCross;
    private HashMap<Integer,Integer> levelView;

    public LSGAchampionAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new int[POPULATION];
        localCostList = new int[POPULATION];
        championP = new boolean[POPULATION];
        localViewGroup = new HashMap<>();
        levelView = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = (int)(Math.random()*domain.length);
            localCostList[i] = Integer.MAX_VALUE;
            cycleCount = 0;
        }
        levelCount = 0;
        isCenter = false;
        isCross = false;
        minLocalCost = Integer.MAX_VALUE;
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
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
            case TYPE_LEVEL_MESSAGE:
                disposeLevelMessage(msg);
                break;
            case TYPE_CROSS_MESSAGE:
                disposeCrossMessage(msg);
                break;
        }
    }

    private void disposeCrossMessage(Message msg) {
        crossover((boolean[])msg.getValue());
    }

    private void disposeLevelMessage(Message msg) {
        levelCount ++;
        levelView.put(msg.getIdSender(),(int)msg.getValue());
        if (levelCount == neighbours.length){
            levelCount = 0;
            isCenter = true;
            for (int neighborId : neighbours) {
                if (minLocalCost > levelView.get(neighborId)){
                    isCenter = false;
                    break;
                }
            }
            if (isCenter && Math.random() < CROSS_P){
                championP = calChampionP(localCostList);
                crossover(championP);
                isCross = true;
                sendCrossoverMessage(championP);
            }
        }
    }

    private boolean[] calChampionP(int[] localCostList) {
        boolean [] temp = new boolean[POPULATION];
        for (int i = 0; i < POPULATION-1; i++) {
            if (valueIndexList[i+1] < valueIndexList[i]){
                temp[i] = true;
            }else {
                temp[i] = false;
            }
        }
        if (valueIndexList[POPULATION-1]<valueIndexList[0]){
            temp[POPULATION - 1] = true;
        }else {
            temp[POPULATION - 1] = false;
        }
        return temp;
    }

    private void mutation() {
        for (int i = 0; i < POPULATION; i++) {
            double temp = Math.random();
            if (temp < MUTATION_P){
                valueIndexList[i] = (int)(Math.random()*domain.length);
            }
        }
    }

    private void crossover(boolean[] championList) {
        int[] tempList = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            tempList[i] = valueIndexList[i];
        }
        for (int i = 0; i < POPULATION-1; i++) {
            if (championList[i]){
                valueIndexList[i] = tempList[i+1];
            }
        }
        if (championList[POPULATION-1]){
            valueIndexList[POPULATION-1] = tempList[0];
        }
        mutation();
    }

    private void sendCrossoverMessage(boolean[] crossList) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_CROSS_MESSAGE,crossList);
            sendMessage(msg);
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
            if (!isCross){
                dsaWork();
            }else {
                isCross = false;
            }
            if (cycleCount % (CROSS_CYCLE - 1) == 0){
                minLocalCost = calMinLocalCost();
                sendMinCostMessage(minLocalCost);
            }
            sendValueMessage(valueIndexList);
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
        }
    }

    private void sendMinCostMessage(int minCost) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_LEVEL_MESSAGE,minCost);
            sendMessage(msg);
        }
    }

    private int calMinLocalCost() {
        //添加尋找最小localcost模塊
        int minCost = Integer.MAX_VALUE;
        for (int i = 0; i < POPULATION; i++) {
            if (localCostList[i] < minCost){
                minCost = localCostList[i];
            }
        }
        return (int)(minCost/neighbours.length);
    }

    private void dsaWork() {
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
            if (minCost < localCostList[i] && Math.random() < P){
                valueIndexList[i] = minCostIndex;
            }
        }
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
