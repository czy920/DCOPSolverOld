package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LSGAGDBA extends MALSAgentCycle {

    public static final int POPULATION = 10;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int TYPE_GAIN_MESSAGE = 202;
    public final static int TYPE_LEVEL_MESSAGE = 203;
    public final static int TYPE_CROSS_MESSAGE = 204;
    public final static int CYCLE_COUNT_END = 800;
    public final static int CROSS_CYCLE = 20;
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
    private boolean isCross;

    private int levelCount;
    private int[] rouletteP;
    private int[] sampleTimes;
    private int minLocalCost;
    private boolean isCenter;
    private HashMap<Integer,Integer> levelView;
    private int gaCycleCount;

    private int [] effSumCostList;
    private boolean[] isNeighborsImprovedList;
    private MinCostMatrix[] minCostList;
    private Modifier [] modifierList;

    public LSGAGDBA(int id, String name, int level, int[] domain) {
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
        sampleTimes = new int[POPULATION];
        levelView = new HashMap<>();
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
            case TYPE_LEVEL_MESSAGE:
                disposeLevelMessage(msg);
                break;
            case TYPE_CROSS_MESSAGE:
                disposeCrossMessage(msg);
                break;
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
        }
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
            int maxCycle = CYCLE_COUNT_END/10;
            double crossP = (double)(Math.log(maxCycle - gaCycleCount))/(double)Math.sqrt((maxCycle));
            if (isCenter && Math.random() < crossP){
                double []p = new double[POPULATION];
                updateSampleTimes(valueIndexList);
                rouletteP = calRouletteP(localCostList);
                crossover(rouletteP);
                isCross = true;
                sendCrossoverMessage(rouletteP);
            }
        }
    }

    private void disposeCrossMessage(Message msg) {
        crossover((int[])msg.getValue());
    }

    private void crossover(int[] rouList) {
        int[] tempList = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            tempList[i] = valueIndexList[i];
        }
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = tempList[rouList[i]];
        }
        isCross = true;
    }

    private void sendCrossoverMessage(int[] crossList) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_CROSS_MESSAGE,crossList);
            sendMessage(msg);
        }
    }

    private void updateSampleTimes(int[] valueList) {
        for (int i = 0; i < POPULATION; i++) {
            for (int j = 0; j < domain.length; j++) {
                if (valueList[i] == j){
                    sampleTimes[j] ++;
                }
            }
        }
    }

    private int[] calRouletteP(int[] localCostList) {
        double [] p = new double[POPULATION];
        int [] pointerList = new int[POPULATION];

//        p = calFitenss();
        p = newFitness();
        for (int i = 0; i < POPULATION; i++) {
            double pointer = 0;
            double temp = Math.random();
            for (int j = 0; j < POPULATION; j++) {
                pointer += p[j];
                if (temp < pointer){
                    pointerList[i] = j;
                    break;
                }
            }
        }
        return pointerList;
    }

    private double newSumCost(int[] localCostList, int[] sampleTimes) {
        double sumCost = 0;
        boolean tt = false;
        for (int i = 0; i < POPULATION; i++) {
            for (int j = 0; j < domain.length; j++) {
                if (valueIndexList[i] == j){
                    sumCost += Math.pow(localCostList[i],Math.log(sampleTimes[j]));
                }
            }
        }
        return sumCost;
    }

    private double[] newFitness() {
        double p[] = new double[POPULATION];
        double test = 0;
        for (int i = 0; i < POPULATION; i++) {
            int times = calTimes(valueIndexList[i]);
            double sumC = newSumCost(localCostList,sampleTimes);
            double cost = sumC - Math.pow(localCostList[i],Math.log(times));
            p[i] = (double) (cost)/((double)(POPULATION-1)*(sumC));
            test+=p[i];
        }
        return p;
    }

    private int calTimes(int i) {
        int times = 0;
        for (int j = 0; j < domain.length; j++) {
            if (i==j){
                times = sampleTimes[j];
            }
        }
        return times;
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
            if (!isCross){
                GDBAWork();
            }else {
                isCross = false;
            }
            if (cycleCount % (CROSS_CYCLE - 1) == 0){
                gaCycleCount ++;
                minLocalCost = calMinLocalCost();
                sendMinCostMessage(minLocalCost);
                mutation();
            }
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
        }
    }

    private void mutation() {
        double[] mutationP = calMutationP();
        for (int i = 0; i < POPULATION; i++) {
            double temp = Math.random();
            if (temp < mutationP[i]){
                valueIndexList[i] = (int)(Math.random()*domain.length);
            }
        }
    }

    private double[] calMutationP() {
        double p[] = new double[POPULATION];
//        p = calFitenss();
        p = newFitness();
        int maxGAcycle = CYCLE_COUNT_END/10;
        double t = -(double) (gaCycleCount - 20)*(double) (gaCycleCount - 20)/(double)(Math.sqrt(maxGAcycle));
        double r = Math.exp(t);
//        System.out.println("RRR: " + r + "gaCycle: " + gaCycleCount + "maxCycle: " + maxGAcycle + "t: " + t);
        for (int i = 0; i < POPULATION; i++) {
            p[i] = r*(1-p[i])*(1-p[i])*((double) ((CYCLE_COUNT_END/10)-gaCycleCount)/(double) (CYCLE_COUNT_END/10));
        }
        return p;
    }

    private void sendMinCostMessage(int minCost) {
        for (int neighorId : neighbours) {
            Message msg = new Message(this.id,neighorId,TYPE_LEVEL_MESSAGE,minCost);
            sendMessage(msg);
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
