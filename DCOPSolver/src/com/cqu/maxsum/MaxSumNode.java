package com.cqu.maxsum;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public abstract class MaxSumNode extends AbstractNode {

	public MaxSumNode(int parentAgentId) {
		super(parentAgentId);
		// TODO Auto-generated constructor stub		
		setIteration(200);
	}	
		
	
	protected abstract NodeInfo constructSelfNodeInfo();

}
