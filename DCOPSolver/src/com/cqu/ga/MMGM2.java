package com.cqu.ga;

import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

public class MMGM2 extends MALSAgentCyclenotChanged {

    public static final int POPULATION = 10;
    public final static int TYPE_VALUE_MESSAGE = 201;
    public final static int TYPE_OFFER_MESSAGE = 202;
    public final static int TYPE_GAIN_MESSAGE = 203;
    public final static int TYPE_ACCEPT_MESSAGE = 204;
    public final static int TYPE_GO_MESSAGE = 205;
    public final static int TYPE_REJECT_MESSAGE = 206;
    public final static int CYCLE_VALUE = 301;
    public final static int CYCLE_OFFER = 302;
    public final static int CYCLE_ACCEPT = 303;
    public final static int CYCLE_GAIN = 304;
    public final static int CYCLE_GO = 305;
    public final static int CYCLE_COUNT_END = 800;
    public final static String TYPE_OFFER = "offer";
    public final static String TYPE_RECEIVER = "receiver";
    private int cycleCount;
    private int cycleTag;
    private String agentType;
    private boolean isCommitted;
    private int coordinate;
    private double Q = 0.5;
    private int[] gain = new int[POPULATION];
    private int[] offerValue = new int[POPULATION];
    private int[] reeciverValue = new int[POPULATION];
    private int[] suggestValue = new int[POPULATION];
    private boolean[] isChanged = new boolean[POPULATION];

    private int[] valueIndexList = new int[POPULATION];
    private int[] localCostList = new int[POPULATION];
    private HashMap<Integer,int[]> localViewGroup = new HashMap<>();
    private HashMap<Integer,HashSet[]> offerViewGroup = new HashMap<>();
    private HashMap<Integer,int[]> gainViewGroup = new HashMap<>();
    private HashSet[] offerGain = new HashSet[POPULATION];


    public MMGM2(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
    }

    @Override
    protected void initRun() {
        super.initRun();
        cycleCount = 0;
        cycleTag = CYCLE_VALUE;
        isCommitted = false;
        for (int i = 0; i < POPULATION; i++) {
            valueIndexList[i] = (int)(Math.random()*domain.length);
            localCostList[i] = Integer.MAX_VALUE;
            isChanged[i] = false;
            gain[i] = 0;
            suggestValue[i] = -1;
            offerValue[i] = -1;
            reeciverValue[i] = -1;
        }
        sendValueMessage(valueIndexList);
    }

    private void sendValueMessage(int[] valueIndexList) {
        for (int neighborId : neighbours) {
            Message msg  = new Message(this.id,neighborId,TYPE_VALUE_MESSAGE,valueIndexList);
            sendMessage(msg);
        }
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        ResultCycleAls resultCycleAls = new ResultCycleAls();
        resultCycleAls.bestCostInCycle = bestCostInCycle;
        System.out.println(bestCostInCycle[bestCostInCycle.length-1]);
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
                diposeValueMessage(msg);
                break;
            case TYPE_OFFER_MESSAGE:
                disposeOfferMessage(msg);
                break;
            case TYPE_GAIN_MESSAGE:
                disposeGainMessage(msg);
                break;
            case TYPE_ACCEPT_MESSAGE:
                disposeAcceptMessage(msg);
                break;
            case TYPE_REJECT_MESSAGE:
                disposeRejectMessage(msg);
                break;
            case TYPE_GO_MESSAGE:
                disposeGoMessage(msg);
                break;
            case TYPE_ALSCOST_MESSAGE:
                disposeAccCostMessage(msg);
                break;
            case TYPE_BEST_MESSAGE:
                disposeBestMessage(msg);
                break;
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        cycleCount ++;
        if (cycleCount<CYCLE_COUNT_END){
            mgm2Work();
            localCostList = calLocalCost(valueIndexList);
            alsWork(valueIndexList,localCostList);
        }else {
            malsStopRunning();
        }
    }

    private void mgm2Work() {
        switch (this.cycleTag){
            case CYCLE_VALUE:
                cycleTag = CYCLE_OFFER;
                cycleForValue();
                break;
            case CYCLE_OFFER:
                cycleTag = CYCLE_ACCEPT;
                cycleForOffer();
                break;
            case CYCLE_ACCEPT:
                cycleTag = CYCLE_GAIN;
                cycleForAccept();
                break;
            case CYCLE_GAIN:
                cycleTag = CYCLE_GO;
                cycleForGain();
                break;
            case CYCLE_GO:
                cycleTag = CYCLE_VALUE;
                cycleForGo();
                break;
        }
    }

    private void cycleForGo() {
        if (!isCommitted){
            for (int i = 0; i < POPULATION; i++) {
                if (isChanged[i]){
                    valueIndexList[i] = suggestValue[i];
                }
            }
        }
        sendValueMessage(valueIndexList);
    }

    private void cycleForGain() {
        boolean isGo[] = new boolean[POPULATION];
        boolean isSend = false;
        for (int i = 0; i < POPULATION; i++) {
            isChanged[i] = false;
            int tempGain = 0;
            isGo[i] = false;
            for (int neighborId : gainViewGroup.keySet()) {
                if (gainViewGroup.get(neighborId)[i]>tempGain){
                    tempGain = gainViewGroup.get(neighborId)[i];
                }
            }
            if (isCommitted){
                if (gain[i] > tempGain){
                    isGo[i] = true;
                    isSend = true;
                }
            }else {
                if (gain[i] > tempGain){
                    isChanged[i] = true;
                }
            }
        }
        if (isSend){
            sendGoMessage(coordinate,isGo);
        }
    }

    private void sendGoMessage(int coordinate,boolean[] goList) {
//        System.out.println("cycleCount:  " + cycleCount + "agentType：  " + this.agentType + "AgentId:   " + this.id + "coordinate:  " +coordinate + "suggestValue: " + suggestValue[1] + "gain:  " + gain[1]);
        Message msg = new Message(this.id,coordinate,TYPE_GO_MESSAGE,goList);
        sendMessage(msg);
    }

    private void cycleForAccept() {
        if (!isCommitted){
            sendGainMessage(gain);
        }else {
            sendNewGainMessage(gain);
        }

    }

    private void sendNewGainMessage(int[] gainList) {
        for (int neighborId : neighbours) {
            if (neighborId != coordinate){
                Message message = new Message(this.id,neighborId,TYPE_GAIN_MESSAGE,gainList);
                sendMessage(message);
            }
        }
    }

    private void sendGainMessage(int[] gainlist) {
        for (int neighborId : neighbours) {
            Message message = new Message(this.id,neighborId,TYPE_GAIN_MESSAGE,gainlist);
            sendMessage(message);
        }
    }

    private void cycleForOffer() {//选一个最优的offer对接
        if (isCommitted){//接收到offer消息的节点
            int [] tempReceiverValue = new int[POPULATION];
            int [] tempOfferValue = new int[POPULATION];
            int tempGainValue = 0;
            int [] gainList = new int[POPULATION];
            int [] globalGain = new int[POPULATION];
            for (int i = 0; i < POPULATION; i++) {
                for (int offerId : offerViewGroup.keySet()){
                    @SuppressWarnings("unchecked")
                    HashSet<Offer> offerSet = offerViewGroup.get(offerId)[i];
                    for (Offer offer:offerSet){
                        int gainValue = offer.offerGain + calReciverGain(i,offerId,offer.offerValue,offer.receiverValue);
                        if (gainValue > tempGainValue){
                            coordinate = offerId;
                            tempGainValue = gainValue;
                        }
                    }
                }
            }

            int tempGain[] = new int[POPULATION];
            boolean isChanged[] = new boolean[POPULATION];
            for (int i = 0; i < POPULATION; i++) {
                tempGain[i] = gain[i];
                isChanged[i] = false;
                if (offerViewGroup.get(coordinate)[i]!=null) {
                    @SuppressWarnings("unchecked")
                    HashSet<Offer> offerSet = offerViewGroup.get(coordinate)[i];
                    for (Offer offer:offerSet) {
                        globalGain[i] = offer.offerGain + calReciverGain(i,coordinate,offer.offerValue,offer.receiverValue);
                        if (globalGain[i] > tempGain[i]){
                            isChanged[i] = true;
                            tempGain[i] = globalGain[i];
                            tempOfferValue[i] = offer.offerValue;
                            tempReceiverValue[i] = offer.receiverValue;
                        }else {
                            tempOfferValue[i] = -1;
                            tempReceiverValue[i] = -1;
                        }
                    }
                }
            }

            AcceptMessage acceptMessage = new AcceptMessage(tempOfferValue,tempReceiverValue,tempGain,isChanged);
            for (int offerId : offerViewGroup.keySet()) {
                if (offerId == coordinate){
                    for (int i = 0; i < POPULATION; i++) {
                        if (isChanged[i]){
                            gain[i] = tempGain[i];
                            suggestValue[i] = tempReceiverValue[i];
                        }
                    }
                    sendAcceptMessage(offerId,acceptMessage);
//                    System.out.println("cycleCount: " + cycleCount+ "acceptOfferId: " + offerId);
                }else {
                    sendRejectMessage(offerId);
//                    System.out.println("cycleCount: " + cycleCount+ "rejectOfferId: " + offerId);
                }
            }
        }
    }

    private int calReciverGain(int i, int offerId, int offerValue, int receiverValue) {
        int formerlCost = 0;
        int latterCost = 0;
        int gain = 0;
        for (int neighborId : neighbours) {
            if (neighborId!=offerId){
                formerlCost += constraintCosts.get(neighborId)[valueIndexList[i]][localViewGroup.get(neighborId)[i]];
                latterCost += constraintCosts.get(neighborId)[receiverValue][localViewGroup.get(neighborId)[i]];
            }
        }
        gain = formerlCost - latterCost;
        return gain;
    }

    private void sendRejectMessage(int neighborId) {
        Message message = new Message(this.id,neighborId,TYPE_REJECT_MESSAGE,null);
        sendMessage(message);
    }

    private void sendAcceptMessage(int tempIndex, AcceptMessage acceptMsg) {
        Message message = new Message(this.id,tempIndex,TYPE_ACCEPT_MESSAGE,acceptMsg);
        sendMessage(message);
    }

    private void cycleForValue() {
        localCostList = calLocalCost(valueIndexList);
        int mgmGain[] = new int[POPULATION];
        int tempSuggestValue = -1;
        isCommitted = false;
        coordinate = -1;
        int maxGain = 0;
        for (int i = 0; i < POPULATION; i++) {
            mgmGain[i] = 0;
            int tempGain = 0;
            for (int j = 0; j < domain.length; j++) {
                mgmGain[i] = localCostList[i] - calCost(j,i);
                if (mgmGain[i] > tempGain){
                    tempGain = mgmGain[i];
                    tempSuggestValue = j;
                }
            }
            if (tempGain>maxGain){
                maxGain = tempGain;
            }
            gain[i] = tempGain;
            if (tempGain > 0) {
                suggestValue[i] = tempSuggestValue;
            }else {
                suggestValue[i] = valueIndexList[i];
            }
        }
        agentType = TYPE_RECEIVER;
        double q = Math.random();
        if (q < Q){
            agentType = TYPE_OFFER;
            coordinate = neighbours[(int)(Math.random()*neighbours.length)];
            if (maxGain>0){//并行的多个mgm2中，只要有一个mgm2的最大增益大于0.这个节点就可以成为offer
                for (int i = 0; i < POPULATION; i++) {
                    HashSet<Offer> offerSet = new HashSet<>();
                    if (gain[i]>0){
                        Offer offer = new Offer(suggestValue[i],localViewGroup.get(coordinate)[i],gain[i]);
                        offerSet.add(offer);
                        offerValue[i] = suggestValue[i];
                        reeciverValue[i] = localViewGroup.get(coordinate)[i];
                    }else {
                        offerValue[i] = -1;
                        reeciverValue[i] = -1;
                    }
                    offerGain[i] = offerSet;
                }
                sendOfferMessage(coordinate,offerGain);//只有offer节点发送给coordinate
            }
        }
    }

    private void sendOfferMessage(int coordinateIndex, HashSet[] offerList) {
        Message msg = new Message(this.id,coordinateIndex,TYPE_OFFER_MESSAGE,offerList);
        sendMessage(msg);
    }

    private int calCost(int valueIndex,int id) {
        int cost = 0;
        for (int neighborId : neighbours) {
            cost += constraintCosts.get(neighborId)[valueIndex][localViewGroup.get(neighborId)[id]];
        }
        return cost;
    }

    private int[] calLocalCost(int[] valueIndexList) {
        int[] cost = new int[POPULATION];
        for (int i = 0; i < POPULATION; i++) {
            cost[i]  = 0;
            for (int neighborId : neighbours) {
                cost[i] += constraintCosts.get(neighborId)[valueIndexList[i]][localViewGroup.get(neighborId)[i]];
            }
        }
        return cost;
    }

    private void disposeGoMessage(Message message) {
        boolean [] goList = (boolean[]) message.getValue();
//        System.out.println("disposeGoMessage：" + "cycleCount: " + cycleCount + "   offerId: " + this.id + "receiverId: " + message.getIdSender() + "gain" + gain[1] + "isGo" + goList[1]);
        for (int i = 0; i < POPULATION; i++) {
            if (suggestValue[i]!=-1 && goList[i]){
                valueIndexList[i] = suggestValue[i];
            }
        }
    }

    private void disposeRejectMessage(Message msg) {
        isCommitted = false;
    }

    private void disposeGainMessage(Message msg) {
        gainViewGroup.put(msg.getIdSender(), (int[]) msg.getValue());
    }

    private void disposeAcceptMessage(Message msg) {
        isCommitted = true;
        coordinate = msg.getIdSender();
        AcceptMessage accmsg = (AcceptMessage) msg.getValue();
        isChanged = ((AcceptMessage) msg.getValue()).isChanged;
        offerValue = accmsg.OfferValue;
        reeciverValue = accmsg.receiverValue;
        for (int i = 0; i < POPULATION; i++) {
            if (isChanged[i]){
                gain[i] = accmsg.globalGain[i];
                suggestValue[i] = accmsg.OfferValue[i];
            }
        }
//        System.out.println("cycleCount:  " + cycleCount +"disposeAcceptMessage: " + "agentId: " + this.id + "   receiverId: " + msg.getIdSender() + "   offerValue: "+accmsg.OfferValue[1]+"   receiverValue: "+accmsg.receiverValue[1] + "   gain: " + gain[1]);
    }

    private void disposeOfferMessage(Message msg) {
        if (agentType.equals(TYPE_RECEIVER)){
            isCommitted = true;
            offerViewGroup.put(msg.getIdSender(), (HashSet[]) msg.getValue());
        }
    }

    private void diposeValueMessage(Message msg) {
        int [] neighborVaule = (int[]) msg.getValue();
        localViewGroup.put(msg.getIdSender(), (int[]) msg.getValue());
    }
    @Override
    protected void messageLost(Message msg) {
    }
}
