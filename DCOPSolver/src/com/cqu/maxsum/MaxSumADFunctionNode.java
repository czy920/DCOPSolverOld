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
    private boolean enableVP;
    private Map<Integer,Integer> fixAssignment;

    public MaxSumADFunctionNode(ParentInfoProvider parent, boolean blocking,String constraintName,int iteration) {
        super(parent, blocking);
        this.constraintName = constraintName;
        precursorList = new LinkedList<>();
        successorList = new LinkedList<>();
        enableVP = false;
        fixAssignment = new HashMap<>();
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
        return receiveCount >= precursorList.size();
    }

    public void reverse(){
        List<Integer> tmpList = precursorList;
        precursorList = successorList;
        successorList = tmpList;
        receiveCount = 0;
        fixAssignment.clear();
        System.out.println(toString() +  "reversed");
    }

    @Override
    protected HyperCube getHyperCube() {
        if (localFunction == null)
            localFunction = parent.getLocalFunction(constraintName);
        localFunction.setSingleConstraint(true);
        return localFunction;
    }

    @Override
    public Message[] handle() {
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
        return wrapedMessages;
    }

    @Override
    public void addMessage(Message message) {
        receiveCount++;
        MessageContent content = (MessageContent) message.getValue();
        if (content.getOptimalIndex() > 0)
            fixAssignment.put(message.getIdSender(),content.getOptimalIndex());
        super.addMessage(message);
    }

    @Override
    public String toString() {
        return "fn,parent:" + parent.getId() + " pre:" + precursorList + " suc:" + successorList;
    }

    @Override
    protected Map<Integer, Integer> getFixAssignment() {
        if (!enableVP)
            return null;
        Map<Integer,Integer> partialAssignment = new HashMap<>();
        for (int id : precursorList){
            if (fixAssignment.containsKey(id))
                partialAssignment.put(id,fixAssignment.get(id));
        }
        return partialAssignment;
    }

    public void setEnableVP(boolean enableVP) {
        this.enableVP = enableVP;
    }
}
