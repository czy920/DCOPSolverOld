package com.cqu.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 计算单位
 * @author CQU
 *
 */
public abstract class Agent implements Runnable{
	
	public final static int INFINITY=Integer.MAX_VALUE;
	
	protected int id;
	protected String name;
	protected int[] domain;
	protected Map<Integer, Integer> valueIndexes;
	
	protected int[] neighbours;
	protected int parent;
	protected int[] pseudoParents;
	protected int[] children;
	protected int[] pseudoChildren;
	
	protected List<int[]> neighbourDomains;
	protected List<int[][]> constraintCosts;
	
	
	protected BlockingQueue<Message> msgQueue;
	
	private boolean isRunning=false;
	
	private MessageMailer msgMailer;
	
	public Agent(int id, String name, int[] domain) {
		super();
		this.id = id;
		this.name = name;
		this.domain=domain;
		
		this.msgQueue=new LinkedBlockingQueue<Message>(50);
		
		this.valueIndexes=new HashMap<Integer, Integer>();
		for(int i=0;i<domain.length;i++)
		{
			this.valueIndexes.put(domain[i], i);
		}
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setNeibours(int[] neighbours, int parent, int[] children, int[] pseudoParents, int[] pseudoChildren, List<int[]> neighbourDomains, List<int[][]> constraintCosts)
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
	
	public void stop()
	{
		isRunning=false;
	}

	@Override
	public void run()
	{
		this.isRunning=true;
		
		initRun();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//延迟启动，让所有的Agent thread创建完成后再运行
		
		while(isRunning==true)
		{
			Message msg;
			try {
				msg = msgQueue.take();
				dispose(msg);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	protected abstract void initRun();
	
	public void addMessage(Message msg)
	{
		try {
			msgQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(Message msg)
	{
		msgMailer.addMessage(msg);
	}
	
	protected abstract void dispose(Message msg);
}
