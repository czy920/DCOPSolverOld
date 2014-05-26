package com.cqu.adopt;

import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class AdoptAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_COST_MESSAGE=1;
	public final static int TYPE_THRESHOLD_MESSAGE=2;
	public final static int TYPE_TERMINATE_MESSAGE=3;
	
	private int[][] lbs;
	private int[][] ubs;
	private int[][] ths;
	private int LB;
	private int UB;
	private int TH;
	
	private Map[][] contexts;
	private Map<Integer, Integer> currentContext;
	
	private int valueIndex;
	
	
	public AdoptAgent(int id, String name, int[] domain) {
		super(id, name, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		TH=0;
		currentContext=new HashMap<Integer, Integer>(); 
		
		lbs=new int[this.domain.length][this.children.length];
		ubs=new int[this.domain.length][this.children.length];
		contexts=new HashMap[this.domain.length][this.children.length];
		for(int i=0;i<this.domain.length;i++)
		{
			for(int j=0;j<this.children.length;j++)
			{
				lbs[i][j]=0;
				ubs[i][j]=Agent.INFINITY;
				ths[i][j]=0;
				contexts[i][j]=new HashMap<Integer, Integer>();
			}
		}
		
		valueIndex=this.computeLB();
		this.sendValueMessages();
		
		backtrack();
	}
	
	private void sendValueMessages()
	{
		for(int i=0;i<this.pseudoChildren.length;i++)
		{
			Message msg=new Message(this.id, this.pseudoChildren[i], AdoptAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendCostMessage()
	{
		
	}
	
	private void sendThresholdMessages()
	{
		
	}
	
	private void sendTerminateMessages()
	{
		
	}

	@Override
	protected void dispose(Message msg) {
		// TODO Auto-generated method stub
		if(msg.getType()==AdoptAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==AdoptAgent.TYPE_COST_MESSAGE)
		{
			disposeCostMessage(msg);
		}else if(msg.getType()==AdoptAgent.TYPE_THRESHOLD_MESSAGE)
		{
			disposeThresholdMessage(msg);
		}else if(msg.getType()==AdoptAgent.TYPE_TERMINATE_MESSAGE)
		{
			disposeTerminateMessage(msg);
		}
	}
	
	private void disposeValueMessage(Message msg)
	{
		
	}
	
	private void disposeCostMessage(Message msg)
	{
		
	}
	
	private void disposeThresholdMessage(Message msg)
	{
		
	}
	
	private void disposeTerminateMessage(Message msg)
	{
		
	}
	
	private int computerLB(int di)
	{
		return 0;
	}
	
	private int computeLB()
	{
		return 0;
	}
	
	private int computeUB()
	{
		return 0;
	}
	
	private void backtrack()
	{
		
	}
	
	private void maintainThresholdInvariant()
	{
		
	}
	
	private void maintainAllocationInvariant()
	{
		
	}
	
	private void maintainChildThresholdInvariant()
	{
		
	}
}
