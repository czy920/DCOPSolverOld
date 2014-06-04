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
		System.out.println(Thread.currentThread().getName()+": before add lock");
		synchronized (msgQueue) {
			System.out.println(Thread.currentThread().getName()+": in add lock");
			if(msgQueue.size()<queueCapacity)
			{
				msgQueue.add(msg);
				msgQueue.notifyAll();
			}else
			{
				messageLost(msg);
			}
		}
		System.out.println(Thread.currentThread().getName()+": after add lock");
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		initRun();
		while(isRunning==true)
		{
			Message msg=null;
			System.out.println(Thread.currentThread().getName()+": before get lock");
			synchronized (msgQueue) {
				System.out.println(Thread.currentThread().getName()+": in get lock");
				while(msgQueue.isEmpty()==true)
				{
					try {
						System.out.println(Thread.currentThread().getName()+": before wait");
						msgQueue.wait();
						System.out.println(Thread.currentThread().getName()+": after wait");
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
			System.out.println(Thread.currentThread().getName()+": after get lock");
			
			if(msg!=null)
			{
				disposeMessage(msg);
				System.out.println(Thread.currentThread().getName()+": after dispose message");
			}
		}
		runFinished();
	}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);

}
