package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.settings.Settings;

import java.util.Map;

/**
 * Created by dyc on 2017/3/23.
 */
public class DSARefiner extends AbstractRefiner {

    private int previousValueIndex;

    public DSARefiner(int valueIndex, Map<Integer, Integer> localView, int maxCycle, Map<Integer, int[][]> constraintCost, int[] neighbours, int domainLength, int id) {
        super(valueIndex, localView, maxCycle, constraintCost, neighbours, domainLength, id);
        previousValueIndex = valueIndex;
    }
    @Override
    public Message[] initRun() {
        return sendValueMessage();
    }

    private Message[] sendValueMessage(){
        Message[] messages = new Message[neighbours.length];
        int index = 0;
        for (int neighbourId : neighbours){
            messages[index++] = new Message(id,neighbourId,MessageTypes.MSG_TYPE_REFINE_VALUE,valueIndex);
        }
        return messages;
    }

    private int calculateLocalCost(int index){
        int sum = 0;
        for (int neighbourId : neighbours){
            sum += constraintCost.get(neighbourId)[index][localView.get(neighbourId)];
        }
        return sum;
    }

    private void decision(){
        int minCost = Integer.MAX_VALUE;
        for (int i = 0; i < domainLength; i++){
            int cost = calculateLocalCost(i);
            if (minCost > cost){
                minCost = cost;
                valueIndex = i;
            }
        }
        if (valueIndex != previousValueIndex){
            previousValueIndex = valueIndex;
            System.out.println("value changed");
        }
    }

    @Override
    public Message[] disposeMessage(Message message) {
        localView.put(message.getIdSender(),(int)message.getValue());
        return null;
    }

    @Override
    public Message[] allMessageDisposed() {
        currentCycle++;
        if (Math.random() < Settings.settings.getSelectProbability()) {
            decision();
            return sendValueMessage();
        }
        return null;
    }
}
