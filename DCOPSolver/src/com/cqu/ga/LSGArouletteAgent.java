package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.*;

public class LSGArouletteAgent extends MALSAgentCycle {

    public static final int POPULATION = 13;
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
    private int[] rouletteP;
    private int[] sampleTimes;
    private HashMap<Integer,int[]> localViewGroup;
    private int cycleCount;
    private int minLocalCost;
    private int levelCount;
    private boolean isCenter;
    private boolean isCross;
    private HashMap<Integer,Integer> levelView;
    private int sumCost;
    private int gaCycleCount;

    public LSGArouletteAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        valueIndexList = new int[POPULATION];
        localCostList = new int[POPULATION];
        rouletteP = new int[POPULATION];
        sampleTimes = new int[domain.length];
        localViewGroup = new HashMap<>();
        levelView = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        gaCycleCount = 0;
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = (int)(Math.random()*domain.length);
            localCostList[i] = Integer.MAX_VALUE;
            cycleCount = 0;
        }
        for (int i = 0; i < domain.length; i++) {
            sampleTimes[i] = 1;
        }
        levelCount = 0;
        isCenter = false;
        isCross = false;
        sumCost = 0;
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
        crossover((int[])msg.getValue());
    }

    private void disposeLevelMessage(Message msg) {
        levelCount ++;
        levelView.put(msg.getIdSender(),(int)msg.getValue());
        if (levelCount == neighbours.length){
            levelCount = 0;
            isCenter = true;
            for (int neighborId : neighbours) {
                if (minLocalCost < levelView.get(neighborId)){
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

    private double[] calFitenss() {
        double p[] = new double[POPULATION];
        double test = 0;
        for (int i = 0; i < POPULATION; i++) {
            int times = calTimes(valueIndexList[i]);
            double cost = Math.pow(localCostList[i],-Math.sqrt(times));
            double sumC = calSumCost(localCostList,sampleTimes);
            p[i] = (double) (sumC - cost)/((double)(sumC*(POPULATION-1)));

        }
        return p;
    }

    private int[] calTimeList(int[] valueList) {
        int[] list = new int[domain.length];
        for (int i = 0; i < domain.length; i++) {
            list[i] = 0;
        }
        for (int i = 0; i < domain.length; i++) {
            for (int j = 0; j < POPULATION; j++) {
                if (valueList[j]==i){
                    list[i]++;
                }
            }
        }
        return list;

    }

    private double calSumCost(int[] costList, int[] samTimes) {
        double sumCost = 0;
        for (int i = 0; i < POPULATION; i++) {
            for (int j = 0; j < domain.length; j++) {
                if (valueIndexList[i] == j){
                    sumCost += Math.pow(localCostList[i],-Math.sqrt(samTimes[j]));
                }
            }
        }
        return sumCost;
    }

    private double calSumC(int id, int[] localCostList) {
        double cost = 0;
        for (int i = 0; i < POPULATION; i++) {
            cost += localCostList[i];
        }
        cost -= localCostList[id];
        return cost;
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
            if (cycleCount % (CROSS_CYCLE) == 0){
                mutation();
                gaCycleCount ++;
                minLocalCost = neighbours.length;
                sendMinCostMessage(minLocalCost);
            }
            sendValueMessage(valueIndexList);
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
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

    private void sendMinCostMessage(int minCost) {
        for (int neighborId : neighbours) {
            Message msg = new Message(this.id,neighborId,TYPE_LEVEL_MESSAGE,minCost);
            sendMessage(msg);
        }
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
