package com.cqu.maxsum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

public class MaxSumStructureRefineAgent extends AgentCycle {
	
	private static Random random = new Random();
	private static final int MSG_TYPE_FACTOR_GRAPH = 8;
	private static final int PROCESS_TYPE_VARIABLE = 0;
	private static final int PROCESS_TYPE_FUNCTION  = 1;
	
	private MaxSumFunctionNode functionNode;
	private MaxSumVariableNode variableNode;
	private int cycleEnd;
	private Map<Integer, Integer> lastKnownValueIndex;
	private boolean isStartMessage = false;
	private boolean isFactorGraphMessage = false;
	private List<Integer> controlledNeighbours;
	private int degree;
	private int randomVal;
	private int processType;
	
	public MaxSumStructureRefineAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		variableNode = new MaxSumVariableNode(id, domain.length);
		cycleEnd = Settings.settings.getCycleCountEnd();
		lastKnownValueIndex = new HashMap<Integer, Integer>();
		controlledNeighbours = new ArrayList<Integer>();
		randomVal = random.nextInt();
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
			//System.out.println(stringBuffer.toString());
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
		if ((msgType & MSG_TYPE_FACTOR_GRAPH) != 0) {
			isFactorGraphMessage = true;
			MessageContent content = (MessageContent)msg.getValue();
			if (content.getDegree() > degree || (content.getDegree() == degree && content.getRandom() > randomVal)) {
				controlledNeighbours.remove((Object)msg.getIdSender());
			}
		}
		else {
			isFactorGraphMessage = false;
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
		for(int id : neighbours)
			controlledNeighbours.add(id);
		degree = neighbours.length;
		
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		//broadcastMessages(variableNode.init());
	
		for (int id : neighbours){
			sendMessage(new Message(this.id, id, MSG_TYPE_FACTOR_GRAPH, new MessageContent(degree, randomVal)));
		}
	}
	
	private void broadcastMessages(Message[] message) {
		for (Message msg : message)
			sendMessage(msg);
	}
	
	@Override
	protected void allMessageDisposed() {
		// TODO Auto-generated method stub
		super.allMessageDisposed();
		if (isFactorGraphMessage) {
			if (controlledNeighbours.size() != 0) {
				int[] ids = new int[controlledNeighbours.size() + 1];
				for (int i = 0; i < ids.length - 1; i++)
					ids[i] = controlledNeighbours.get(i);
				ids[controlledNeighbours.size()] = id;
				Map<Integer, int[][]> tmpCosts = new HashMap<Integer, int[][]>();
				for (int id : neighbours) {
					if (controlledNeighbours.contains(id)) {
						tmpCosts.put(id, constraintCosts.get(id));
					}
				}
				LargerHyperCube localFunction = new LargerHyperCube(ids, id,
						tmpCosts);
				localFunction.setDomainSize(domain.length);
				functionNode = new MaxSumFunctionNode(id, localFunction,
						domain.length);
				for (int id : controlledNeighbours) {
					functionNode.addNeighbour(new NodeInfo(neighbourDomains
							.get(id).length, id));
				}
			}
			else {
				variableNode.removeNeighbour(this.id);
			}
			
			for (int id : neighbours){
				if (!controlledNeighbours.contains(id)) {
					variableNode.addNeighbour(new NodeInfo(neighbourDomains.get(id).length, id));
				}
			}
			broadcastMessages(variableNode.init());
			return;
		}
		if (isStartMessage) {
			if (functionNode != null) {
				broadcastMessages(functionNode.handleMessage(null));
				processType = PROCESS_TYPE_VARIABLE;
			}			
			//System.out.println(id+" start round");
			return;
		}
		if (processType == PROCESS_TYPE_FUNCTION)
			if(functionNode != null)
				broadcastMessages(functionNode.handleMessage(null));
		if (processType == PROCESS_TYPE_VARIABLE)
			broadcastMessages(variableNode.handleMessage(null));
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
			stringBuffer.append(" hypercube:[");
			for(int i=0;i<content.getLargerHyperCube().utilLength();i++){
				stringBuffer.append(content.getLargerHyperCube().indexUtils(i)+" ");
			}
			stringBuffer.append("]");
		}		
		System.out.println(stringBuffer.toString());
	}

}