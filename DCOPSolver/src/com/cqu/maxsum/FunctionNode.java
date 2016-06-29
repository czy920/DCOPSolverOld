package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/19.
 */
public class FunctionNode extends AbstractNode {

    public FunctionNode(ParentInfoProvider parent, boolean blocking) {
        super(parent, blocking);
        nodeType = NODE_TYPE_FUNCTION_NODE;
    }

    @Override
    public Message[] handle() {
        if (!evaluateHandleCondition()){
            return null;
        }
        List<Integer> targetList = new ArrayList<>(getDest());
        if (targetId != -1){
            targetList.retainAll(Arrays.asList(targetId));
        }
        Message[] sendMessages = new Message[targetList.size()];
        int messageIndex = 0;
        for (int id : targetList){
            for (int componentId : getSource()){
                if (id == componentId)
                    continue;
                getHyperCube().join(comingMessages.get(componentId));
            }
            MessageContent content = new MessageContent();
            content.setCube(getHyperCube().resolveFunctions(id,getFixAssignment()));
            sendMessages[messageIndex++] = new Message(parent.getId(),id,AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE,content);
        }
        targetId = -1;
        return sendMessages;
    }

    protected HyperCube getHyperCube(){
        return parent.getLocalFunction();
    }

    protected Map<Integer,Integer> getFixAssignment(){
        return null;
    }
}
