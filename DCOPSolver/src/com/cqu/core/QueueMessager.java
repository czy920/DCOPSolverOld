package com.cqu.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class QueueMessager extends ThreadEx{

	private LinkedList<Message> msgQueue;
	
	private int queueCapacity;
	
	public QueueMessager(String threadName, int queueCapacity) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.queueCapacity=queueCapacity;
		this.msgQueue=new LinkedList<Message>();
	}
	
	/**
	 * 添加消息至缓冲队列，若已满，则丢弃消息；
	 * 但是terminate消息不会被丢弃，而是丢弃队列尾的一个非terminate消息并把它加上;
	 * 如果队列尾为terminate消息，则前移，空出一个位置加上现在的terminate消息
	 * @param msg
	 */
	public void addMessage(Message msg)
	{
		synchronized (msgQueue) {
			//消息不可丢失
			if(queueCapacity<0)
			{
				msgQueue.add(msg);
				msgQueue.notifyAll();
				return;
			}
			//消息可丢失
			if(msgQueue.size()<queueCapacity)
			{
				msgQueue.add(msg);
				msgQueue.notifyAll();
			}else
			{
				if(msg.getType()==Message.TYPE_TERMINATE_MESSAGE)
				{
					List<Message> tempList=new ArrayList<Message>();
					Message temp=msgQueue.removeLast();
					while(temp!=null&&temp.getType()==Message.TYPE_TERMINATE_MESSAGE)
					{
						tempList.add(temp);
						temp=msgQueue.removeLast();
					}
					
					if(temp==null)
					{
						//说明队列中全部是terminate消息，那么必将会有terminate消息被遗漏，整个算法可能会失败
						throw new RuntimeException("TERMINATE消息被遗漏，运行失败，需要加大消息缓冲队列容量！");
					}
					
					for(int i=tempList.size()-1;i>-1;i--)
					{
						msgQueue.add(tempList.get(i));
					}
					msgQueue.add(msg);
					
					messageLost(temp);
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
						//当检测到中断消息时，认为是结束线程的通知，所以直接跳出循环
						break;
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
