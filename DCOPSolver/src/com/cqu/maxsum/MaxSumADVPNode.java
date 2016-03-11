package com.cqu.maxsum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public abstract class MaxSumADVPNode extends MaxSumNode {

	public static final int MSG_TYPE_CHANGE_REQ=8;
	public static final int MSG_TYPE_CHANGE_ACK=16;
	public static final String NODE_TYPE_FUNCTION_NODE="function node";
	public static final String NODE_TYPE_VARIABLE_NODE="variable node";
	
	protected List<Integer> previousNodes;
	protected List<Integer> followNodes;
	protected Map<Integer, Boolean> receiveFlag;
	protected Map<Integer, Boolean> reqReceiveFlag;
	protected Map<Integer, Boolean> ackReceiveFlag;
	protected boolean variableBeforeFunction;
	private boolean sendReq=false;
	private OnDirectionChanged directionChangedListener;
	protected String tag;
	
	public MaxSumADVPNode(int parentAgentId,boolean variableBeforeFunction,OnDirectionChanged directionChangedListener) {
		super(parentAgentId);
		// TODO Auto-generated constructor stub
		previousNodes=new ArrayList<Integer>();
		followNodes=new ArrayList<Integer>();
		receiveFlag=new HashMap<Integer, Boolean>();
		this.variableBeforeFunction=variableBeforeFunction;
		reqReceiveFlag=new HashMap<Integer, Boolean>();
		ackReceiveFlag=new HashMap<Integer, Boolean>();
		this.directionChangedListener=directionChangedListener;
		setIteration(20);
	}
	
	@Override
	public boolean checkTermination() {
		// TODO Auto-generated method stub
		for(int key:receiveFlag.keySet()){
			if (!receiveFlag.get(key)) {
				return false;
			}
		}
		return true;
	}
	
	public void changeDirection() {
		decreaseIteration();
		List<Integer> tmp=previousNodes;
		previousNodes=followNodes;
		followNodes=tmp;
		receiveFlag.clear();
		ackReceiveFlag.clear();
		reqReceiveFlag.clear();
		sendReq=false;
		for(int id:previousNodes){
			receiveFlag.put(id, false);
			ackReceiveFlag.put(id, false);
		}
		for(int id:followNodes){
			reqReceiveFlag.put(id, false);
		}		
		directionChangedListener.directionChanged(this);
	}
	
	public boolean isRootNode(){
		return previousNodes.size()==0;
	}
	public boolean isLeafNode(){
		return followNodes.size()==0;
	}
	
	protected Message[] handleReqMessage(Message message){
		reqReceiveFlag.put(message.getIdSender(), true);
		if (!checkAllReqReceived()) {
			return null;
		}
		if (isRootNode()) {			
			Message[] msgs=generateChangeMessages(followNodes, MSG_TYPE_CHANGE_ACK);
			changeDirection();
			return msgs;
		}
		return generateChangeMessages(previousNodes, MSG_TYPE_CHANGE_REQ);
	}
	
	protected Message[] handleAckMessage(Message message){
		ackReceiveFlag.put(message.getIdSender(), true);
		if (!checkAllAckReceived()) {
			return null;
		}		
		Message[] msgs=null;
		if (!isLeafNode()) {
			msgs=generateChangeMessages(followNodes, MSG_TYPE_CHANGE_ACK);
		}
		changeDirection();
		return msgs;
	}	
	
	private boolean checkAllReqReceived(){
		for(int id:reqReceiveFlag.keySet()){
			if (!reqReceiveFlag.get(id)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean checkAllAckReceived(){
		for(int id:ackReceiveFlag.keySet()){
			if (!ackReceiveFlag.get(id)) {
				return false;
			}
		}
		return true;
	}
	
	private Message[] generateChangeMessages(List<Integer> target,int type)
	{
		Message[] sendMessages=new Message[target.size()];
		for(int i=0;i<sendMessages.length;i++){
			sendMessages[i]=new Message(getParentAgentId(), target.get(i), type, null);
		}
		return sendMessages;
	}
	
	@Override
	public Message[] handleMessage(Message message) {
		// TODO Auto-generated method stub
		int msgType=message.getType();
		if ((msgType&MSG_TYPE_CHANGE_ACK)!=0) {
			return handleAckMessage(message);
		}
		else if ((msgType&MSG_TYPE_CHANGE_REQ)!=0) {
			return handleReqMessage(message);
		}
		return null;
	}
	
	public Message[] generateReqMessages(){
		if (!sendReq) {
			sendReq=true;
			return generateChangeMessages(previousNodes, MSG_TYPE_CHANGE_REQ);
		}
		return null;
	}
	interface OnDirectionChanged{
		void directionChanged(MaxSumADVPNode node);
	}
}
