package com.cqu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cqu.test.Debugger;

public class MessageMailer extends QueueMessager{
	
	public final static int QUEUE_CAPACITY=100;
	
	private AgentManager agentManager;
	private List<Map<String, Object>> results;
	
	private long timeStart=0;
	private long timeEnd=0;
	
	public MessageMailer(AgentManager agentManager) {
		// TODO Auto-generated constructor stub
		super("Mailer", QUEUE_CAPACITY);
		this.agentManager=agentManager;
		results=new ArrayList<Map<String, Object>>();
	}
	
	public void setResult(Map<String, Object> result)
	{
		synchronized (results) {
			results.add(result);
			if(results.size()>=this.agentManager.getAgentCount())
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
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost "+this.easyMessageContent(msg));
		}
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		timeStart=System.currentTimeMillis();
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		this.agentManager.printResults(results);
		Debugger.printValueChanges();
		
		timeEnd=System.currentTimeMillis();
		System.out.println("Mailer stopped, totalTime: "+(timeEnd-timeStart)+"ms");
	}
}
