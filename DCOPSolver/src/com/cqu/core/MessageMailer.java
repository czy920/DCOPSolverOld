package com.cqu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageMailer extends ThreadEx{
	
	private AgentManager agentManager;
	
	private BlockingQueue<Message> msgQueue;
	
	private List<Map<String, Object>> results;
	
	public MessageMailer(AgentManager agentManager) {
		// TODO Auto-generated constructor stub
		this.agentManager=agentManager;
		
		msgQueue=new LinkedBlockingQueue<Message>(100);
		results=new ArrayList<Map<String, Object>>();
	}
	
	public void addMessage(Message msg)
	{
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}
	
	public synchronized void setResult(Map<String, Object> result)
	{
		results.add(result);
		if(results.size()==this.agentManager.getAgentCount())
		{
			this.stopRunning();
		}
	}
	
	public List<Map<String, Object>> getResults()
	{
		return this.results;
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		while(isRunning==true)
		{
			Message msg;
			try {
				msg = msgQueue.take();
				if(msg!=null)
				{
					agentManager.getAgent(msg.getIdReceiver()).addMessage(msg);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}
		}
		this.agentManager.printResults(results);
		
		System.out.println("Message mailer stopped!");
	}
	
	public String easyMessageContent(Message msg)
	{
		return this.agentManager.easyMessageContent(msg);
	}
}
