package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dyc on 2017/3/28.
 */
public class ADConcurrentVariableNode extends VariableNode{
    private List<Integer> downAgents;
    private List<Integer> upAgents;
    private int downReceivedCount;
    private int upReceivedCount;
    private boolean downSent;
    private boolean upSent;

    public ADConcurrentVariableNode(AgentInfoProvider agentInfoProvider,List<Integer> downAgents) {
        super(agentInfoProvider);
        this.downAgents = downAgents;
        upAgents = new LinkedList<>();
        for (int neighbourId : agentInfoProvider.neighbours){
            if (!downAgents.contains(neighbourId)){
                upAgents.add(neighbourId);
            }
        }

    }

    @Override
    protected boolean evaluateHandleCondition() {
        return (downAgents.size() == downReceivedCount && !upSent) || (upAgents.size() == upReceivedCount && !downSent);
    }

    public boolean evaluateDecisionCondition(){
        return upAgents.size() == upReceivedCount && !downSent;
    }

    @Override
    public void addMessage(Message message) {
        int senderId = ((UtilityMessage)message.getValue()).getFakeIndex();
        if (downAgents.contains(senderId)){
            downReceivedCount++;
        }
        else {
            upReceivedCount++;
        }
        super.addMessage(message);
    }

    public void reset(){
        downReceivedCount = upReceivedCount = 0;
        downSent = upSent = false;
    }

    @Override
    protected List<Integer> getTarget() {
        List<Integer> target = new LinkedList<>();
        if (downAgents.size() == downReceivedCount && !upSent){
            target.addAll(upAgents);
            upSent = true;
        }
        if (upAgents.size() == upReceivedCount && !downSent){
            target.addAll(downAgents);
            downSent = true;
        }

        return target;
    }
}
