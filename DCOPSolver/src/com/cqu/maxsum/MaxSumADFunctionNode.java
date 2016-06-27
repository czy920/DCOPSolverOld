package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/29.
 */
public class MaxSumADFunctionNode extends FunctionNode {

    private List<Integer> precursorList;
    private List<Integer> successorList;
    private String constraintName;
    private HyperCube localFunction;
    private int receiveCount;
    private int sendCount;
    private ReverseController reverseController;

    public MaxSumADFunctionNode(ParentInfoProvider parent, boolean blocking,String constraintName,int iteration) {
        super(parent, blocking);
        this.constraintName = constraintName;
        precursorList = new LinkedList<>();
        successorList = new LinkedList<>();
        reverseController = new ReverseController(iteration,successorList,precursorList,parent.getId(),AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE);
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
        sendCount = 0;
        receiveCount = 0;
        reverseController.update(precursorList,successorList);
        System.out.println(toString() +  "reversed");
    }

    @Override
    protected HyperCube getHyperCube() {
        if (localFunction == null)
            localFunction = parent.getLocalFunction(constraintName);
        localFunction.setSingleConstraint(true);
        return localFunction;
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
    public Message[] handle() {
        if (allMessageSend() && allMessageReceived()){
            Message[] reverseMessages = reverseController.handle();
            if (reverseController.canReverse()){
                reverse();
            }
            return reverseMessages;
        }
        Message[] msgs = super.handle();
        if (msgs == null)
            return null;
        Message[] wrapedMessages = new Message[msgs.length];
        for (int i = 0; i < wrapedMessages.length; i++){
            if (parent.getId() == msgs[i].getIdReceiver()){
                ((MessageContent)(msgs[i].getValue())).setFakeIndex(Integer.parseInt(constraintName));
                wrapedMessages[i] = new Message(msgs[i].getIdSender(),msgs[i].getIdReceiver(),msgs[i].getType(),msgs[i].getValue());
            }
            else {
                wrapedMessages[i] = msgs[i];
            }
        }
        sendCount += msgs == null ? 0 : msgs.length;
        return wrapedMessages;
    }

    @Override
    public void addMessage(Message message) {
        if (message.getType() == AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION)
            receiveCount++;
        else if ((message.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_ACK) != 0|| (message.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_REQ) != 0){
            reverseController.addMessage(message);
            return;
        }
        super.addMessage(message);
    }

    @Override
    public String toString() {
        return "fn,parent:" + parent.getId() + " pre:" + precursorList + " suc:" + successorList;
    }

    public boolean canTerminate(){
        return reverseController.canTerminate();
    }
}
