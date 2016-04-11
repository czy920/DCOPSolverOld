package com.cqu.maxsum;

import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class MaxSumADVPFunctionNode extends MaxSumADVPNode{

	private LargerHyperCube localFunction;
	private int domainSize;
	private Map<Integer, Integer> assignMap;
	
	public MaxSumADVPFunctionNode(int parentAgentId,boolean variableBeforeFunction,LargerHyperCube localFunction,int domainSize,OnDirectionChanged directionChangedListener) {
		super(parentAgentId,variableBeforeFunction,directionChangedListener);
		this.localFunction=localFunction;
		this.domainSize=domainSize;
		this.assignMap=new HashMap<Integer, Integer>();
		comingMessages = new HashMap<Integer, LargerHyperCube>();
		addNeighbour(constructSelfNodeInfo());
		tag=NODE_TYPE_FUNCTION_NODE;
		
	}

	@Override
	protected NodeInfo constructSelfNodeInfo() {
		// TODO Auto-generated method stub
		return new NodeInfo(domainSize, getParentAgentId(),variableBeforeFunction);
	}

	@Override
	public void addNeighbour(NodeInfo neighbourInfo) {
		// TODO Auto-generated method stub
		LargerHyperCube cube=LargerHyperCube.createSimpleLargerUtil(neighbourInfo.getAgentId(), neighbourInfo.getDomainSize(), 0);
		comingMessages.put(neighbourInfo.getAgentId(), cube);
		if (neighbourInfo.getPreviousThanCurrent()) {
			previousNodes.add(neighbourInfo.getAgentId());
			receiveFlag.put(neighbourInfo.getAgentId(), false);		
			ackReceiveFlag.put(neighbourInfo.getAgentId(), false);
		}
		else {
			followNodes.add(neighbourInfo.getAgentId());
			reqReceiveFlag.put(neighbourInfo.getAgentId(), false);
		}
	}

	@Override
	public Message[] handleMessage(Message message) {
		// TODO Auto-generated method stub
		Message[] superRetrunedMessages = super.handleMessage(message);
		Message[] sendMessages;
		if (superRetrunedMessages!=null) {
			sendMessages=new Message[superRetrunedMessages.length];
			for(int i=0;i<sendMessages.length;i++){
				Message msg=superRetrunedMessages[i];
				sendMessages[i]=new Message(msg.getIdSender(),msg.getIdReceiver(), msg.getType()|MSG_TYPE_TO_VARIABLE_NODE,msg.getValue());
			}
			return sendMessages;
		}
		if ((message.getType()&MSG_TYPE_CHANGE_ACK)!=0||(message.getType()&MSG_TYPE_CHANGE_REQ)!=0) {
			return null;
		}
		if((message.getType()&AbstractNode.MSG_TYPE_START)==0){
			comingMessages.put(message.getIdSender(), ((MessageContent)message.getValue()).getLargerHyperCube());
			receiveFlag.put(message.getIdSender(), true);
			int assignIndex=((MessageContent)message.getValue()).getCurrentValueIndex();
			if (assignIndex!=-1) {
				//assignMap.put(message.getIdSender(), assignIndex);
			}
		}
		if (!checkTermination()) {
			return null;
		}
		sendMessages=new Message[followNodes.size()];
		int messageIndex=0;
		for (int targetId:followNodes){
			for (int componentId:comingMessages.keySet()){
				if (targetId==componentId) {
					continue;
				}
				localFunction.join(comingMessages.get(componentId));
			}
			sendMessages[messageIndex++]=new Message(getParentAgentId(), 
					targetId, 
					AbstractNode.MSG_TYPE_TO_VARIABLE_NODE, 
 					new MessageContent(localFunction.resovle(targetId, HyperCube.HYPER_CUBE_OPERATE_MAX_SUM, assignMap,null)));
		}
		return sendMessages;
	}
	
	@Override
	public void changeDirection() {
		// TODO Auto-generated method stub
		super.changeDirection();
		assignMap.clear();
		//System.out.println(getParentAgent().getId()+"'s function node changed,iter:"+getIteration());
	}
	
	@Override
	public Message[] generateReqMessages() {
		// TODO Auto-generated method stub
		Message[] superRetrunedMessages = super.generateReqMessages();
		Message[] sendMessages;
		if (superRetrunedMessages!=null) {
			sendMessages=new Message[superRetrunedMessages.length];
			for(int i=0;i<sendMessages.length;i++){
				Message msg=superRetrunedMessages[i];
				sendMessages[i]=new Message(msg.getIdSender(),msg.getIdReceiver(), msg.getType()|MSG_TYPE_TO_VARIABLE_NODE,null);
			}
			return sendMessages;
		}	
		return null;
	}
}
