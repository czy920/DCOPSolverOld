package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dyc on 2017/3/28.
 */
public class ADConcurrentFunctionNode extends FunctionNode {
    private int downAgent;
    private int upAgent;
    private boolean downReceived;
    private boolean upReceived;
    private boolean downSent;
    private boolean upSent;
    private boolean valuePropagation = true;

    public ADConcurrentFunctionNode(AgentInfoProvider agentInfoProvider, int oppositeId, LocalFunction localFunction,int downAgent) {
        super(agentInfoProvider, oppositeId, localFunction);
        this.downAgent = downAgent;
        upAgent = downAgent == agentInfoProvider.agentId ? oppositeId : agentInfoProvider.agentId;
    }

    @Override
    protected boolean evaluateHandleCondition() {
        return (downReceived && !upSent) || (upReceived && !downSent);
    }

    @Override
    public void addMessage(Message message) {
        int senderId = message.getIdSender();
        if (senderId == downAgent){
            downReceived = true;
        }
        else {
            upReceived = true;
        }
        super.addMessage(message);
    }

    public void reset(){
        downReceived = upReceived = false;
        downSent = upSent = false;
    }

    @Override
    protected List<Integer> getTarget() {
        List<Integer> target = new LinkedList<>();
        if (downReceived && !upSent){
            target.add(upAgent);
            upSent = true;
        }
        if (upReceived && !downSent){
            target.add(downAgent);
            downSent = true;
        }
        return target;
    }

    @Override
    public Message[] handle() {
        if (evaluateHandleCondition()){
            List<Integer> target = getTarget();
            Message[] messages = new Message[target.size()];
            int index = 0;
            for (int targetId : target){
                int[] projectedUtility = null;
                int sourceId = -1;
                for (int id : receivedMessages.keySet()){
                    if (id != targetId){
                        sourceId = id;
                        if (targetId == upAgent) {
                            projectedUtility = localFunction.project(targetId, receivedMessages.get(id).getUtility());
//                            Arrays.fill(projectedUtility,0);
                        }
                        else {
                            projectedUtility = localFunction.restrict(targetId, receivedMessages.get(sourceId).getValueIndex());
                        }
                        break;
                    }
                }
                UtilityMessage utilityMessage = new UtilityMessage(projectedUtility);
                utilityMessage.setFakeIndex(sourceId);
                messages[index++] = new Message(agentInfoProvider.agentId,targetId,MessageTypes.MSG_TYPE_FUNCTION_TO_VARIABLE,utilityMessage);
            }
            return messages;
        }
        return null;
    }
}
