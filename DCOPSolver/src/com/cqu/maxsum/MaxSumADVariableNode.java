package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/29.
 */
public class MaxSumADVariableNode extends VariableNode {

    private List<Integer> precursorList;
    private List<Integer> successorList;
    private int receiveCount;

    public MaxSumADVariableNode(ParentInfoProvider parent, boolean blocking,int iteration) {
        super(parent, blocking);
        precursorList = new LinkedList<>();
        successorList = new LinkedList<>();
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
        System.out.println(toString() +  "reversed");
    }

    public boolean allMessageReceived(){
        return evaluateHandleCondition();
    }



    @Override
    public void addMessage(Message message) {
        receiveCount++;
        MessageContent content = (MessageContent)message.getValue();
        if (content.getFakeIndex() != -1)
            comingMessages.put(content.getFakeIndex(),content.getCube());
        else
            comingMessages.put(message.getIdSender(),content.getCube());
    }

    @Override
    public Message[] handle() {
        Message[] msgs = super.handle();
        return msgs;
    }

    @Override
    public String toString() {
        return "vn,parent:" + parent.getId() + " pre:" + precursorList + " suc:" + successorList;
    }



}
