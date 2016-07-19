package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/29.
 */
public class MaxSumADVariableNode extends VariableNode {

    private List<Integer> precursorList;
    private List<Integer> successorList;
    private boolean decisionFlag = false;

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
        for (int id : precursorList){
            if (!receiveFlag.containsKey(id)  || !receiveFlag.get(id)){
                return false;
            }
        }
       return true;
    }

    public void reverse(){
        List<Integer> tmpList = precursorList;
        precursorList = successorList;
        successorList = tmpList;
        receiveFlag.clear();
        decisionFlag = false;
        //System.out.println(toString() +  "reversed");
    }

    public void swap(List<Integer> neighbours){
        for (int id : neighbours){
            if (precursorList.contains(id)){
                precursorList.remove((Object)id);
                successorList.add(id);
                receiveFlag.remove(id);
            }
            else if (successorList.contains(id)){
                successorList.remove((Object)id);
                precursorList.add(id);
                receiveFlag.put(id,false);
            }
        }
        decisionFlag = false;
    }

    @Override
    public void addMessage(Message message) {
        MessageContent content = (MessageContent)message.getValue();
        if (content.getFakeIndex() != -1) {
            comingMessages.put(content.getFakeIndex(), content.getCube());
            receiveFlag.put(content.getFakeIndex(),true);
        }
        else{
            comingMessages.put(message.getIdSender(),content.getCube());
            receiveFlag.put(message.getIdSender(),true);
        }
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

    @Override
    protected boolean evaluateFindOptimalCondition() {
        if (!decisionFlag && evaluateHandleCondition()){
            decisionFlag = true;
            return true;
        }
        return false;
    }


}
