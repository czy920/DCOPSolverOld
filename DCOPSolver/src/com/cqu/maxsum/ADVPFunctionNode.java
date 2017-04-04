package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dyc on 2017/3/15.
 */
public class ADVPFunctionNode extends FunctionNode {
    private int precursor;
    private int successor;
    private boolean valuePropagation;

    /**
     * Constructor for Max-sum_AD function node
     * @param agentInfoProvider The wrapper class for host agent information
     * @param oppositeId  Opposite agent id
     * @param localFunction The constraint matrix
     * @param successor The initial successor of the function node
     */
    public ADVPFunctionNode(AgentInfoProvider agentInfoProvider, int oppositeId, LocalFunction localFunction,int successor) {
        super(agentInfoProvider, oppositeId, localFunction);
        this.successor = successor;
        this.precursor = oppositeId == successor ? agentInfoProvider.agentId : oppositeId;
        valuePropagation = false;
    }


    public void changeDirection(){
        int tmp = precursor;
        precursor = successor;
        successor = tmp;
    }

    public void setValuePropagation(boolean valuePropagation) {
        this.valuePropagation = valuePropagation;
    }

    @Override
    public Message[] handle() {
        // If value propagation is not enabled, produce messages just like max-sum dose
        if (!valuePropagation) {
            return super.handle();
        }
        else {
            // else produce message by restricting utility function to certain row. Refers to [Ziyu, Yanchen et.al 2017] for detail
            if (evaluateHandleCondition()){
                List<Integer> target = getTarget();
                Message[] messages = new Message[target.size()];
                int index = 0;
                for (int targetId : target){
                    int[] restrictedUtility = null;
                    int sourceId = -1;
                    for (int id : receivedMessages.keySet()){
                        if (id != targetId){
                            sourceId = id;
                            restrictedUtility = localFunction.restrict(targetId,receivedMessages.get(sourceId).getValueIndex());
                            break;
                        }
                    }
                    UtilityMessage utilityMessage = new UtilityMessage(restrictedUtility);
                    int[] context = new int[getDomainLength(targetId)];
                    Arrays.fill(context,receivedMessages.get(sourceId).getValueIndex());
                    utilityMessage.setContext(context);
                    utilityMessage.setFakeIndex(sourceId);
                    messages[index++] = new Message(agentInfoProvider.agentId,targetId,MessageTypes.MSG_TYPE_FUNCTION_TO_VARIABLE,utilityMessage);
                }
                return messages;
            }
            return null;
        }
    }

    @Override
    protected List<Integer> getTarget() {
        return Arrays.asList(successor);
    }
}
