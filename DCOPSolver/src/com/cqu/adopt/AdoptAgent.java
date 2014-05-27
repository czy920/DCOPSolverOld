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
	
	public final static String KEY_CONTEXT="KEY_CONTEXT";
	public final static String KEY_LB="KEY_LB";
	public final static String KEY_UB="KEY_UB";
	public final static String KEY_TH="KEY_TH";
	
	private Map<Integer, int[]> lbs;
	private Map<Integer, int[]> ubs;
	private Map<Integer, int[]> ths;
	private int LB;
	private int UB;
	private int TH;
	
	private Map<Integer, Context[]> contexts;
	private Context currentContext;
	
	private int valueIndex;
	private boolean terminateReceivedFromParent=false;
	
	
	public AdoptAgent(int id, String name, int[] domain) {
		super(id, name, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		TH=0;
		currentContext=new Context(); 
		
		lbs=new HashMap<Integer, int[]>();
		ubs=new HashMap<Integer, int[]>();
		contexts=new HashMap<Integer, Context[]>();
		for(int i=0;i<this.children.length;i++)
		{
			int[] childLbs=new int[this.domain.length];
			int[] childUbs=new int[this.domain.length];
			int[] childThs=new int[this.domain.length];
			Context[] childContexts=new Context[this.domain.length];
			for(int j=0;j<this.domain.length;j++)
			{
				childLbs[j]=0;
				childUbs[j]=Agent.INFINITY;
				childThs[j]=0;
				childContexts[j]=new Context();
			}
			lbs.put(this.children[i], childLbs);
			ubs.put(this.children[i], childUbs);
			ths.put(this.children[i], childThs);
			contexts.put(this.children[i], childContexts);
		}
		
		valueIndex=this.computeLBAndUB()[0];
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
		Map<String, Object> cost=new HashMap<String, Object>();
		cost.put(AdoptAgent.KEY_CONTEXT, new Context(currentContext));
		cost.put(AdoptAgent.KEY_LB, LB);
		cost.put(AdoptAgent.KEY_UB, UB);
		
		Message msg=new Message(this.id, this.parent, AdoptAgent.TYPE_COST_MESSAGE, cost);
		this.sendMessage(msg);
	}
	
	private void sendThresholdMessages()
	{
		for(int i=0;i<this.children.length;i++)
		{
			Map<String, Object> thresh=new HashMap<String, Object>();
			thresh.put(AdoptAgent.KEY_CONTEXT, new Context(currentContext));
			thresh.put(AdoptAgent.KEY_TH, this.ths.get(i)[valueIndex]);
			
			Message msg=new Message(this.id, this.children[i], AdoptAgent.TYPE_THRESHOLD_MESSAGE, thresh);
			this.sendMessage(msg);
		}
	}
	
	private void sendTerminateMessages()
	{
		for(int i=0;i<this.children.length;i++)
		{
			Context c=new Context(currentContext);
			c.addOrUpdate(this.id, this.valueIndex);
			
			Message msg=new Message(this.id, this.children[i], AdoptAgent.TYPE_TERMINATE_MESSAGE, c);
			this.sendMessage(msg);
		}
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
		if(this.terminateReceivedFromParent==false)
		{
			currentContext.addOrUpdate(msg.getIdSender(), (Integer)msg.getValue());
			checkCompatible();
			maintainThresholdInvariant();
			backtrack();
		}
	}
	
	private void checkCompatible()
	{
		for(int i=0;i<this.children.length;i++)
		{
			for(int j=0;j<this.domain.length;j++)
			{
				if(contexts.get(i)[j].compatible(currentContext)==false)
				{
					lbs.get(i)[j]=0;
					ths.get(i)[j]=0;
					ubs.get(i)[j]=Agent.INFINITY;
					contexts.get(i)[j].reset();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void disposeCostMessage(Message msg)
	{
		Map<String, Object> cost=(Map<String, Object>) msg.getValue();
		Context c=(Context) cost.get(AdoptAgent.KEY_CONTEXT);
		int myValueIndex=c.remove(this.id);
		if(this.terminateReceivedFromParent==false)
		{
			merge(c);
			checkCompatible();
			if(c.compatible(currentContext)==true)
			{
				lbs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgent.KEY_LB);
				ubs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgent.KEY_UB);
				contexts.get(msg.getIdSender())[myValueIndex]=c;
				
				maintainChildThresholdInvariant();
				maintainThresholdInvariant();
			}
		}
		backtrack();
		
	}
	
	private void merge(Context c)
	{
		Context temp=new Context(c);
		temp.remove(this.neighbours);
		currentContext.union(temp);
	}
	
	@SuppressWarnings("unchecked")
	private void disposeThresholdMessage(Message msg)
	{
		Map<String, Object> thresh=(Map<String, Object>) msg.getValue();
		if(((Context)thresh.get(AdoptAgent.KEY_CONTEXT)).compatible(currentContext)==true)
		{
			this.TH=(Integer) thresh.get(AdoptAgent.KEY_TH);
			maintainThresholdInvariant();
			backtrack();
		}
	}
	
	private void disposeTerminateMessage(Message msg)
	{
		this.terminateReceivedFromParent=true;
		currentContext=(Context) msg.getValue();
		backtrack();
	}
	
	//return int[]{dMinimizesLB, LB(curValue), dMinimizesUB}
	private int[] computeLBAndUB()
	{
		int[] localCosts_=this.localCosts();
		int minLB=Agent.INFINITY;
		int minUB=Agent.INFINITY;
		int dMinimizesLB=0;
		int LB_CurValue=0;
		int dMinimizesUB=0;
		for(int i=0;i<this.domain.length;i++)
		{
			int sumlb=0;
			int sumub=0;
			for(int j=0;j<this.children.length;j++)
			{
				sumlb+=this.lbs.get(j)[i];
				sumub+=this.ubs.get(j)[i];
			}
			sumlb+=localCosts_[i];
			sumub+=localCosts_[i];
			if(i==valueIndex)
			{
				LB_CurValue=sumlb;
			}
			if(sumlb<minLB)
			{
				minLB=sumlb;
				dMinimizesLB=i;
			}
			if(sumub<minUB)
			{
				minUB=sumub;
				dMinimizesUB=i;
			}
		}
		return new int[]{dMinimizesLB, LB_CurValue, dMinimizesUB};
	}
	
	private int[] localCosts()
	{
		int[] ret=new int[this.domain.length];
		for(int i=0;i<this.pseudoParents.length;i++)
		{
			for(int j=0;j<this.domain.length;j++)
			{
				ret[j]+=this.constraintCosts.get(i)[currentContext.get(this.pseudoParents[i])][j];
			}
		}
		return ret;
	}
	
	private void backtrack()
	{
		int[] ret=computeLBAndUB();
		int dMinimizesLB=ret[0];
		int LB_CurValue=ret[1];
		int dMinimizesUB=ret[2];
		if(this.TH==this.UB)
		{
			this.valueIndex=dMinimizesUB;
		}else if(LB_CurValue>this.TH)
		{
			this.valueIndex=dMinimizesLB;
		}
		sendValueMessages();
		maintainAllocationInvariant();
		if(this.TH==this.UB)
		{
			if(this.terminateReceivedFromParent==true||this.parent==-1)
			{
				sendTerminateMessages();
				this.stop();
			}
		}
		sendCostMessage();
	}
	
	private void maintainThresholdInvariant()
	{
		if(this.TH<this.LB)
		{
			this.TH=this.LB;
		}
		if(this.TH>this.UB)
		{
			this.TH=this.UB;
		}
	}
	
	private void maintainAllocationInvariant()
	{
		//fasdfa
		
		sendThresholdMessages();
	}
	
	private void maintainChildThresholdInvariant()
	{
		for(int i=0;i<this.children.length;i++)
		{
			for(int j=0;j<this.domain.length;j++)
			{
				if(lbs.get(i)[j]>ths.get(i)[j])
				{
					ths.get(i)[j]=lbs.get(i)[j];
				}
				if(ths.get(i)[j]>ubs.get(i)[j])
				{
					ths.get(i)[j]=ubs.get(i)[j];
				}
			}
		}
	}
}
