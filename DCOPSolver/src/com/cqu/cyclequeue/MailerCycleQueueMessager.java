package com.cqu.cyclequeue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cqu.aco.PublicConstants;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ProcessThread;

public abstract class MailerCycleQueueMessager extends ProcessThread{
	
    private LinkedList<Message> msgQueue;
    
    protected AtomicBoolean cycleBegin;     //用于通知Agent开始
    protected AtomicBoolean cycleEnd;       //用于Agent通知消息中心开始
    protected AtomicInteger cycleEndCount;     //用于每轮Agent处理完信息标志
    protected AtomicInteger totalAgentCount;   //用于每轮开始总共运行的Agent
    protected AtomicInteger OperateEndCount;    //用于抑制消息中心太快更新totalAgentCount＝totalAgentCountTemp，而totalAgentCountTemp还未更新
    protected AtomicInteger totalAgentCountTemp;   //用于每轮运行的Agent总数更新
    
    protected int cycleCount;
    
	public MailerCycleQueueMessager(String threadName, int totalAgentCount) {
		super(threadName);
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
		this.totalAgentCount=new AtomicInteger(totalAgentCount);
		this.totalAgentCountTemp=new AtomicInteger(totalAgentCount);
		this.OperateEndCount=new AtomicInteger(totalAgentCount);   //初始化是假设处理完了时的
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
		while(isRunning()==true)
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
				
				dataInCycleIncrease();
//				System.out.println("cycleCount: "+cycleCount);
				PublicConstants.realCycle = cycleCount;  //仅仅用于蚁群，不影响其他部分
				cycleCount++;

				cycleEnd.set(false);
				synchronized (cycleBegin) {
					// modify by hechen, 2015.11.23
					//避免agent结束时，totalAgentCountTemp还没变化的错误
					//OperateEndCount表示操作结束，即totalAgentCountTemp一定发生了变化
					synchronized(OperateEndCount){
						while(OperateEndCount.intValue()<this.totalAgentCount.intValue())
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
		//数组长度修正
		dataInCycleCorrection();
		
		runFinished();
	}
	
	public int getCycleCount(){
		return cycleCount;
		
	}
	
	//保存每个回合的dataInCycle
	protected void dataInCycleIncrease(){}
	
	//dataInCycle数组长度修正
	protected void dataInCycleCorrection(){}
	
	protected void initRun(){}
	
	protected void runFinished(){}
	
	protected abstract void disposeMessage(Message msg);
	
	protected abstract void messageLost(Message msg);

}
