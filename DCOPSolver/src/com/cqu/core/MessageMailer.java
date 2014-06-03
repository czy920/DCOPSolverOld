package com.cqu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageMailer extends ThreadEx{
	
	public final static int QUEUE_CAPACITY=100;
	
	private AgentManager agentManager;
	
	private BlockingQueue<Message> msgQueue;
	
	private List<Map<String, Object>> results;
	
	public MessageMailer(AgentManager agentManager) {
		// TODO Auto-generated constructor stub
		super("Mailer");
		this.agentManager=agentManager;
		
		msgQueue=new ArrayBlockingQueue<Message>(QUEUE_CAPACITY, true);
		results=new ArrayList<Map<String, Object>>();
	}
	
	public void addMessage(Message msg)
	{
		try {
			System.out.println(Thread.currentThread().getName()+": before put() in mailer...");//for debug
			msgQueue.put(msg);
			System.out.println(Thread.currentThread().getName()+": Message put into mailer: "+this.easyMessageContent(msg));//for debug
			System.out.println(Thread.currentThread().getName()+": after put() in mailer...");//for debug
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
				System.out.println(Thread.currentThread().getName()+": before take() in mailer...");//for debug
				msg = msgQueue.take();
				System.out.println(Thread.currentThread().getName()+": after take() in mailer...");//for debug
				System.out.println(Thread.currentThread().getName()+": Message taken out in mailer: "+this.easyMessageContent(msg));//for debug
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
