package com.cqu.cyclequeue;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cqu.core.Message;
import com.cqu.core.ThreadEx;

public abstract class AgentCycleQueueMessager extends ThreadEx{
	
    private LinkedList<Message> msgQueue;
    private AtomicBoolean cycleBegin;
    private AtomicInteger cycleBeginCount;
    private AtomicBoolean cycleEnd;
    protected AtomicInteger cycleEndCount;
    private AtomicInteger totalAgentCount;
	
	public AgentCycleQueueMessager(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
	}
	
	public void setLocks(AtomicBoolean cycleBegin, AtomicInteger cycleBeginCount, AtomicBoolean cycleEnd, AtomicInteger cycleEndCount, AtomicInteger totalAgentCount)
	{
		this.cycleBegin=cycleBegin;
		this.cycleBeginCount=cycleBeginCount;
		this.cycleEnd=cycleEnd;
		this.cycleEndCount=cycleEndCount;
		this.totalAgentCount=totalAgentCount;
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
			//wait for mailer to put messages out to all agents
			synchronized (cycleBegin) {
				while(cycleBegin.get()==false)
				{
					try {
						cycleBegin.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Thread.currentThread().interrupt();
						//当检测到中断消息时，认为是结束线程的通知，所以直接跳出循环
						break;
					}
				}
			}
			if(cycleBegin.get()==true)
			{
				synchronized (cycleBeginCount) {
					if(cycleBeginCount.incrementAndGet()>=this.totalAgentCount.get())
					{
						cycleEnd.set(true);
						cycleBeginCount.set(0);
					}
				}
				while(msgQueue.isEmpty()==false)
				{
					Message msg=msgQueue.removeFirst();
					if(msg!=null)
					{
						disposeMessage(msg);
					}
				}
				this.notifyCycleEnd();
				synchronized (cycleEnd) {
					while(cycleEnd.get()==false)
					{
						try {
							cycleEnd.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Thread.currentThread().interrupt();
						}
					}
				}
			}
		}
		runFinished();
	}
	
	private void notifyCycleEnd()
	{
		synchronized (cycleEndCount) {
			if(cycleEndCount.incrementAndGet()>=this.totalAgentCount.get())
			{
				this.cycleEnd.set(true);
				this.cycleEndCount.set(0);
			}
		}
	}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);

}
