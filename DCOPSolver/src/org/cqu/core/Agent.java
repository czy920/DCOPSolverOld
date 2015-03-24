package org.cqu.core;

import org.cqu.core.control.Message;
import org.cqu.core.control.MessageSink;
import org.cqu.core.control.QueueMessagerListener;
import org.cqu.core.control.asynchronous.QueueMessager;
import org.cqu.statistics.StatisticsListener;

/**
 * computing unit
 * @author CQU
 *
 */
public abstract class Agent implements MessageSink{
	
	protected int id;
	protected String name;
	protected int[] domain;
	protected int[] neighbours;
	protected int valueIndex;
	protected QueueMessager queueMessager;
	protected MessageSink msgSink;
	protected StatisticsListener statisticsListener;
	protected final int capacity;
	
	public Agent(int id, String name, int[] domain, int[] neighbours, MessageSink msgSink, StatisticsListener statisticsListener, int capacity) {
		this.id = id;
		this.name = name;
		this.domain=domain;
		this.neighbours=neighbours;
		this.msgSink=msgSink;
		this.statisticsListener=statisticsListener;
		this.capacity=capacity;
		
		this.valueIndex=0;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getValue()
	{
		return this.domain[this.valueIndex];
	}
	
	public final void start()
	{
		if(this.queueMessager==null)
		{
			this.queueMessager=QueueMessager.newQueueMessager("Agent"+id, queueMessagerListener, capacity);
			this.queueMessager.startProcess();
		}
	}
	
	protected final void stop()
	{
		if(this.queueMessager!=null)
		{
			this.queueMessager.stopRunning();
		}
	}
	
	private QueueMessagerListener queueMessagerListener=new QueueMessagerListener() {
		
		@Override
		public void processEnded(Object data) {
			// TODO Auto-generated method stub
			Agent.this.processEnded();
		}
		
		@Override
		public void messageTaken(Message msg) {
			// TODO Auto-generated method stub
			disposeMessage(msg);
		}
		
		@Override
		public void messageLost(Message msg) {
			// TODO Auto-generated method stub
			Agent.this.messageLost(msg);
		}
		
		@Override
		public void messageAdded(Message msg) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void initProcess() {
			// TODO Auto-generated method stub
			Agent.this.initProcess();
		}
	};
	
	protected void sendMessage(Message msg)
	{
		msgSink.addMessage(msg);
	}

	@Override
	public final void addMessage(Message msg) {
		// TODO Auto-generated method stub
		queueMessager.addMessage(msg);
	}
	
	protected void initProcess(){}
	protected void processEnded(){}
	protected void messageLost(Message msg){}
	protected abstract void disposeMessage(Message msg);
}
