package com.cqu.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Agent implements Runnable{
	
	public final static String KEY_PARENT="parent";
	public final static String KEY_PSEUDO_PARENT="pseudo_parent";
	public final static String KEY_CHILDREN="children";
	public final static String KEY_NEIGHBOUR="neighbour";
	
	public final static int CHECK_MESSAGE_CYCLE=100;
	
	protected int id;
	protected String name;
	
	protected Variable internalVariable;
	protected Variable[] externalVariables;
	protected Map<String, int[]> graph;
	
	protected BlockingQueue<Message> msgQueue;
	
	private boolean isRunning=false;
	
	public Agent(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void initialize(Variable internalVariable, Variable[] externalVariables, Map<String, int[]> graph)
	{
		this.internalVariable=internalVariable;
		this.externalVariables=externalVariables;
		this.graph=graph;
		
		this.msgQueue=new LinkedBlockingQueue<Message>(50);
	}
	
	public void stop()
	{
		isRunning=false;
	}

	@Override
	public void run()
	{
		this.isRunning=true;
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
		
	}
	
	protected abstract void dispose(Message msg);
}
