package com.cqu.cyclequeue;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cqu.core.Message;
import com.cqu.core.ProcessThread;

public abstract class AgentCycleQueueMessager extends ProcessThread{
	
    protected LinkedList<Message> msgQueue;
    private AtomicBoolean cycleBegin;
    private AtomicBoolean cycleEnd;
    private AtomicInteger cycleEndCount;
    private AtomicInteger totalAgentCount;
    protected AtomicInteger OperateEndCount;
    private AtomicInteger totalAgentCountTemp;
	
	public AgentCycleQueueMessager(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
	}
	
	public void setLocks(AtomicBoolean cycleBegin, AtomicBoolean cycleEnd, AtomicInteger cycleEndCount, AtomicInteger totalAgentCount, AtomicInteger totalAgentCountTemp, AtomicInteger OperateEndCount)
	{
		this.cycleBegin=cycleBegin;
		this.cycleEnd=cycleEnd;
		this.cycleEndCount=cycleEndCount;
		this.totalAgentCount=totalAgentCount;
		this.totalAgentCountTemp=totalAgentCountTemp;
		this.OperateEndCount=OperateEndCount;
	}
	
	/**
	 * 添加消息至缓冲队列，若已满，则丢弃消息；
	 * 但是terminate消息不会被丢弃，而是丢弃队列尾的一个非terminate消息并把它加上;
	 * 如果队列尾为terminate消息，则前移，空出一个位置加上现在的terminate消息
	 * @param msg
	 */
	public void addMessage(Message msg)
	{
		msgQueue.add(msg);
	}

	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		initRun();
		synchronized (cycleEnd) {
			if(this.cycleEnd.get()==false)
			{
				cycleEndCount.incrementAndGet();
				if(cycleEndCount.get()>=totalAgentCount.get())
				{
					cycleEndCount.set(0);
					this.cycleEnd.set(true);
					this.cycleEnd.notifyAll();
				}
			}
		}
		while(isRunning()==true)
		{
			//wait for mailer to put messages out to all agents
			synchronized (cycleBegin) {
				while(cycleBegin.get()==false)
				{
					try {
						cycleBegin.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Thread.currentThread().interrupt();
						//当检测到中断消息时，认为是结束线程的通知，所以直接跳出循环
						break;
					}
				}
			}
			
			if(cycleBegin.get()==true)
			{
				//添加work()方法，确保如果本轮没有收到message也能执行操作
				work(msgQueue.size());
				localSearchCheck();
								
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
				
				allMessageDisposed();
				
				/*
				if(msgQueue.isEmpty()==false){
					System.out.println("Wrong!!!!!!!!!!!!!!!!!!!!!!!!!!");
					int i = 1;
					i = 1/0;
				}
				if(msgQueue.size() != 0){
					System.out.println("Wrong!!!!!!!!!!!!!!!!!!!!!!!!!!");
					int i = 1;
					i = 1/0;
				}
				*/
				
				boolean lastAgent=true;
				synchronized (cycleEndCount) {
					cycleEndCount.incrementAndGet();
					//System.out.println(Thread.currentThread().getName()+" cycleEndCount: "+cycleEndCount);
					if(cycleEndCount.get()<totalAgentCount.get())
					{
						lastAgent=false;
						try {
							cycleEndCount.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							Thread.currentThread().interrupt();
						}
					}
					else if(cycleEndCount.get()>=this.totalAgentCount.get())
					{
						lastAgent=true;
						cycleEndCount.set(0);
						this.cycleEndCount.notifyAll();
						if(this.cycleBegin.get()==true)
						{
							this.cycleBegin.set(false);
						}
					}
				}

				if(lastAgent==false){
					if(isRunning()==false){
						
					}else{
						synchronized(OperateEndCount){
							this.OperateEndCount.incrementAndGet();
							if(OperateEndCount.intValue()==this.totalAgentCount.intValue()){
								OperateEndCount.notifyAll();
							}
						}
					}
				}
				if(lastAgent==true)
				{
					if(isRunning()==false){
						
					}else{
						synchronized(OperateEndCount){
							this.OperateEndCount.incrementAndGet();
							if(OperateEndCount.intValue()==this.totalAgentCount.intValue()){
								OperateEndCount.notifyAll();
							}
						}
					}
					synchronized (cycleEnd) {
						if(cycleEnd.get()==false)
						{
							cycleEnd.set(true);
							cycleEnd.notifyAll();
						}
					}
				}
			}
		}
		
		synchronized(OperateEndCount){
			this.totalAgentCountTemp.decrementAndGet();
			this.OperateEndCount.incrementAndGet();
			if(OperateEndCount.intValue()==this.totalAgentCount.intValue()){
				OperateEndCount.notifyAll();
			}
		}
		
		runFinished();
	}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	/*
	 * 添加work()方法，确保如果本轮没有收到message也能执行操作
	 */
	protected void work(int i){}
	/*
	 * 添加localSearchCheck()方法，用于局部搜索算法中避免错误
	 */
	protected void localSearchCheck(){};
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);
	
	protected void allMessageDisposed(){};
	

}
