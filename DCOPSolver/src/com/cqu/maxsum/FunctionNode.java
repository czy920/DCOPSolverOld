package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dyc on 2017/3/6.
 */
public class FunctionNode extends AbstractNode {

    protected int oppositeId;
    protected LocalFunction localFunction;
    /**
     * Constructor for function node
     * @param agentInfoProvider The wrapper class for host agent information
     * @param oppositeId  Opposite agent id
     * @param localFunction The constraint matrix
     */
    public FunctionNode(AgentInfoProvider agentInfoProvider,int oppositeId,LocalFunction localFunction) {
        super(agentInfoProvider);
        this.oppositeId = oppositeId;
        this.localFunction = localFunction;
        initReceivedMessages();
    }

    @Override
    protected void initReceivedMessages() {
        receivedMessages.put(agentInfoProvider.agentId,new UtilityMessage(new int[agentInfoProvider.domainSize]));
        receivedMessages.put(oppositeId,new UtilityMessage(new int[agentInfoProvider.neighboursDomainSize.get(oppositeId)]));
    }

    @Override
    protected boolean evaluateHandleCondition() {
        return true;
    }

    @Override
    public Message[] handle() {
        if (evaluateHandleCondition()){
            List<Integer> target = getTarget();
            Message[] messages = new Message[target.size()];
            int index = 0;
            for (int targetId : target){
                int[] projectedUtility = null;
                int[] context = new int[getDomainLength(targetId)];
                int sourceId = -1;
                for (int id : receivedMessages.keySet()){
                    if (id != targetId){
                        sourceId = id;
                        projectedUtility = localFunction.project(targetId,receivedMessages.get(id).getUtility(),context);
                        break;
                    }
                }
                UtilityMessage utilityMessage = new UtilityMessage(projectedUtility);
                utilityMessage.setFakeIndex(sourceId);
                utilityMessage.setContext(context);
                messages[index++] = new Message(agentInfoProvider.agentId,targetId,MessageTypes.MSG_TYPE_FUNCTION_TO_VARIABLE,utilityMessage);
            }
            return messages;
        }
        return null;
    }

    @Override
    protected List<Integer> getTarget() {
        return Arrays.asList(agentInfoProvider.agentId,oppositeId);
    }
}
