package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dyc on 2017/3/6.
 */
public class VariableNode extends AbstractNode {

    private List<Integer> targets;

    public VariableNode(AgentInfoProvider agentInfoProvider) {
        super(agentInfoProvider);
        initReceivedMessages();
    }

    @Override
    protected void initReceivedMessages() {
        for (int neighbour : agentInfoProvider.neighbours){
            receivedMessages.put(neighbour,new UtilityMessage(new int[agentInfoProvider.neighboursDomainSize.get(neighbour)]));
        }
    }

    @Override
    public void addMessage(Message message) {
        UtilityMessage utilityMessage = (UtilityMessage) message.getValue();
        receivedMessages.put(utilityMessage.getFakeIndex(),utilityMessage);
    }

    @Override
    protected boolean evaluateHandleCondition() {
        return true;
    }

    @Override
    public Message[] handle() {
        if (evaluateHandleCondition()){
            List<Integer> targets = getTarget();
            Message[] messages = new Message[targets.size()];
            int index = 0;
            for (int targetId : targets){
                UtilityMessage tmpUtilityMessage = new UtilityMessage(new int[agentInfoProvider.domainSize]);
                for (int id : receivedMessages.keySet()){
                    if (targetId != id){
                        tmpUtilityMessage.addUtility(receivedMessages.get(id));
                    }
                }
                if (agentInfoProvider.controlledFunctionNodes.contains(targetId)){
                    tmpUtilityMessage.setFakeIndex(targetId);
                    messages[index++] = new Message(agentInfoProvider.agentId,agentInfoProvider.agentId,MessageTypes.MSG_TYPE_VARIABLE_TO_FUNCTION,tmpUtilityMessage);
                }
                else {
                    tmpUtilityMessage.setFakeIndex(agentInfoProvider.agentId);
                    messages[index++] = new Message(agentInfoProvider.agentId,targetId,MessageTypes.MSG_TYPE_VARIABLE_TO_FUNCTION,tmpUtilityMessage);
                }

            }
            return messages;
        }
        return null;
    }

    @Override
    protected List<Integer> getTarget() {
        if (targets != null)
            return targets;
        targets = new LinkedList<>();
        for (int neighbor : agentInfoProvider.neighbours){
            targets.add(neighbor);
        }
        return targets;
    }

    protected int findOptima(){
        int[] utility = new int[agentInfoProvider.domainSize];
        Arrays.fill(utility,0);
        UtilityMessage sumUtility = new UtilityMessage(utility);
        for (UtilityMessage utilityMessage : receivedMessages.values()){
            sumUtility.addUtility(utilityMessage);
        }
        return sumUtility.getOptimalIndex();
    }
}
