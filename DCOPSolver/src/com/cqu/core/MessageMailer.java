package com.cqu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageMailer extends QueueMessager{
	
	public final static int QUEUE_CAPACITY=100;
	
	private AgentManager agentManager;
	
	private List<Map<String, Object>> results;
	
	public MessageMailer(AgentManager agentManager) {
		// TODO Auto-generated constructor stub
		super("Mailer", QUEUE_CAPACITY);
		this.agentManager=agentManager;
		results=new ArrayList<Map<String, Object>>();
	}
	
	public void setResult(Map<String, Object> result)
	{
		synchronized (result) {
			results.add(result);
			if(results.size()==this.agentManager.getAgentCount())
			{
				this.stopRunning();
			}
		}
	}
	
	public List<Map<String, Object>> getResults()
	{
		return this.results;
	}
	
	public String easyMessageContent(Message msg)
	{
		return this.agentManager.easyMessageContent(msg);
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		agentManager.getAgent(msg.getIdReceiver()).addMessage(msg);
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		System.out.println(Thread.currentThread().getName()+": message lost "+this.easyMessageContent(msg));
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		this.agentManager.printResults(results);
		
		System.out.println("Mailer stopped!");
	}
}
