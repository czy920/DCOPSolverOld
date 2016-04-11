package com.cqu.maxsum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class MaxSumFunctionNode extends MaxSumNode {

	protected LargerHyperCube localFunction;
	protected int selfDomainSize;
	
	public MaxSumFunctionNode(int parentAgent,LargerHyperCube localFunction,int domainSize) {
		super(parentAgent);
		// TODO Auto-generated constructor stub
		this.localFunction=localFunction;
		this.selfDomainSize=domainSize;		
		comingMessages = new HashMap<Integer, LargerHyperCube>();
		addNeighbour(constructSelfNodeInfo());
	}

	@Override
	protected NodeInfo constructSelfNodeInfo() {
		// TODO Auto-generated method stub
		return new NodeInfo(selfDomainSize, getParentAgentId());
	}

	@Override
	public void addNeighbour(NodeInfo neighbourInfo) {
		// TODO Auto-generated method stub		
		LargerHyperCube hyperCube = LargerHyperCube.createSimpleLargerUtil(neighbourInfo.getAgentId(), neighbourInfo.getDomainSize(), 0);
		comingMessages.put(neighbourInfo.getAgentId(), hyperCube);		
	}
	
	public void addMessage(Message message){
		comingMessages.put(message.getIdSender(), ((MessageContent)message.getValue()).getLargerHyperCube());
	}
	
	@Override
	public Message[] handleMessage(Message message) {
		// TODO Auto-generated method stub		
		List<Integer> destNodes=new ArrayList<Integer>(comingMessages.keySet());		
		Message[] sendMessages=new Message[destNodes.size()];
		int messageIndex=0;
		for(int destId:destNodes){
			for(int componentId:comingMessages.keySet()){
				if (componentId==destId) {
					continue;
				}
				localFunction.join(comingMessages.get(componentId));
			}
			sendMessages[messageIndex++]=new Message(getParentAgentId(), 
					destId, 
					AbstractNode.MSG_TYPE_TO_VARIABLE_NODE, 
					new MessageContent(localFunction.resovle(destId, LargerHyperCube.HYPER_CUBE_OPERATE_MAX_SUM, null, null)));
		}
		return sendMessages;
	}
}
