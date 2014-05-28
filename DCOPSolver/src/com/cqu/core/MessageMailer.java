package com.cqu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageMailer extends Thread{
	
	private AgentManager agentManager;
	
	private BlockingQueue<Message> msgQueue;
	
	private boolean isRunning=false;
	
	private List<Map<String, Object>> results;
	
	public MessageMailer(AgentManager agentManager) {
		// TODO Auto-generated constructor stub
		this.agentManager=agentManager;
		
		msgQueue=new LinkedBlockingQueue<Message>(100);
		results=new ArrayList<Map<String, Object>>();
	}
	
	public void stopRunning()
	{
		isRunning=false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		isRunning=true;
		
		this.agentManager.startAgents(this);
		
		while(isRunning==true)
		{
			Message msg;
			try {
				msg = msgQueue.take();
				agentManager.getAgent(msg.getIdReceiver()).addMessage(msg);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		this.agentManager.printResults(results);
	}
	
	public void addMessage(Message msg)
	{
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
