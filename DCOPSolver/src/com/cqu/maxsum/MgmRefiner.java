package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.settings.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/6/21.
 */
public class MgmRefiner extends AbstractLocalRefiner {
    public static final int MSG_TYPE_MGM_VALUE = 64;
    public static final int MSG_TYPE_MGM_GAIN = 128;

    private int currentMsgType;
    private int selectValue;
    private int gain;
    private Map<Integer,Integer> localView;
    private Map<Integer,Integer> gainView;
    private int cycle;
    private int valueReceiveCount;
    private int gainReceiveCount;

    public MgmRefiner(PunchedAgentInfoProvider punchedParent, int initValue) {
        super(punchedParent, initValue);
        localView = new HashMap<>();
        gainView = new HashMap<>();
        cycle = Settings.settings.getRefineCycle();
    }

    @Override
    public Message[] initRun() {
        Message[] messages = getValueMessages();
        System.out.println(punchedParent.getId()+" inited");
        return messages;
    }

    private Message[] getValueMessages() {
        Message[] messages = new Message[punchedParent.getNeighbour().length];
        for (int i = 0; i < messages.length; i++){
            messages[i] = new Message(punchedParent.getId(),punchedParent.getNeighbour()[i],MSG_TYPE_MGM_VALUE,valueIndex);
        }
        return messages;
    }

    private Message[] getGainMessages() {
        Message[] messages = new Message[punchedParent.getNeighbour().length];
        for (int i = 0; i < messages.length; i++){
            messages[i] = new Message(punchedParent.getId(),punchedParent.getNeighbour()[i],MSG_TYPE_MGM_GAIN,gain);
        }
        return messages;
    }

    @Override
    public Message[] disposeMessage(Message msg) {
        currentMsgType = msg.getType();
        if (msg.getType() == MSG_TYPE_MGM_VALUE){
            localView.put(msg.getIdSender(),(Integer)msg.getValue());
            valueReceiveCount++;
        }
        else if (msg.getType() == MSG_TYPE_MGM_GAIN){
            gainView.put(msg.getIdSender(),(Integer)msg.getValue());
            gainReceiveCount++;
        }
        return null;
    }

    @Override
    public Message[] allMessageReceived() {
        if (currentMsgType == MSG_TYPE_MGM_VALUE && valueReceiveCount == punchedParent.getNeighbour().length){
            valueReceiveCount = 0;
            cycle--;
            if (canTerminate())
                return null;
            Map<Integer,Integer> optimalCost = new HashMap<>();
            for (int i : punchedParent.getPunchedDomain()){
                int cost = 0;
                for (int id : punchedParent.getNeighbour()){
                    cost += punchedParent.getConstraintCost().get(id)[i][localView.get(id)];
                }
                optimalCost.put(i,cost);
            }
            int minCost = getLocalCost();
            for (int i : optimalCost.keySet()){
                if (minCost > optimalCost.get(i)){
                    minCost = optimalCost.get(i);
                    selectValue = i;
                }
            }
            gain = getLocalCost() - minCost;
            return getGainMessages();
        }
        else if (currentMsgType == MSG_TYPE_MGM_GAIN && gainReceiveCount == punchedParent.getNeighbour().length)
        {
            gainReceiveCount = 0;
            for (int id : gainView.keySet()){
                if (gainView.get(id) >= gain){
                    return getValueMessages();
                }
            }
            valueIndex = selectValue;
            return getValueMessages();

        }
        return new Message[0];
    }

    @Override
    public boolean canTerminate() {
        return cycle <= 0;
    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        for (int id : punchedParent.getNeighbour()){
            sum += punchedParent.getConstraintCost().get(id)[valueIndex][localView.get(id)];
        }
        return sum;
    }
}
