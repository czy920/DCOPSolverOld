package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.settings.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dyc on 2016/7/12.
 */
public class DsaRefiner extends AbstractLocalRefiner {
    public static final int MSG_TYPE_DSA_VALUE = 64;

    private Map<Integer,Integer> localView;
    private int cycle;

    public DsaRefiner(PunchedAgentInfoProvider punchedParent, int initValue) {
        super(punchedParent, initValue);
        localView = new HashMap<>();
        cycle = Settings.settings.getRefineCycle();
    }

    @Override
    public Message[] initRun() {
        return getValueMessages();
    }

    private Message[] getValueMessages(){
        Message[] messages = new Message[punchedParent.getNeighbour().length];
        for (int i = 0; i < messages.length; i++){
            messages[i] = new Message(punchedParent.getId(),punchedParent.getNeighbour()[i],MSG_TYPE_DSA_VALUE,valueIndex);
        }
        return messages;
    }

    @Override
    public Message[] disposeMessage(Message msg) {
        localView.put(msg.getIdSender(),(Integer)msg.getValue());
        return null;
    }

    @Override
    public Message[] allMessageReceived() {
        cycle--;
        if (canTerminate())
            return null;
        if (Math.random() < Settings.settings.getSelectProbability()){
            int minCost = getLocalCost();
            for (int i : punchedParent.getPunchedDomain()){
                int cost = calcuIndexCost(i);
                if (cost < minCost){
                    minCost = cost;
                    valueIndex = i;
                }
            }
        }
        return getValueMessages();
    }

    @Override
    public boolean canTerminate() {
        return cycle <= 0;
    }

    @Override
    public int getLocalCost() {
        return calcuIndexCost(valueIndex);
    }

    private int calcuIndexCost(int index){
        int sum = 0;
        for (int id : localView.keySet()){
            sum += punchedParent.getConstraintCost().get(id)[index][localView.get(id)];
        }
        return sum;
    }
}
