package com.cqu.adopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.test.Debugger;

public class AdoptAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_COST_MESSAGE=1;
	public final static int TYPE_THRESHOLD_MESSAGE=2;
	public final static int TYPE_TERMINATE_MESSAGE=Message.TYPE_TERMINATE_MESSAGE;
	
	public final static String KEY_CONTEXT="KEY_CONTEXT";
	public final static String KEY_LB="KEY_LB";
	public final static String KEY_UB="KEY_UB";
	public final static String KEY_TH="KEY_TH";
	
	public final static String KEY_ID="KEY_ID";
	public final static String KEY_NAME="KEY_NAME";
	public final static String KEY_VALUE="KEY_VALUE";
	
	public final static String KEY_VALUE_MESSAGE="KEY_VALUE_MESSAGE";
	
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
		super.initRun();
		
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
					childUbs[j]=Infinity.INFINITY;
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
		
		Debugger.valueChanges.get(this.name).add(this.valueIndex);
		
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
	
	/**
	 * 由于QueueMessager会丢失消息，所以Agent停止前必须保证：
	 * 1. children agent必须接到自己的terminate消息
	 * 2. pseudo children agent必须接到自己的最新value消息
	 * QueueMessager保证了terminate消息一定不会被丢弃
	 * 这两点保证均由terminate消息来保证，即在terminate消息中包含
	 * value消息，并且terminate消息扩展为发往pseudo children
	 */
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
			
			Message valueMsg=new Message(this.id, childId, AdoptAgent.TYPE_VALUE_MESSAGE, valueIndex);
			
			Map<String, Object> mapValue=new HashMap<String, Object>();
			mapValue.put(KEY_CONTEXT, c);
			mapValue.put(KEY_VALUE_MESSAGE, valueMsg);
			
			Message msg=new Message(this.id, childId, AdoptAgent.TYPE_TERMINATE_MESSAGE, mapValue);
			this.sendMessage(msg);
		}
		
		if(this.pseudoChildrenReal!=null)
		{
			int pseudoChildId=0;
			for(int i=0;i<this.pseudoChildrenReal.length;i++)
			{
				pseudoChildId=this.pseudoChildrenReal[i];
				
				Message valueMsg=new Message(this.id, pseudoChildId, AdoptAgent.TYPE_VALUE_MESSAGE, valueIndex);
				
				Message msg=new Message(this.id, pseudoChildId, AdoptAgent.TYPE_TERMINATE_MESSAGE, valueMsg);
				this.sendMessage(msg);
			}
		}
	}
	
	/*private void sendTerminateMessages()
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
	}*/

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" LB="+this.LB+" UB="+Infinity.infinityEasy(this.UB)+" TH="+Infinity.infinityEasy(this.TH));
		}
		
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
	
	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
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
					ubs.get(childId)[j]=Infinity.INFINITY;
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
		}
		if(c.compatible(currentContext)==true)
		{
			lbs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgent.KEY_LB);
			ubs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgent.KEY_UB);
			contexts.get(msg.getIdSender())[myValueIndex]=c;
			
			maintainChildThresholdInvariant();
			maintainThresholdInvariant();
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
	
	@SuppressWarnings("unchecked")
	private void disposeTerminateMessage(Message msg)
	{
		Message valueMsg=null;
		//父agent发过来的terminate消息为包含了基本的terminate消息和value消息
		if(msg.getIdSender()==this.parent)
		{
			Map<String, Object> mapValue=(Map<String, Object>) msg.getValue();
			currentContext=(Context) mapValue.get(KEY_CONTEXT);
			
			valueMsg=(Message) mapValue.get(KEY_VALUE_MESSAGE);
			disposeMessage(valueMsg);
			
            this.terminateReceivedFromParent=true;

			backtrack();
		}else
		{
			//pseudo父agent发过来的terminate消息仅包含了value消息
			valueMsg=(Message) msg.getValue();
			disposeMessage(valueMsg);
		}
		
	}
	
	//return int[]{dMinimizesLB, LB(curValue), dMinimizesUB}
	private int[] computeMinimalLBAndUB()
	{
		int[] localCosts_=this.localCosts();
		int minLB=Infinity.INFINITY;
		int minUB=Infinity.INFINITY;
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
				sumub=Infinity.add(sumub, this.ubs.get(childId)[i]);
			}
			sumlb+=localCosts_[i];
			sumub=Infinity.add(sumub, localCosts_[i]);
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
			TH_di=Infinity.add(TH_di, this.ths.get(childId)[di]);
		}
		TH_di=Infinity.add(TH_di, localCost_);
		
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
					//ret[j]+=this.constraintCosts.get(pseudoParentId)[j][oppositeAgentValueIndex];
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
				//ret+=this.constraintCosts.get(pseudoParentId)[di][oppositeAgentValueIndex];
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
			if(this.valueIndex!=dMinimizesUB)
			{
				Debugger.valueChanges.get(this.name).add(dMinimizesUB);
			}
			
			this.valueIndex=dMinimizesUB;
		}else if(LB_CurValue>this.TH)
		{
			if(this.valueIndex!=dMinimizesLB)
			{
				Debugger.valueChanges.get(this.name).add(dMinimizesLB);
			}
			
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
		int diffOriginalValue=diff;
		int childId=0;
		if(diff>0)
		{
			while(diff!=0)
			{
				diffOriginalValue=diff;
				for(int i=0;i<this.children.length;i++)
				{
					childId=this.children[i];
					int availDiff=Infinity.minus(this.ubs.get(childId)[valueIndex], this.ths.get(childId)[valueIndex]);
					if(availDiff>0)
					{
						if((diff-availDiff)<=0)
						{
							this.ths.get(childId)[valueIndex]=Infinity.add(this.ths.get(childId)[valueIndex],diff);
							diff=0;
							break;
						}else
						{
							this.ths.get(childId)[valueIndex]=Infinity.add(this.ths.get(childId)[valueIndex],availDiff);
							diff=Infinity.minus(diff, availDiff);
						}
					}
				}
				if(diff==diffOriginalValue)
				{
					break;//无法使diff为0，也退出
				}
			}
		}else if(diff<0)
		{
			while(diff!=0)
			{
				diffOriginalValue=diff;
				for(int i=0;i<this.children.length;i++)
				{
					childId=this.children[i];
					int availDiff=Infinity.minus(this.ths.get(childId)[valueIndex], this.lbs.get(childId)[valueIndex]);
					if(availDiff>0)
					{
						if((diff+availDiff)>=0)
						{
							this.ths.get(childId)[valueIndex]=Infinity.minus(this.ths.get(childId)[valueIndex], diff);
							diff=0;
							break;
						}else
						{
							this.ths.get(childId)[valueIndex]=Infinity.minus(this.ths.get(childId)[valueIndex], availDiff);
							diff=Infinity.add(diff, availDiff);
						}
					}
				}
				if(diff==diffOriginalValue)
				{
					break;//无法使diff为0，也退出
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
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AdoptAgent.KEY_ID, this.id);
		result.put(AdoptAgent.KEY_NAME, this.name);
		result.put(AdoptAgent.KEY_VALUE, this.domain[valueIndex]);
		result.put(AdoptAgent.KEY_LB, this.LB);
		result.put(AdoptAgent.KEY_UB, this.UB);
		result.put(AdoptAgent.KEY_TH, this.TH);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=-1;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AdoptAgent.KEY_ID);
			String name_=(String) result.get(AdoptAgent.KEY_NAME);
			int value_=(Integer) result.get(AdoptAgent.KEY_VALUE);
			int LB_=(Integer) result.get(AdoptAgent.KEY_LB);
			int UB_=(Integer) result.get(AdoptAgent.KEY_UB);
			int TH_=(Integer) result.get(AdoptAgent.KEY_TH);
			if(totalCost==-1)
			{
				totalCost=UB_;
			}
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_+" LB="+LB_+" UB=";
			displayStr+=Infinity.infinityEasy(UB_);
			displayStr+=" TH="+Infinity.infinityEasy(TH_);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: "+Infinity.infinityEasy(totalCost));
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AdoptAgent.messageContent(msg);
	}
	
	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case AdoptAgent.TYPE_VALUE_MESSAGE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case AdoptAgent.TYPE_COST_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int LB_=(Integer) msgValue.get(KEY_LB);
			int UB_=(Integer) msgValue.get(KEY_UB);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB="+LB_+" UB="+UB_+" context="+c.toString()+"]";
		}
		case AdoptAgent.TYPE_THRESHOLD_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int TH_=(Integer) msgValue.get(KEY_TH);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "threshold[TH="+TH_+" context="+c.toString()+"]";
		}
		case AdoptAgent.TYPE_TERMINATE_MESSAGE:
		{
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}
}
