package com.cqu.maxsum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;
import com.cqu.core.Result;
import com.cqu.maxsum.MaxSumADVPNode.OnDirectionChanged;
import com.cqu.settings.Settings;

public class MaxSumADVPAgent extends Agent implements OnDirectionChanged{

	private LargerHyperCube localFunction;
	private MaxSumADVPVariableNode variableNode;
	private MaxSumADVPFunctionNode functionNode;
	private boolean variableBeforeFunction=true;	
	private Map<Integer, Integer> lastKnownValueIndex;
	private static Object object=new Object();
	private int iteration;
	
	public MaxSumADVPAgent(int id, String name, int level, int[] domain)  {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		variableNode=new MaxSumADVPVariableNode(this.id, variableBeforeFunction, this.domain.length,this);
		lastKnownValueIndex=new HashMap<Integer, Integer>();
		this.iteration = Settings.settings.getCycleCountEnd();
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
		Result retResult=new Result();
		retResult.totalCost=(int)totalCost;		
		return  retResult;
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
//		synchronized (object) {
//			printMessage(msg);
//		}		

		
		int msgType=msg.getType();
		Message[] sendMessages=null;
		if (msg.getValue()!=null) {
			int senderValueIndex=((MessageContent)msg.getValue()).getCurrentValueIndex();
			if (senderValueIndex!=-1&&msg.getIdSender()!=id) {
				lastKnownValueIndex.put(msg.getIdSender(), senderValueIndex);
				//System.out.println(id+"receive:"+msg.getIdSender()+":"+senderValueIndex);
			}
		}		
		if ((msgType&AbstractNode.MSG_TYPE_TO_FUNCTION_NODE)!=0) {
			sendMessages=functionNode.handleMessage(msg);
		}
		else if ((msgType&AbstractNode.MSG_TYPE_TO_VARIABLE_NODE)!=0) {
			sendMessages=variableNode.handleMessage(msg);
		}
		if (sendMessages!=null) {
			for(Message message:sendMessages){
				sendMessage(message);
			}
			
			
//			if (functionNodeStopped&&variableNodeStopped) {
//				functionNode.changeDirection();
//				variableNode.changeDirection();
//				System.out.println(name+" changed,iter:"+iterations);
//				iterations--;
//				variableNodeStopped=false;
//				functionNodeStopped=false;
//				initSystem();
//				if (iterations<200) {
//					//variableNode.setUsingVP(true);
//				}
//			}
		}
		sendMessages=null;
		if (functionNode.checkTermination()&&functionNode.isLeafNode()) {				
			sendMessages=functionNode.generateReqMessages();
		}
		else if (variableNode.checkTermination()&&variableNode.isLeafNode()) {				
			sendMessages=variableNode.generateReqMessages();
			//System.out.println(name+":"+variableNode.getOptimalIndex());
		}
		if (sendMessages!=null) {	
			for(Message message:sendMessages){
				sendMessage(message);
			}		
		}
		if (functionNode.getIteration()<=0&&variableNode.getIteration()<=0) {
			//System.out.println(name+" stopped");
			this.stopRunning();			
			
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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
		localFunction = new LargerHyperCube(ids, id, constraintCosts);
		localFunction.setDomainSize(domain.length);
		functionNode = new MaxSumADVPFunctionNode(this.id, variableBeforeFunction, localFunction, domain.length, this); 
		variableNode.setIteration(iteration);
		functionNode.setIteration(iteration);		
		for(int id:neighbours)
		{
			functionNode.addNeighbour(new NodeInfo(neighbourDomains.get(id).length, id,id<this.id));
			variableNode.addNeighbour(new NodeInfo(neighbourDomains.get(id).length, id,id<this.id));
		}
	}
		
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();		
		initSystem();
	}
	private void initSystem(){
		Message fakeMessage=new Message(id, id, AbstractNode.MSG_TYPE_START, null);
		
		Message[] sendMessage;
		if (variableNode.isRootNode()) {
			//variableNode.printFollow();
			//System.out.println(name+"'s variable node is root");
			sendMessage=variableNode.handleMessage(fakeMessage);
			if (sendMessage!=null) {
				for(Message msg:sendMessage){
					sendMessage(msg);
				}
			}
		}	
		if (functionNode.isRootNode()) {
			//System.out.println(name+"'s function node is root");
			sendMessage=functionNode.handleMessage(fakeMessage);
			if (sendMessage!=null) {
				for(Message msg:sendMessage){
					sendMessage(msg);
				}
			}
		}		
	}
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		valueIndex=variableNode.getOptimalIndex();
		Map<String, Object> resultMap=new HashMap<String, Object>();
		resultMap.put("id", id);
		resultMap.put("val", valueIndex + 1);
		resultMap.put("localCost", calcuLocalCost());
		msgMailer.setResult(resultMap);
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
	public String toString() {
		// TODO Auto-generated method stub
		return name;
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

	@Override
	public void directionChanged(MaxSumADVPNode node) {
		// TODO Auto-generated method stub
		Message fakeMessage=new Message(id, id, AbstractNode.MSG_TYPE_START, null);
		Message[] sendMessage;
		if (node.isRootNode()) {
			//System.out.println(node.getParentAgent().getId()+"'s "+node.tag+" is root");
			sendMessage=node.handleMessage(fakeMessage);
			if (sendMessage!=null) {
				for(Message msg:sendMessage){
					sendMessage(msg);
				}
			}
		}			
	}
	
}
