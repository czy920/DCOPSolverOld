package com.cqu.cyclequeue;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cqu.core.Message;
import com.cqu.core.ThreadEx;

public abstract class MailerCycleQueueMessager extends ThreadEx{
	
    private LinkedList<Message> msgQueue;
    
    protected AtomicBoolean cycleBegin;
    protected AtomicBoolean cycleEnd;
    protected AtomicInteger cycleEndCount;
    protected AtomicInteger totalAgentCount;
    protected AtomicInteger OperateEndCount;
    protected AtomicInteger totalAgentCountTemp;
    
    protected int cycleCount;
	
	public MailerCycleQueueMessager(String threadName, int totalAgentCount) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
		this.totalAgentCount=new AtomicInteger(totalAgentCount);
		this.totalAgentCountTemp=new AtomicInteger(totalAgentCount);
		this.OperateEndCount=new AtomicInteger(totalAgentCount);   //初始化是假设处理完了的
		this.cycleBegin=new AtomicBoolean(false);
		this.cycleEnd=new AtomicBoolean(false);
		this.cycleEndCount=new AtomicInteger(0);
		
		this.cycleCount=0;
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
			msgQueue.add(msg);
		}
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		initRun();
		while(isRunning==true)
		{
			//wait for all agents notify arrivals and then put all messages to agents
			synchronized (cycleEnd) {
				while(cycleEnd.get()==false)
				{
					try {
						cycleEnd.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Thread.currentThread().interrupt();
						//当检测到中断消息时，认为是结束线程的通知，所以直接跳出循环
						break;
					}
				}
			}
			if(cycleEnd.get()==true)
			{
				while(msgQueue.isEmpty()==false)
				{
					Message msg=null;
					try{
						msg=msgQueue.removeFirst();
					}catch(NoSuchElementException e)
					{
						
					}
					if(msg!=null)
					{
						disposeMessage(msg);
					}
				}

				cycleCount++;
				System.out.println("cycleCount: "+cycleCount);
				cycleEnd.set(false);
				synchronized (cycleBegin) {
					synchronized(OperateEndCount){
						if(OperateEndCount.intValue()<this.totalAgentCount.intValue())
							try {
								OperateEndCount.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
					this.OperateEndCount.set(0);
					this.totalAgentCount.set(this.totalAgentCountTemp.get());
					cycleBegin.set(true);//open entrance
					cycleBegin.notifyAll();
				}
			}
		}
		runFinished();
	}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);

}
