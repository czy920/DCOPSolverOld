package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MGDBAAgent extends MALSAgentCycle {

    public static final int POPULATION = 10;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int TYPE_GAIN_MESSAGE = 202;
    public final static int CYCLE_COUNT_END = 1000;
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

    private int [] effSumCostList;
    private boolean[] isNeighborsImprovedList;
    private MinCostMatrix[] minCostList;
    private Modifier [] modifierList;

    public MGDBAAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new int[POPULATION];
        localCostList = new int[POPULATION];
        gainList = new int[POPULATION];
        suggestValueList = new int[POPULATION];
        localViewGroup = new HashMap<>();
        gainViewGroup = new HashMap<>();
        effSumCostList = new int[POPULATION];
        isNeighborsImprovedList = new boolean[POPULATION];
        modifierList = new Modifier[POPULATION];
        minCostList = new MinCostMatrix[POPULATION];
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
        effInit();
        minCostList = calMinCost();
        sendValueMessage(valueIndexList);
    }

    private MinCostMatrix[] calMinCost() {
        for (int i = 0; i < POPULATION; i++) {
            HashMap<Integer,Integer> minCost = new HashMap<>();
            for (int neighborId : neighbours) {
                int cost = Integer.MAX_VALUE;
                for (int j = 0; j < neighbourDomains.get(neighborId).length; j++) {
                    for (int k = 0; k < domain.length; k++) {
                        int t = constraintCosts.get(neighborId)[k][j];
                        if (t < cost){
                            cost = t;
                        }
                    }
                }
                minCost.put(neighborId,cost);
            }
            MinCostMatrix minCostMatrix = new MinCostMatrix(minCost);
            minCostList[i] = minCostMatrix;
        }
        return minCostList;
    }

    private void effInit() {
        for (int i = 0; i < POPULATION; i++) {
            HashMap<Integer,int[][]> modifier = new HashMap<>();
            for (int neighborId : neighbours) {
                modifier.put(neighborId,new int[domain.length][neighbourDomains.get(neighborId).length]);
            }
            Modifier modi = new Modifier(modifier);
            modifierList[i] = modi;
            HashMap<Integer,Integer> minMap = new HashMap<>();
            MinCostMatrix minCostList = new MinCostMatrix(minMap);
        }
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
        for (int i = 0; i < POPULATION; i++) {
            if (valueIndexList[i] != suggestValueList[i]){
                isNeighborsImprovedList[i] = true;
            }else {
                isNeighborsImprovedList[i] = false;
            }
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        cycleCount ++;
        if (cycleCount < CYCLE_COUNT_END){
            localCostList = calLocalCost(valueIndexList);
            GDBAWork();
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
        }
    }

    private void GDBAWork() {
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
            if (gainList[i] > 0){
                int maxGain = 0;
                for (int neighborId : neighbours) {
                    if (gainViewGroup.get(neighborId)[i] > maxGain){
                        maxGain = gainViewGroup.get(neighborId)[i];
                    }
                }
                if (gainList[i] > maxGain){
                    valueIndexList[i] = suggestValueList[i];
                }
            }else {
                if (!isNeighborsImprovedList[i]){
                    for (int neighborId : neighbours) {
                        int temp = effCost(i,valueIndexList[i],neighborId);
                        if (temp > (int)(minCostList[i].minCost.get(neighborId))){
                            inCreaseMod(i,valueIndexList[i],neighborId);
                        }
                    }
                }
            }
        }
        sendValueMessage(valueIndexList);

    }

    private void inCreaseMod(int index, int value, int neighborIndex) {
        for (int i = 0; i < POPULATION; i++) {
            int[][] temp = modifierList[i].modifierMap.get(neighborIndex);
            for (int j = 0; j < domain.length; j++) {
                for (int k = 0; k < neighbourDomains.get(neighborIndex).length; k++) {
                    temp[j][k] ++;
                }
            }
            modifierList[i].modifierMap.put(neighborIndex,temp);
        }
    }

    private int effCost(int index, int value, int neighborIndex) {
        int effCost = 0;
        int [][] temp = modifierList[index].modifierMap.get(neighborIndex);
        effCost = constraintCosts.get(neighborIndex)[value][localViewGroup.get(neighborIndex)[index]]*
                (temp[value][localViewGroup.get(neighborIndex)[index]] + 1);
        return effCost;
    }

    private void cycleForValue() {
        for (int i = 0; i < POPULATION; i++) {
            effSumCostList[i] = calEffCost(i,valueIndexList[i]);
            isNeighborsImprovedList[i] = false;
        }
        int[] minEffCost = new int[POPULATION];
        int[] minEffCostIndex = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            minEffCost[i] = Integer.MAX_VALUE;
            minEffCostIndex[i] = -1;
            for (int j = 0; j < domain.length; j++) {
                if (calEffCost(i,j) < minEffCost[i]){
                    minEffCost[i] = calEffCost(i,j);
                    minEffCostIndex[i] = j;
                }
            }
            gainList[i] = effSumCostList[i] - minEffCost[i];
            suggestValueList[i] = minEffCostIndex[i];
        }
        sendGainMessage(gainList);
    }

    private int calEffCost(int index,int value) {
        int effCost = 0;
        for (int neighborId : neighbours) {
            int [][] temp = modifierList[index].modifierMap.get(neighborId);
            effCost += constraintCosts.get(neighborId)[value][localViewGroup.get(neighborId)[index]]*
                    (temp[value][localViewGroup.get(neighborId)[index]] + 1);
        }
        return effCost;
    }

    private void sendGainMessage(int[] gain) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_GAIN_MESSAGE,gain);
            sendMessage(msg);
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
