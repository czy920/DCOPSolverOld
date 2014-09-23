package com.cqu.synchronousqueue;

import java.util.List;
import java.util.Map;

import com.cqu.core.Message;
import com.cqu.util.CollectionUtil;

public abstract class AgentSynchronous extends FakeQueueMessager{
	
	public final static String KEY_ID="KEY_ID";
	public final static String KEY_NAME="KEY_NAME";
	public final static String KEY_VALUE="KEY_VALUE";
	
	public static int QUEUE_CAPACITY=50;
	
	protected int id;
	protected String name;
	protected int level;
	protected int[] domain;
	
	protected int[] neighbours;
	protected int parent;
	protected int[] allParents;
	protected int[] pseudoParents;
	protected int[] allChildren;
	protected int[] children;
	protected int[] pseudoChildren;
	
	protected Map<Integer, int[]> neighbourDomains;
	protected Map<Integer, Integer> neighbourLevels;
	protected Map<Integer, int[][]> constraintCosts;
	
	protected MessageMailerSynchronous msgMailer;
	
	protected int valueIndex;
	
	public AgentSynchronous(int id, String name, int level, int[] domain) {
		super();
		this.id = id;
		this.level=level;
		this.name = name;
		this.domain=domain;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setNeibours(int[] neighbours, int parent, int[] children, int[] allParents, 
			int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, 
			int[][]> constraintCosts, Map<Integer, Integer> neighbourLevels)
	{
		this.neighbours=neighbours;
		this.parent=parent;
		this.children=children;
		this.allParents=allParents;
		this.allChildren=allChildren;
		if(this.allChildren!=null&&this.children!=null)
		{
			this.pseudoChildren=CollectionUtil.except(this.allChildren, this.children);
		}
		if(this.allParents!=null&&this.parent!=-1)
		{
			this.pseudoParents=CollectionUtil.except(this.allParents, new int[]{this.parent});
		}
		
		this.neighbourDomains=neighbourDomains;
		this.constraintCosts=constraintCosts;
		this.neighbourLevels=neighbourLevels;
	}
	
	public void setMessageMailer(MessageMailerSynchronous msgMailer)
	{
		this.msgMailer=msgMailer;
	}
	
	public void sendMessage(Message msg)
	{
		msgMailer.addMessage(msg);
	}
	
	public abstract void initRun();
	
    public abstract void printResults(List<Map<String, Object>> results);
    
	public abstract String easyMessageContent(Message msg, AgentSynchronous sender, AgentSynchronous receiver);
	
	protected boolean isLeafAgent()
	{
		return this.children==null||this.children.length==0;
	}
	
	protected boolean isRootAgent()
	{
		return this.parent==-1;
	}
}
