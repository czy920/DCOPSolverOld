package com.cqu.adopt;

import java.util.HashMap;
import java.util.List;
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
	
	public final static String KEY_ID="KEY_ID";
	public final static String KEY_VALUE="KEY_VALUE";
	
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
		ths=new HashMap<Integer, int[]>();
		contexts=new HashMap<Integer, Context[]>();
		
		if(this.isLeafAgent()==false)
		{
			int childId=0;
			for(int i=0;i<this.children.length;i++)
			{
				childId=this.children[i];
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
				lbs.put(childId, childLbs);
				ubs.put(childId, childUbs);
				ths.put(childId, childThs);
				contexts.put(childId, childContexts);
			}
		}
		
		valueIndex=this.computeMinimalLBAndUB()[0];
		
		backtrack();
	}
	
	private boolean isLeafAgent()
	{
		return this.children==null;
	}
	
	private boolean isRootAgent()
	{
		return this.parent==-1;
	}
	
	private void sendValueMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int pseudoChildId=0;
		for(int i=0;i<this.pseudoChildren.length;i++)
		{
			pseudoChildId=this.pseudoChildren[i];
			Message msg=new Message(this.id, pseudoChildId, AdoptAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendCostMessage()
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		
		Map<String, Object> cost=new HashMap<String, Object>();
		cost.put(AdoptAgent.KEY_CONTEXT, new Context(currentContext));
		cost.put(AdoptAgent.KEY_LB, LB);
		cost.put(AdoptAgent.KEY_UB, UB);
		
		Message msg=new Message(this.id, this.parent, AdoptAgent.TYPE_COST_MESSAGE, cost);
		this.sendMessage(msg);
	}
	
	private void sendThresholdMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			Map<String, Object> thresh=new HashMap<String, Object>();
			thresh.put(AdoptAgent.KEY_CONTEXT, new Context(currentContext));
			thresh.put(AdoptAgent.KEY_TH, this.ths.get(childId)[valueIndex]);
			
			Message msg=new Message(this.id, childId, AdoptAgent.TYPE_THRESHOLD_MESSAGE, thresh);
			this.sendMessage(msg);
		}
	}
	
	private void sendTerminateMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			Context c=new Context(currentContext);
			c.addOrUpdate(this.id, this.valueIndex);
			
			Message msg=new Message(this.id, childId, AdoptAgent.TYPE_TERMINATE_MESSAGE, c);
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
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			for(int j=0;j<this.domain.length;j++)
			{
				if(contexts.get(childId)[j].compatible(currentContext)==false)
				{
					lbs.get(childId)[j]=0;
					ths.get(childId)[j]=0;
					ubs.get(childId)[j]=Agent.INFINITY;
					contexts.get(childId)[j].reset();
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
		
		if(myValueIndex==-1)
		{
			return;
		}
		
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
	private int[] computeMinimalLBAndUB()
	{
		int[] localCosts_=this.localCosts();
		int minLB=Agent.INFINITY;
		int minUB=Agent.INFINITY;
		int dMinimizesLB=0;
		int LB_CurValue=0;
		int dMinimizesUB=0;
		
		if(this.isLeafAgent()==true)
		{
			for(int i=0;i<this.domain.length;i++)
			{
				if(i==valueIndex)
				{
					LB_CurValue=localCosts_[i];
				}
				if(localCosts_[i]<minLB)
				{
					minLB=localCosts_[i];
					dMinimizesLB=i;
				}
				if(localCosts_[i]<minUB)
				{
					minUB=localCosts_[i];
					dMinimizesUB=i;
				}
			}
			
			this.LB=minLB;
			this.UB=minUB;
			
			return new int[]{dMinimizesLB, LB_CurValue, dMinimizesUB};
		}
		
		int childId=0;
		for(int i=0;i<this.domain.length;i++)
		{
			int sumlb=0;
			int sumub=0;
			for(int j=0;j<this.children.length;j++)
			{
				childId=this.children[j];
				sumlb+=this.lbs.get(childId)[i];
				sumub+=this.ubs.get(childId)[i];
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
		
		this.LB=minLB;
		this.UB=minUB;
		
		return new int[]{dMinimizesLB, LB_CurValue, dMinimizesUB};
	}
	
	private int computeTH(int di)
	{
		int localCost_=localCost(di);
		
		if(this.isLeafAgent()==true)
		{
			return localCost_;
		}
		
		int TH_di=0;
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			TH_di+=this.ths.get(childId)[di];
		}
		TH_di+=localCost_;
		
		return TH_di;
	}
	
	private int[] localCosts()
	{
		int[] ret=new int[this.domain.length];
		
		if(this.isRootAgent()==true)
		{
			for(int i=0;i<this.domain.length;i++)
			{
				ret[i]=0;
			}
			return ret;
		}
		
		int pseudoParentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.pseudoParents.length;i++)
		{
			pseudoParentId=this.pseudoParents[i];
			for(int j=0;j<this.domain.length;j++)
			{
				oppositeAgentValueIndex=currentContext.get(pseudoParentId);
				if(oppositeAgentValueIndex==-1)
				{
					ret[j]+=0;
				}else
				{
					ret[j]+=this.constraintCosts.get(pseudoParentId)[oppositeAgentValueIndex][j];	
				}
			}
		}
		return ret;
	}
	
	private int localCost(int di)
	{
		int ret=0;
		
		if(this.isRootAgent()==true)
		{
			return ret;
		}
		
		int pseudoParentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.pseudoParents.length;i++)
		{
			pseudoParentId=this.pseudoParents[i];
			
			oppositeAgentValueIndex=currentContext.get(pseudoParentId);
			if(oppositeAgentValueIndex==-1)
			{
				ret+=0;
			}else
			{
				ret+=this.constraintCosts.get(pseudoParentId)[oppositeAgentValueIndex][di];	
			}
		}
		return ret;
	}
	
	private void backtrack()
	{
		int[] ret=computeMinimalLBAndUB();
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
			if(this.terminateReceivedFromParent==true||this.isRootAgent()==true)
			{
				sendTerminateMessages();
				this.stopRunning();
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
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int diff=this.TH-computeTH(valueIndex);
		int childId=0;
		if(diff>0)
		{
			while(diff!=0)
			{
				for(int i=0;i<this.children.length;i++)
				{
					childId=this.children[i];
					if(this.ubs.get(childId)[valueIndex]>this.ths.get(childId)[valueIndex])
					{
						this.ths.get(childId)[valueIndex]+=1;
						diff-=1;
						if(diff==0)
						{
							break;
						}
					}
				}
			}
		}else if(diff<0)
		{
			while(diff!=0)
			{
				for(int i=0;i<this.children.length;i++)
				{
					childId=this.children[i];
					if(this.ths.get(childId)[valueIndex]>this.lbs.get(childId)[valueIndex])
					{
						this.ths.get(childId)[valueIndex]-=1;
						diff+=1;
						if(diff==0)
						{
							break;
						}
					}
				}
			}
		}
		
		sendThresholdMessages();
	}
	
	private void maintainChildThresholdInvariant()
	{
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			for(int j=0;j<this.domain.length;j++)
			{
				if(lbs.get(childId)[j]>ths.get(childId)[j])
				{
					ths.get(childId)[j]=lbs.get(childId)[j];
				}
				if(ths.get(childId)[j]>ubs.get(childId)[j])
				{
					ths.get(childId)[j]=ubs.get(childId)[j];
				}
			}
		}
	}

	@Override
	protected void finished() {
		// TODO Auto-generated method stub
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AdoptAgent.KEY_ID, this.id);
		result.put(AdoptAgent.KEY_VALUE, this.domain[valueIndex]);
		result.put(AdoptAgent.KEY_LB, this.LB);
		result.put(AdoptAgent.KEY_UB, this.UB);
		result.put(AdoptAgent.KEY_TH, this.TH);
		
		this.msgMailer.setResult(result);
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=0;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AdoptAgent.KEY_ID);
			int value_=(Integer) result.get(AdoptAgent.KEY_VALUE);
			int LB_=(Integer) result.get(AdoptAgent.KEY_LB);
			int UB_=(Integer) result.get(AdoptAgent.KEY_UB);
			int TH_=(Integer) result.get(AdoptAgent.KEY_TH);
			if(totalCost<UB_)
			{
				totalCost=UB_;
			}
			
			System.out.println(id_+" "+value_+" "+LB_+" "+UB_+" "+TH_);
		}
		System.out.println("totalCost: "+totalCost);
	}
}
