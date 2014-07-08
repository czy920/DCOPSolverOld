package com.cqu.dpop;

import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class DPOPAgent extends Agent{

	public DPOPAgent(int id, String name, int[] domain) {
		super(id, name, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "";
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
