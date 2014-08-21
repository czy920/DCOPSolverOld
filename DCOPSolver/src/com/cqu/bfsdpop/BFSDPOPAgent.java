package com.cqu.bfsdpop;

import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class BFSDPOPAgent extends Agent{
	
	

	public BFSDPOPAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		
	}

}
