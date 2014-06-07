package com.cqu.core;

import java.util.LinkedList;

public abstract class QueueMessager extends ThreadEx{

	private LinkedList<Message> msgQueue;
	
	private int queueCapacity;
	
	public QueueMessager(String threadName, int queueCapacity) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.queueCapacity=queueCapacity;
		this.msgQueue=new LinkedList<Message>();
	}
	
	public void addMessage(Message msg)
	{
		synchronized (msgQueue) {
			if(msgQueue.size()<queueCapacity)
			{
				msgQueue.add(msg);
				msgQueue.notifyAll();
			}else
			{
				//终止消息不能被丢弃：丢弃队列尾的消息，而把它加上
				if(msg.getType()==Message.TYPE_TERMINATE_MESSAGE)
				{
					messageLost(msgQueue.removeLast());
					msgQueue.add(msg);
				}else
				{
					messageLost(msg);
				}
			}
		}
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		initRun();
		while(isRunning==true)
		{
			Message msg=null;
			synchronized (msgQueue) {
				while(msgQueue.isEmpty()==true)
				{
					try {
						msgQueue.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Thread.currentThread().interrupt();
					}
				}
				if(msgQueue.isEmpty()==false)
				{
					msg=msgQueue.removeFirst();
				}
			}
			
			if(msg!=null)
			{
				disposeMessage(msg);
			}
		}
		runFinished();
	}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);

}
