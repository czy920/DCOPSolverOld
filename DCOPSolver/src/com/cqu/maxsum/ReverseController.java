package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.List;

/**
 * Created by YanChenDeng on 2016/6/7.
 */
public class ReverseController {
    private int iteration;
    private List<Integer> successorList;
    private List<Integer> precursorList;
    private int REQreceiveCount;
    private int ACKreceiveCount;
    private int id;
    private boolean REQsend;
    private boolean ACKsend;
    private int invokeCount;
    private int nodeType;

    public ReverseController(int iteration, List<Integer> successorList, List<Integer> precursorList, int id, int nodeType) {
        this.iteration = iteration;
        this.successorList = successorList;
        this.precursorList = precursorList;
        this.id = id;
        REQsend = false;
        ACKsend = false;
        this.nodeType = nodeType;
    }

    public void update(List<Integer> precursorList,List<Integer> successorList){
        this.precursorList = precursorList;
        this.successorList = successorList;
        ACKreceiveCount = 0;
        REQreceiveCount = 0;
        REQsend = false;
        ACKsend = false;
        invokeCount = 0;
    }
    public void addMessage(Message msg){
        if ((msg.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_ACK) != 0){
            ACKreceiveCount++;
        }
        else if ((msg.getType() & AbstractNode.MSG_TYPE_AD_REVERSE_REQ) != 0){
            REQreceiveCount++;
        }
    }
    public Message[] handle(){
        Message[] messages = null;
        if (canSendREQ() && !REQsend && precursorList.size() != 0){
            REQsend = true;
            messages = new Message[precursorList.size()];
            for (int i = 0; i < precursorList.size(); i++){
                messages[i] = new Message(this.id,precursorList.get(i),AbstractNode.MSG_TYPE_AD_REVERSE_REQ | nodeType,new MessageContent());
            }
        }
        else if (canSendACK() && !ACKsend){
            ACKsend = true;
            messages = new Message[successorList.size()];
            for (int i = 0; i < successorList.size(); i++){
                messages[i] = new Message(this.id,successorList.get(i),AbstractNode.MSG_TYPE_AD_REVERSE_ACK | nodeType,new MessageContent());
            }
        }
        return messages;
    }

    public boolean canSendREQ(){
        return REQreceiveCount == successorList.size();
    }

    public boolean canSendACK(){
        if (precursorList.size() == 0)
            return canSendREQ();
        return ACKreceiveCount == precursorList.size();
    }

    public boolean canReverse(){
        if (invokeCount == 0) {
            if (canSendACK()){
                invokeCount++;
                iteration--;
                return true;
            }
            return false;
        }
        else
            return false;
    }

    public boolean canTerminate(){
        return iteration <= 0;
    }

}
