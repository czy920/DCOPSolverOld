package com.cqu.adopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Context;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.MessageNCCC;
import com.cqu.synchronousqueue.AgentSynchronous;
import com.cqu.test.Debugger;

public class AdoptAgentSynchronous extends AgentSynchronous{
	
	private Map<Integer, int[]> lbs;
	private Map<Integer, int[]> ubs;
	private Map<Integer, int[]> ths;
	private int LB;
	private int UB;
	private int TH;
	
	private Map<Integer, Context[]> contexts;
	private Context currentContext;
	private boolean terminateReceivedFromParent=false;
	
	private int nccc;
	
	public AdoptAgentSynchronous(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		this.nccc=0;
	}
	
	@Override
	public void initRun() {
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
	
	private void sendValueMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int childId=0;
		for(int i=0;i<this.allChildren.length;i++)
		{
			childId=this.allChildren[i];
			Message msg=new Message(this.id, childId, AdoptAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(this.constructNcccMessage(msg));
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
		this.sendMessage(this.constructNcccMessage(msg));
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
			this.sendMessage(this.constructNcccMessage(msg));
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
			this.sendMessage(this.constructNcccMessage(msg));
		}
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" LB="+this.LB+" UB="+Infinity.infinityEasy(this.UB)+" TH="+Infinity.infinityEasy(this.TH));
		}
		
		//do nccc message here
		this.increaseNcccFromMessage((MessageNCCC)msg);
		
		int type=msg.getType();
		if(type==AdoptAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(type==AdoptAgent.TYPE_COST_MESSAGE)
		{
			disposeCostMessage(msg);
		}else if(type==AdoptAgent.TYPE_THRESHOLD_MESSAGE)
		{
			disposeThresholdMessage(msg);
		}else if(type==AdoptAgent.TYPE_TERMINATE_MESSAGE)
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
	
	private void disposeTerminateMessage(Message msg)
	{
		this.terminateReceivedFromParent=true;
		currentContext=(Context) msg.getValue();
        
        //此处与Adopt:Pragnesh Jay Modi et al.中的伪代码不一样
        //terminateReceivedFromParent变为true，再次调用maintainThresholdInvariant()
        //防止TH==UB失败导致agent不能终止
        maintainThresholdInvariant();
		backtrack();
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
		
		int parentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.allParents.length;i++)
		{
			parentId=this.allParents[i];
			for(int j=0;j<this.domain.length;j++)
			{
				oppositeAgentValueIndex=currentContext.get(parentId);
				if(oppositeAgentValueIndex==-1)
				{
					ret[j]+=0;
				}else
				{
					//保证id小的为行，id大的为列
					if(this.id<parentId)
					{
						ret[j]+=this.constraintCosts.get(parentId)[j][oppositeAgentValueIndex];
					}else
					{
						ret[j]+=this.constraintCosts.get(parentId)[oppositeAgentValueIndex][j];
					}
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
		
		int parentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.allParents.length;i++)
		{
			parentId=this.allParents[i];
			
			oppositeAgentValueIndex=currentContext.get(parentId);
			if(oppositeAgentValueIndex==-1)
			{
				ret+=0;
			}else
			{
				//保证id小的为行，id大的为列
				if(this.id<parentId)
				{
					ret+=this.constraintCosts.get(parentId)[di][oppositeAgentValueIndex];
				}else
				{
					ret+=this.constraintCosts.get(parentId)[oppositeAgentValueIndex][di];
				}
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
		
		//do nccc local here
		this.increaseNcccLocal();
		
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
				this.runFinished();
			}
		}
		sendCostMessage();
	}
	
	/**
	 * Non-concurrent constraint checks (NCCCs):NCCCs are a weighted sum of processing<br/>
	 * and communication time. Every agent A maintains a counter NCCC(A), which is <br/>
	 * initialized to 0. The agent assigns NCCC(A):= NCCC(A)+1 every time it performs <br/>
	 * a constraint check to account for the time it takes to perform the constraint<br/>
	 * check. It assigns NCCC(A):=max{NCCC(A), NCCC(B)+t} every time it receives a <br/>
	 * message from agent B to account for the time it takes to wait for agent B to<br/>
	 * send the message (NCCC(B)) and the transmission time of the message (t). We use<br/> 
	 * t = 0 to simulate fast communication and t = 1000 to simulate slow communication.<br/> 
	 * The number of NCCCs then is the largest counter value of any agent. NCCCs are a<br/> 
	 * good runtime metric if the ratio of processing and communication time can be estimated reliably.
	 */
	private void increaseNcccLocal()
	{
		this.nccc++;
	}
	
	private void increaseNcccFromMessage(MessageNCCC mn)
	{
		int t=0;
		this.nccc=Math.max(mn.getNccc()+t, this.nccc);
	}
	
	private Message constructNcccMessage(Message msg)
	{
		return new MessageNCCC(msg, this.nccc);
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
	
	private void runFinished() {
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AdoptAgent.KEY_ID, this.id);
		result.put(AdoptAgent.KEY_NAME, this.name);
		result.put(AdoptAgent.KEY_VALUE, this.domain[valueIndex]);
		result.put(AdoptAgent.KEY_LB, this.LB);
		result.put(AdoptAgent.KEY_UB, this.UB);
		result.put(AdoptAgent.KEY_TH, this.TH);
		result.put(AdoptAgent.KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=-1;
		int maxNccc=0;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AdoptAgent.KEY_ID);
			String name_=(String) result.get(AdoptAgent.KEY_NAME);
			int value_=(Integer) result.get(AdoptAgent.KEY_VALUE);
			int LB_=(Integer) result.get(AdoptAgent.KEY_LB);
			int UB_=(Integer) result.get(AdoptAgent.KEY_UB);
			int TH_=(Integer) result.get(AdoptAgent.KEY_TH);
			int ncccTemp=(Integer) result.get(AdoptAgent.KEY_NCCC);
			if(maxNccc<ncccTemp)
			{
				maxNccc=ncccTemp;
			}
			if(totalCost<UB_)
			{
				totalCost=UB_;
			}
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_+" LB="+LB_+" UB=";
			displayStr+=Infinity.infinityEasy(UB_);
			displayStr+=" TH="+Infinity.infinityEasy(TH_);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: "+Infinity.infinityEasy(totalCost)+" NCCC: "+maxNccc);
	}

	@Override
	public String easyMessageContent(Message msg, AgentSynchronous sender, AgentSynchronous receiver) {
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
			int LB_=(Integer) msgValue.get(AdoptAgent.KEY_LB);
			int UB_=(Integer) msgValue.get(AdoptAgent.KEY_UB);
			Context c=(Context) msgValue.get(AdoptAgent.KEY_CONTEXT);
			return "cost[LB="+LB_+" UB="+Infinity.infinityEasy(UB_)+" context="+c.toString()+"]";
		}
		case AdoptAgent.TYPE_THRESHOLD_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int TH_=(Integer) msgValue.get(AdoptAgent.KEY_TH);
			Context c=(Context) msgValue.get(AdoptAgent.KEY_CONTEXT);
			return "threshold[TH="+Infinity.infinityEasy(TH_)+" context="+c.toString()+"]";
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
