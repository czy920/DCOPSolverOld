package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.Map;

/**
 * Created by dyc on 2017/3/20.
 */
public class MGMRefiner extends AbstractRefiner {

    private static final int PHASE_GAIN = 0;
    private static final int PHASE_VALUE = 1;

    private int maxGain;
    private int maxGainIndex;
    private boolean canReplaceValue = true;
    private int currentPhase;

    public MGMRefiner(int valueIndex, Map<Integer, Integer> localView, int maxCycle,Map<Integer,int[][]> constraintCost,int[] neighbours,int domainLength,int id) {
        super(valueIndex, localView, maxCycle,constraintCost,neighbours,domainLength,id);
    }

    private void decision(){
        maxGain = -1;
        int currentCost = calculateCost(valueIndex);
        for (int i = 0; i < domainLength; i++){
            int gain = currentCost - calculateCost(i);
            if (gain > maxGain){
                maxGain = gain;
                maxGainIndex = i;
            }
        }
    }

    private int calculateCost(int value){
        int sum = 0;
        for (int neighbourId : neighbours){
            sum += constraintCost.get(neighbourId)[value][localView.get(neighbourId)];
        }
        return sum;
    }



    @Override
    public Message[] initRun() {
        decision();
        currentPhase = PHASE_GAIN;
        return sendGainMessage();
    }

    private Message[] sendGainMessage(){
        Message[] messages = new Message[neighbours.length];
        int index = 0;
        for (int neighbourId : neighbours){
            messages[index++] = new Message(id,neighbourId,MessageTypes.MSG_TYPE_REFINE_GAIN,maxGain);
        }
        return messages;
    }

    private Message[] sendValueMessage(){
        Message[] messages = new Message[neighbours.length];
        int index = 0;
        for (int neighbourId : neighbours){
            messages[index++] = new Message(id,neighbourId,MessageTypes.MSG_TYPE_REFINE_VALUE,valueIndex);
        }
        return messages;
    }
    @Override
    public Message[] disposeMessage(Message message) {
        switch (message.getType()){
            case MessageTypes.MSG_TYPE_REFINE_GAIN:
                if (maxGain < (int)message.getValue()){
                    canReplaceValue = false;
                }
                break;
            case MessageTypes.MSG_TYPE_REFINE_VALUE:
                localView.put(message.getIdSender(),(int) message.getValue());
        }
        return null;
    }

    @Override
    public Message[] allMessageDisposed() {
        currentCycle++;
        if (currentPhase == PHASE_GAIN) {
            if (canReplaceValue) {
                valueIndex = maxGainIndex;
            }
            canReplaceValue = true;
            currentPhase = PHASE_VALUE;
            return sendValueMessage();
        }
        else if (currentPhase == PHASE_VALUE){
            decision();
            currentPhase = PHASE_GAIN;
            return sendGainMessage();
        }
        else {
            throw new RuntimeException("phase not in the scope!");
        }

    }
}
