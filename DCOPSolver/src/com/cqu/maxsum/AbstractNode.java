package com.cqu.maxsum;

import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public abstract class AbstractNode {
	
	public static final int MSG_TYPE_TO_FUNCTION_NODE=1;
	public static final int MSG_TYPE_TO_VARIABLE_NODE=2;
	public static final int MSG_TYPE_START=4;
	
	private int parentAgentId;
	private int iteration;
	Map<Integer, LargerHyperCube> comingMessages;
	
	public AbstractNode(int parentAgentId) {
		// TODO Auto-generated constructor stub
		this.parentAgentId = parentAgentId;
		comingMessages=new HashMap<Integer, LargerHyperCube>();
	}
	
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}
	
	public boolean checkTermination(){
		return iteration<=0;
	}
	
	public int getParentAgentId() {
		return parentAgentId;
	}
	
	protected void decreaseIteration(){
		iteration--;
	}
	
	public int getIteration() {
		return iteration;
	}
	
	public abstract void addNeighbour(NodeInfo neighbourInfo);
	public abstract Message[] handleMessage(Message message);
	
}
