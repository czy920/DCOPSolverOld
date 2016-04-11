package com.cqu.maxsum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

public class MaxSumAgent extends AgentCycle {
	private static final int PROCESS_TYPE_VARIABLE = 0;
	private static final int PROCESS_TYPE_FUNCTION  = 1;
	
	private MaxSumFunctionNode functionNode;
	private MaxSumVariableNode variableNode;
	private int cycleEnd;
	private Map<Integer, Integer> lastKnownValueIndex;
	private boolean isStartMessage = false;
	private int processType;

	public MaxSumAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		variableNode = new MaxSumVariableNode(id, domain.length);
		cycleEnd = Settings.settings.getCycleCountEnd();
		lastKnownValueIndex = new HashMap<Integer, Integer>();
	}

	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		double totalCost=0;
		for (Map<String, Object> map : results) {
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("id:");
			stringBuffer.append((Integer)map.get("id"));
			stringBuffer.append(" optimalVal:");
			stringBuffer.append((Integer)map.get("val"));  
			System.out.println(stringBuffer.toString());
			totalCost+=((double)((Integer)map.get("localCost")))/2;
		}
		ResultCycle retResult=new ResultCycle();
		retResult.totalCost=(int)totalCost;
		return retResult;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		//printMessage(msg);
		if (msg.getIdSender()!=id && msg.getValue() != null) {
			int i = ((MessageContent)msg.getValue()).getCurrentValueIndex();
			if (i != -1) {				
				lastKnownValueIndex.put(msg.getIdSender(),i);
			}			
		}
		int msgType = msg.getType();
		if ((msgType & AbstractNode.MSG_TYPE_TO_FUNCTION_NODE) != 0) {
			functionNode.addMessage(msg);			
		}
		else if ((msgType & AbstractNode.MSG_TYPE_TO_VARIABLE_NODE) != 0) {
			variableNode.addMessage(msg);			
		}				
		if ((msgType & AbstractNode.MSG_TYPE_START) != 0) {
			isStartMessage = true;
		}
		else {
			isStartMessage = false;
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setNeibours(int[] neighbours, int parent, int[] children,
			int[] allParents, int[] allChildren,
			Map<Integer, int[]> neighbourDomains,
			Map<Integer, int[][]> constraintCosts,
			Map<Integer, Integer> neighbourLevels) {
		// TODO Auto-generated method stub
		super.setNeibours(neighbours, parent, children, allParents, allChildren,
				neighbourDomains, constraintCosts, neighbourLevels);
		int[] ids = new int[neighbours.length + 1];
		for (int i = 0; i < neighbours.length; i++)
			ids[i] = neighbours[i];
		ids[neighbours.length] = id;
		LargerHyperCube localFunction = new LargerHyperCube(ids, id, constraintCosts);
		localFunction.setDomainSize(domain.length);
		functionNode = new MaxSumFunctionNode(id, localFunction, domain.length);
		for (int id : neighbours){
			functionNode.addNeighbour(new NodeInfo(neighbourDomains.get(id).length, id));
			variableNode.addNeighbour(new NodeInfo(neighbourDomains.get(id).length, id));
		}
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		broadcastMessages(variableNode.init());
	}
	
	private void broadcastMessages(Message[] message) {
		for (Message msg : message)
			sendMessage(msg);
	}
	
	@Override
	protected void allMessageDisposed() {
		// TODO Auto-generated method stub
		super.allMessageDisposed();
		if (isStartMessage) {
			broadcastMessages(functionNode.handleMessage(null));
			processType = PROCESS_TYPE_VARIABLE;
			//System.out.println(id+" start round");
			return;
		}
		if (processType == PROCESS_TYPE_VARIABLE) {
			broadcastMessages(variableNode.handleMessage(null));
			processType = PROCESS_TYPE_FUNCTION;
		}
		else {
			broadcastMessages(functionNode.handleMessage(null));
			processType = PROCESS_TYPE_VARIABLE;
		}
		if (--cycleEnd <= 0) {
			stopRunning();
		}
		//System.out.println(id+" iteration decrease:"+cycleEnd);
	}
	
	private int calcuLocalCost(){
		int cost=0;
		for(int key:lastKnownValueIndex.keySet()){
			if (id<key) {
				cost+=constraintCosts.get(key)[variableNode.getOptimalIndex()][lastKnownValueIndex.get(key)];
			}
			else {
				cost+=constraintCosts.get(key)[lastKnownValueIndex.get(key)][variableNode.getOptimalIndex()];
			}
		}
		return cost;
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		valueIndex=variableNode.getOptimalIndex();
		Map<String, Object> resultMap=new HashMap<String, Object>();
		resultMap.put("id", id);
		resultMap.put("val", domain[valueIndex]);
		resultMap.put("localCost", calcuLocalCost());
		msgMailer.setResult(resultMap);
	}
	
private void printMessage(Message message){
		
		MessageContent content=null;
		if (message.getValue()!=null&&message.getValue() instanceof MessageContent) {
			content=(MessageContent)message.getValue();
		}		
		StringBuffer stringBuffer=new StringBuffer();
		stringBuffer.append("from:");
		stringBuffer.append(message.getIdSender());
		stringBuffer.append(" to:");
		stringBuffer.append(message.getIdReceiver());
		stringBuffer.append(" type:");
		if ((message.getType()&AbstractNode.MSG_TYPE_TO_FUNCTION_NODE)!=0) {
			stringBuffer.append("v2f");
		}
		else if((message.getType()&AbstractNode.MSG_TYPE_TO_VARIABLE_NODE)!=0){
			stringBuffer.append("f2v");
		}
		if ((message.getType()&MaxSumADVPNode.MSG_TYPE_CHANGE_ACK)!=0) {
			stringBuffer.append(",ack");
		}
		else if ((message.getType()&AbstractNode.MSG_TYPE_START)!=0) {
			stringBuffer.append(",start");
		}
		else if((message.getType()&MaxSumADVPNode.MSG_TYPE_CHANGE_REQ)!=0) {
			stringBuffer.append(",req");
		}		
		if (content==null) {
			System.out.println(stringBuffer.toString());
			return;
		}
		stringBuffer.append(" currentIndex:");
		stringBuffer.append(content.getCurrentValueIndex());
		if (content.getLargerHyperCube() != null) {
			stringBuffer.append(" hypercube:" + content.getLargerHyperCube().toString());
		}		
		System.out.println(stringBuffer.toString());
	}

}