package com.cqu.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 计算单位
 * @author CQU
 *
 */
public abstract class Agent extends ThreadEx{
	
	public final static int INFINITY=Integer.MAX_VALUE;
	
	protected int id;
	protected String name;
	protected int[] domain;
	
	protected int[] neighbours;
	protected int parent;
	protected int[] pseudoParents;
	protected int[] children;
	protected int[] pseudoChildren;
	
	protected Map<Integer, int[]> neighbourDomains;
	protected Map<Integer, int[][]> constraintCosts;
	
	
	protected BlockingQueue<Message> msgQueue;
	
	private boolean isRunning=false;
	
	protected MessageMailer msgMailer;
	
	public Agent(int id, String name, int[] domain) {
		super();
		this.id = id;
		this.name = name;
		this.domain=domain;
		
		this.msgQueue=new LinkedBlockingQueue<Message>(50);
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setNeibours(int[] neighbours, int parent, int[] children, int[] pseudoParents, int[] pseudoChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, int[][]> constraintCosts)
	{
		this.neighbours=neighbours;
		this.parent=parent;
		this.children=children;
		this.pseudoParents=pseudoParents;
		this.pseudoChildren=pseudoChildren;
		
		this.neighbourDomains=neighbourDomains;
		this.constraintCosts=constraintCosts;
	}
	
	public void setMessageMailer(MessageMailer msgMailer)
	{
		this.msgMailer=msgMailer;
	}
	
	@Override
	protected void runProcess() {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(100);//延迟启动，让所有的Agent thread创建完成后再运行
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
		
		initRun();
		
		while(isRunning==true)
		{
			Message msg;
			try {
				msg = msgQueue.take();
				if(msg!=null)
				{
					dispose(msg);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				Thread.currentThread().interrupt();
			}
		}
		
		runFinished();
	}
	
	public void addMessage(Message msg)
	{
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}
	
	public void sendMessage(Message msg)
	{
		msgMailer.addMessage(msg);
	}
	
	protected abstract void initRun();
	protected abstract void runFinished();
	protected abstract void dispose(Message msg);
    public abstract void printResults(List<Map<String, Object>> results);
	public abstract String easyMessageContent(Message msg, Agent sender, Agent receiver);
}
