package com.cqu.maxsum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class MaxSumVariableNode extends MaxSumNode {

	private int domainSize;
	private int optimalUtility=Integer.MIN_VALUE;
	private int optimalIndex= -1;
	
	public MaxSumVariableNode(int parentAgent,int domainSize) {
		super(parentAgent);
		// TODO Auto-generated constructor stub
		this.domainSize=domainSize;
		comingMessages = new HashMap<Integer, LargerHyperCube>();
		addNeighbour(constructSelfNodeInfo());
	}

	@Override
	protected NodeInfo constructSelfNodeInfo() {
		// TODO Auto-generated method stub
		return new NodeInfo(domainSize, getParentAgentId());
	}

	@Override
	public void addNeighbour(NodeInfo neighbourInfo) {
		// TODO Auto-generated method stub		
		LargerHyperCube hyperCube = LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
		comingMessages.put(neighbourInfo.getAgentId(), hyperCube);
	}

	public void addMessage(Message message){
		comingMessages.put(message.getIdSender(), ((MessageContent)message.getValue()).getLargerHyperCube());
	}	
	
	@Override
	public Message[] handleMessage(Message message) {
		// TODO Auto-generated method stub
		
		//look for optimal value
		LargerHyperCube cube = LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);				
		for(int key:comingMessages.keySet()){
			cube.join(comingMessages.get(key));
		}
		LargerHyperCube optimalCube=cube.resovle(LargerHyperCube.HYPER_CUBE_OPERATE_MAX_SUM);
		for(int i=0;i<domainSize;i++){
			if (optimalCube.indexUtils(i)>optimalUtility) {
				optimalUtility=optimalCube.indexUtils(i);
				optimalIndex=i;
			}
		}
		
		//send messages
		Message[] sendMessages=new Message[comingMessages.keySet().size()];		
		cube = LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
		int messageIndex=0;
		for(int targetId:comingMessages.keySet()){
			for(int componentId:comingMessages.keySet()){
				if (targetId==componentId) {
					continue;
				}
				cube.join(comingMessages.get(componentId));
			}
			MessageContent content = new MessageContent(cubeNormalization(cube.resovle(HyperCube.HYPER_CUBE_OPERATE_MAX_SUM)));
			content.setCurrentValueIndex(optimalIndex);
			sendMessages[messageIndex++]=new Message(getParentAgentId(), 
					targetId, 
					AbstractNode.MSG_TYPE_TO_FUNCTION_NODE, 
					content);
		}
		return sendMessages;
	}
	
	private LargerHyperCube cubeNormalization(LargerHyperCube cube){
		assert cube.getVariableCount()==1:"multiple variable found!!!";
		int[] util=new int[cube.utilLength()];
		int sum=0;
		for(int i=0;i<util.length;i++){
			sum+=cube.indexUtils(i);
		}
		int regularizer=sum/util.length;
		for(int i=0;i<util.length;i++){
			util[i]=cube.indexUtils(i)-regularizer;
		}
		return LargerHyperCube.createSimpleLargerUtil(cube.getMainVariable(), util);
	}
	
	public int getOptimalIndex() {
		return optimalIndex;
	}
	
	public int getOptimalUtility() {
		return optimalUtility;
	}
	
	public Message[] init(){
		Message[] sendMessages=new Message[comingMessages.keySet().size()];
		int messageIndex=0;
		for(int targetId:comingMessages.keySet()){
			LargerHyperCube cube=LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
			sendMessages[messageIndex++]=new Message(getParentAgentId(), 
					targetId, 
					AbstractNode.MSG_TYPE_TO_FUNCTION_NODE|AbstractNode.MSG_TYPE_START, 
					new MessageContent(cube));			
		}
		return sendMessages;
	}
}
