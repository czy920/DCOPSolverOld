package com.cqu.core;

import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;

/**
 * computing unit
 * @author CQU
 *
 */
public abstract class Agent extends QueueMessager{
	
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
	
	protected MessageMailer msgMailer;
	
	protected int valueIndex;
	
	public Agent(int id, String name, int level, int[] domain) {
		super("Agent "+name, QUEUE_CAPACITY);
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
	
	public void setMessageMailer(MessageMailer msgMailer)
	{
		this.msgMailer=msgMailer;
	}
	
	public void sendMessage(Message msg)
	{
		msgMailer.addMessage(msg);
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		try {
			Thread.sleep(100);//延迟启动，让所有的Agent thread创建完成后再运行
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.currentThread().interrupt();
		}
	}
	
    public abstract void printResults(List<Map<String, Object>> results);
    
	public abstract String easyMessageContent(Message msg, Agent sender, Agent receiver);
	
	public boolean isLeafAgent()
	{
		return this.children==null||this.children.length==0;
	}
	
	public boolean isRootAgent()
	{
		return this.parent==-1;
	}

	public int[] getDomain() {
		return domain;
	}

	public int getValueIndex() {
		return valueIndex;
	}

	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}

	public void setDomain(int[] domain) {
		this.domain = domain;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public int[] getPseudoParents() {
		return pseudoParents;
	}

	public void setPseudoParents(int[] pseudoParents) {
		this.pseudoParents = pseudoParents;
	}

	public int[] getChildren() {
		return children;
	}

	public void setChildren(int[] children) {
		this.children = children;
	}

	public int[] getPseudoChildren() {
		return pseudoChildren;
	}

	public void setPseudoChildren(int[] pseudoChildren) {
		this.pseudoChildren = pseudoChildren;
	}
}
