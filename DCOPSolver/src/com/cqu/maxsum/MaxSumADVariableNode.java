package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/29.
 */
public class MaxSumADVariableNode extends VariableNode {

    private List<Integer> precursorList;
    private List<Integer> successorList;
    private int sendCount;
    private int receiveCount;
    private ReverseController reverseController;

    public MaxSumADVariableNode(ParentInfoProvider parent, boolean blocking,int iteration) {
        super(parent, blocking);
        precursorList = new LinkedList<>();
        successorList = new LinkedList<>();
        reverseController = new ReverseController(iteration,successorList,precursorList,parent.getId(),AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION);
    }

    public void addNeighbours(int id,int domainSize,boolean isPrecursor){
        super.addNeighbours(id,domainSize);
        if (isPrecursor){
            precursorList.add(id);
        }
        else {
            successorList.add(id);
        }
    }

    @Override
    protected Set<Integer> getDest() {
        return new HashSet<>(successorList);
    }

    @Override
    protected Set<Integer> getSource() {
        return comingMessages.keySet();
    }

    @Override
    protected boolean evaluateHandleCondition() {
       return receiveCount == precursorList.size();
    }

    public void reverse(){
        List<Integer> tmpList = precursorList;
        precursorList = successorList;
        successorList = tmpList;
        receiveCount = 0;
        sendCount = 0;
        reverseController.update(precursorList,successorList);
        System.out.println(toString() +  "reversed");
    }

    public boolean isLeafNode(){
        return successorList.size() == 0;
    }

    public boolean isRootNode(){return precursorList.size() == 0;}

    public boolean allMessageReceived(){
        return evaluateHandleCondition();
    }

    public boolean allMessageSend(){
        return sendCount == successorList.size();
    }


    @Override
    public void addMessage(Message message) {
        if (message.getType() == AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE)
            receiveCount++;
        else if ((message.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_ACK) != 0 || (message.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_REQ) != 0){
            reverseController.addMessage(message);
            return;
        }
        MessageContent content = (MessageContent)message.getValue();
        if (content.getFakeIndex() != -1)
            comingMessages.put(content.getFakeIndex(),content.getCube());
        else
            comingMessages.put(message.getIdSender(),content.getCube());
    }

    @Override
    public Message[] handle() {
        if (allMessageSend() && allMessageReceived()){
            Message[] reverseMessages = reverseController.handle();
            if (reverseController.canReverse()){
                reverse();
            }
            return reverseMessages;
        }
        Message[] msgs = super.handle();

        sendCount += msgs == null ? 0 : msgs.length;
        return msgs;
    }

    @Override
    public String toString() {
        return "vn,parent:" + parent.getId() + " pre:" + precursorList + " suc:" + successorList;
    }

    public boolean canTerminate(){
        return reverseController.canTerminate();
    }


}
