package com.cqu.maxsum;

import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class MaxSumADVPVariableNode extends MaxSumADVPNode {

	private int domainSize;
	private boolean usingVP=true;
	private int optimalValue;
	private int optimalIndex;
	private static Object object = new Object();
	
	public MaxSumADVPVariableNode(int parentAgentId,boolean variableBeforeFunction,int domainSize,OnDirectionChanged directionChangedListener) {
		super(parentAgentId,variableBeforeFunction,directionChangedListener);
		// TODO Auto-generated constructor stub
		this.domainSize=domainSize;		
		optimalIndex=-1;
		optimalValue=Integer.MIN_VALUE;
		tag=NODE_TYPE_VARIABLE_NODE;
		comingMessages = new HashMap<Integer, LargerHyperCube>();
		addNeighbour(constructSelfNodeInfo());
	}

	@Override
	protected NodeInfo constructSelfNodeInfo() {
		// TODO Auto-generated method stub
		return new NodeInfo(domainSize, getParentAgentId(),!variableBeforeFunction);
	}

	@Override
	public void addNeighbour(NodeInfo neighbourInfo) {
		// TODO Auto-generated method stub
		LargerHyperCube hyperCube=LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
		comingMessages.put(neighbourInfo.getAgentId(), hyperCube);
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
				sendMessages[i]=new Message(msg.getIdSender(),msg.getIdReceiver(), msg.getType()|MSG_TYPE_TO_FUNCTION_NODE,msg.getValue());
			}
			return sendMessages;
		}
		if ((message.getType()&MSG_TYPE_CHANGE_ACK)!=0||(message.getType()&MSG_TYPE_CHANGE_REQ)!=0) {
			return null;
		}
		if ((message.getType()&AbstractNode.MSG_TYPE_START)==0) {
			comingMessages.put(message.getIdSender(), ((MessageContent)message.getValue()).getLargerHyperCube());
			receiveFlag.put(message.getIdSender(), true);
		}
		
		if (!checkTermination()) {
			return null;
		}
		
		LargerHyperCube cube=LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
		for(int key:comingMessages.keySet()){
			cube.join(comingMessages.get(key));
		}
		LargerHyperCube optimalCube=cube.resovle(HyperCube.HYPER_CUBE_OPERATE_MAX_SUM);
		for(int i=0;i<domainSize;i++){
			if (optimalValue<optimalCube.indexUtils(i)) {
				optimalIndex=i;
				optimalValue=optimalCube.indexUtils(i);
			}
		}
//		synchronized (object) {
//			System.out.println("----------"+getParentAgent().getName()+"="+optimalIndex+"-------------");
//			for(int key:comingMessages.keySet()){
//				comingMessages.get(key).printMaxAssignment(optimalIndex);
//			}
//			System.out.println();
//			System.out.println();
//		}
		
		sendMessages=new Message[followNodes.size()];
		cube=LargerHyperCube.createSimpleLargerUtil(getParentAgentId(), domainSize, 0);
		int msgIndex=0;
		for(int targetId:followNodes){		
			for(int id:comingMessages.keySet()){
				if (targetId==id) {
					continue;
				}
				cube.join(comingMessages.get(id));							
			}
			MessageContent content=new MessageContent(cubeNormalization(cube.resovle(HyperCube.HYPER_CUBE_OPERATE_MAX_SUM)));
			if (usingVP) {
				content.setCurrentValueIndex(optimalIndex);
			}
			sendMessages[msgIndex++]=new Message(getParentAgentId(), 
					targetId, 
					AbstractNode.MSG_TYPE_TO_FUNCTION_NODE,
					content
					);
		}
		return sendMessages;
	}
	
	public void setUsingVP(boolean usingVP) {
		this.usingVP = usingVP;
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
	@Override
	public void changeDirection() {
		// TODO Auto-generated method stub
		super.changeDirection();
		//System.out.println(getParentAgent().getId()+"'s variable node changed,iter:"+getIteration());
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
				//((MessageContent)msg.getValue()).setCurrentValueIndex(optimalIndex);
				sendMessages[i]=new Message(msg.getIdSender(),msg.getIdReceiver(), msg.getType()|MSG_TYPE_TO_FUNCTION_NODE,msg.getValue());
			}
			return sendMessages;
		}	
		return null;
	}
	
	public void printFollow(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(getParentAgentId()+"'s follow:");
		if (followNodes != null) {
			for(int id : followNodes)
				stringBuffer.append(id+" ");
		}
		System.out.println(stringBuffer.toString());
	}
}
