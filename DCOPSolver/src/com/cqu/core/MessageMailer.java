package com.cqu.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageMailer extends Thread{
	
	private AgentManager agentManager;
	
	private BlockingQueue<Message> msgQueue;
	
	private boolean isRunning=false;
	
	public MessageMailer() {
		// TODO Auto-generated constructor stub
		msgQueue=new LinkedBlockingQueue<Message>(100);
	}
	
	public void stopRunning()
	{
		isRunning=false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		isRunning=true;
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
}
