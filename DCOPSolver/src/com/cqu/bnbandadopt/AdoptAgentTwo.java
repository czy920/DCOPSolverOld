package com.cqu.bnbandadopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.bnbadopt.Context;
import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.test.Debugger;

public class AdoptAgentTwo extends Agent{

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
	
	private int height;
	
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	private int valueIndex;
	private int valueID;
	private boolean terminateReceivedFromParent=false;
	
	
	public AdoptAgentTwo(int id, String name, int level, int[] domain,int Height) {
		super(id, name, level, domain);
		this.height=Height;
	
	}
	
	public AdoptAgentTwo(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		this.height=0;
	
	}
	
	@Override
	protected void initRun() {
		super.initRun();
		
		TH=0;
		valueID=0;
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
		//valueID=valueID+1;
		Debugger.valueChanges.get(this.name).add(this.valueIndex);
		
		backtrack();
	}
	
	private void sendValueMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int pseudoChildId=0;
		int[] val=new int[3] ;
		val[0]=valueIndex;
		val[1]=valueID;
		val[2]=-1;   //a virtural th
		for(int i=0;i<this.pseudoChildren.length;i++)
		{
			pseudoChildId=this.pseudoChildren[i];
			Message msg=new Message(this.id, pseudoChildId, AdoptAgentTwo.TYPE_VALUE_MESSAGE, val);
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
		cost.put(AdoptAgentTwo.KEY_CONTEXT, new Context(currentContext));
		cost.put(AdoptAgentTwo.KEY_LB, LB);
		cost.put(AdoptAgentTwo.KEY_UB, UB);
		
		Message msg=new Message(this.id, this.parent, AdoptAgentTwo.TYPE_COST_MESSAGE, cost);
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
			thresh.put(AdoptAgentTwo.KEY_CONTEXT, new Context(currentContext));
			thresh.put(AdoptAgentTwo.KEY_TH, this.ths.get(childId)[valueIndex]);
			
			Message msg=new Message(this.id, childId, AdoptAgentTwo.TYPE_THRESHOLD_MESSAGE, thresh);
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
			c.addOrUpdate(this.id, this.valueIndex,this.valueID);
			int[] val=new int[3];
			val[0]=valueIndex;
			val[1]=valueID;
			val[2]=-1;
			Message valueMsg=new Message(this.id, childId, AdoptAgentTwo.TYPE_VALUE_MESSAGE, val);
			
			Map<String, Object> mapValue=new HashMap<String, Object>();
			mapValue.put(KEY_CONTEXT, c);
			mapValue.put(KEY_VALUE_MESSAGE, valueMsg);
			
			Message msg=new Message(this.id, childId, AdoptAgentTwo.TYPE_TERMINATE_MESSAGE, mapValue);
			this.sendMessage(msg);
		}
		
		if(this.pseudoChildren!=null)
		{
			int pseudoChildId=0;
			for(int i=0;i<this.pseudoChildren.length;i++)
			{
				pseudoChildId=this.pseudoChildren[i];
				int[] val=new int[3];
				val[0]=valueIndex;
				val[1]=valueID;
				val[2]=-1;
				Message valueMsg=new Message(this.id, pseudoChildId, AdoptAgentTwo.TYPE_VALUE_MESSAGE, val);
				
				Message msg=new Message(this.id, pseudoChildId, AdoptAgentTwo.TYPE_TERMINATE_MESSAGE, valueMsg);
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
	
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" LB="+this.LB+" UB="+Infinity.infinityEasy(this.UB)+" TH="+Infinity.infinityEasy(this.TH));
		}
		
		if(msg.getType()==AdoptAgentTwo.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==AdoptAgentTwo.TYPE_COST_MESSAGE)
		{
			disposeCostMessage(msg);
		}else if(msg.getType()==AdoptAgentTwo.TYPE_THRESHOLD_MESSAGE)
		{
			disposeThresholdMessage(msg);
		}else if(msg.getType()==AdoptAgentTwo.TYPE_TERMINATE_MESSAGE)
		{
			disposeTerminateMessage(msg);
		}
	}
	
	@Override
	protected void messageLost(Message msg) {
		
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}
	
	protected void disposeValueMessage(Message msg)
	{
		if(this.terminateReceivedFromParent==false)
		{
			int[] val=new int[3];
			val=(int[]) msg.getValue();
			if(msg.getIdSender()==this.id&&this.height==4&&val[2]!=(-1)){
				this.TH=val[2];
				maintainThresholdInvariant();
			}
			currentContext.addOrUpdate(msg.getIdSender(), val[0],val[1]);
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
		Context c=(Context) cost.get(AdoptAgentTwo.KEY_CONTEXT);
		int myValueIndex=c.Remove(this.id);
		
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
			lbs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgentTwo.KEY_LB);
			ubs.get(msg.getIdSender())[myValueIndex]=(Integer) cost.get(AdoptAgentTwo.KEY_UB);
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
	protected void disposeThresholdMessage(Message msg)
	{
		Map<String, Object> thresh=(Map<String, Object>) msg.getValue();
		if(((Context)thresh.get(AdoptAgentTwo.KEY_CONTEXT)).compatible(currentContext)==true)
		{
			this.TH=(Integer) thresh.get(AdoptAgentTwo.KEY_TH);
			maintainThresholdInvariant();
			backtrack();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void disposeTerminateMessage(Message msg)
	{
		Message valueMsg=null;
		//鐖禷gent鍙戣繃鏉ョ殑terminate娑堟伅涓哄寘鍚簡鍩烘湰鐨則erminate娑堟伅鍜寁alue娑堟伅
		if(msg.getIdSender()==this.parent)
		{
			Map<String, Object> mapValue=(Map<String, Object>) msg.getValue();
			currentContext=(Context) mapValue.get(KEY_CONTEXT);
			valueMsg=(Message) mapValue.get(KEY_VALUE_MESSAGE);
			disposeMessage(valueMsg);
			
            this.terminateReceivedFromParent=true;
            
            //姝ゅ涓嶢dopt:Pragnesh Jay Modi et al.涓殑浼唬鐮佷笉涓�牱
            //terminateReceivedFromParent鍙樹负true锛屽啀娆¤皟鐢╩aintainThresholdInvariant()
            //闃叉TH==UB澶辫触瀵艰嚧agent涓嶈兘缁堟
            maintainThresholdInvariant();
			backtrack();
		}else
		{
			valueMsg=(Message) msg.getValue();
			disposeMessage(valueMsg);
		}
		
	}
	
	// return int[]{dMinimizesLB, LB(curValue), dMinimizesUB}
	private int[] computeMinimalLBAndUB() {
		int[] localCosts_ = this.localCosts();
		int minLB = Infinity.INFINITY;
		int minUB = Infinity.INFINITY;
		int dMinimizesLB = 0;
		int LB_CurValue = 0;
		int dMinimizesUB = 0;

		if (this.isLeafAgent() == true) {
			for (int i = 0; i < this.domain.length; i++) {
				if (i == valueIndex) {
					LB_CurValue = localCosts_[i];
				}
				if (localCosts_[i] < minLB) {
					minLB = localCosts_[i];
					dMinimizesLB = i;
				}
				if (localCosts_[i] < minUB) {
					minUB = localCosts_[i];
					dMinimizesUB = i;
				}
			}
			this.LB = minLB;
			this.UB = minUB;

			return new int[] { dMinimizesLB, LB_CurValue, dMinimizesUB };
		}

		int childId = 0;
		for (int i = 0; i < this.domain.length; i++) {
			int sumlb = 0;
			int sumub = 0;
			for (int j = 0; j < this.children.length; j++) {
				childId = this.children[j];
				sumlb += this.lbs.get(childId)[i];
				sumub = Infinity.add(sumub, this.ubs.get(childId)[i]);
			}
			sumlb += localCosts_[i];
			sumub = Infinity.add(sumub, localCosts_[i]);
			if (i == valueIndex) {
				LB_CurValue = sumlb;
			}
			if (sumlb < minLB) {
				minLB = sumlb;
				dMinimizesLB = i;
			}
			if (sumub < minUB) {
				minUB = sumub;
				dMinimizesUB = i;
			}
		}
		this.LB = minLB;
		this.UB = minUB;

		return new int[] { dMinimizesLB, LB_CurValue, dMinimizesUB };
	}
	
	private int computeTH(int di) {
		int localCost_ = localCost(di);

		if (this.isLeafAgent() == true) {
			return localCost_;
		}

		int TH_di = 0;
		int childId = 0;
		for (int i = 0; i < this.children.length; i++) {
			childId = this.children[i];
			TH_di = Infinity.add(TH_di, this.ths.get(childId)[di]);
		}
		TH_di = Infinity.add(TH_di, localCost_);

		return TH_di;
	}
	
	private int[] localCosts() {
		int[] ret = new int[this.domain.length];

		if (this.isRootAgent() == true) {
			for (int i = 0; i < this.domain.length; i++) {
				ret[i] = 0;
			}
			return ret;
		}

		int parentId = 0;
		int oppositeAgentValueIndex = 0;
		for (int i = 0; i < this.allParents.length; i++) {
			parentId = this.allParents[i];
			for (int j = 0; j < this.domain.length; j++) {
				oppositeAgentValueIndex = currentContext.get(parentId);
				if (oppositeAgentValueIndex == -1) {
					ret[j] += 0;
				} else {
					// 保证id小的为行，id大的为列
					if (this.id < parentId) {
						ret[j] += this.constraintCosts.get(parentId)[j][oppositeAgentValueIndex];
					} else {
						ret[j] += this.constraintCosts.get(parentId)[oppositeAgentValueIndex][j];
					}
				}
			}
		}
		return ret;
	}
	
	private int localCost(int di) {
		int ret = 0;

		if (this.isRootAgent() == true) {
			return ret;
		}

		int parentId = 0;
		int oppositeAgentValueIndex = 0;
		for (int i = 0; i < this.allParents.length; i++) {
			parentId = this.allParents[i];

			oppositeAgentValueIndex = currentContext.get(parentId);
			if (oppositeAgentValueIndex == -1) {
				ret += 0;
			} else {
				// 保证id小的为行，id大的为列
				if (this.id < parentId) {
					ret += this.constraintCosts.get(parentId)[di][oppositeAgentValueIndex];
				} else {
					ret += this.constraintCosts.get(parentId)[oppositeAgentValueIndex][di];
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
		if(this.TH==this.UB)
		{
			if(this.valueIndex!=dMinimizesUB)
			{
				Debugger.valueChanges.get(this.name).add(dMinimizesUB);
			}
			
			this.valueIndex=dMinimizesUB;
			//valueID=valueID+1;
		}else if(LB_CurValue>this.TH)
		{
			if(this.valueIndex!=dMinimizesLB)
			{
				Debugger.valueChanges.get(this.name).add(dMinimizesLB);
			}
			
			this.valueIndex=dMinimizesLB;
			//valueID=valueID+1;
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
					break;//鏃犳硶浣縟iff涓�锛屼篃閫�嚭
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
					break;//鏃犳硶浣縟iff涓�锛屼篃閫�嚭
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
		
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AdoptAgentTwo.KEY_ID, this.id);
		result.put(AdoptAgentTwo.KEY_NAME, this.name);
		result.put(AdoptAgentTwo.KEY_VALUE, this.domain[valueIndex]);
		result.put(AdoptAgentTwo.KEY_LB, this.LB);
		result.put(AdoptAgentTwo.KEY_UB, this.UB);
		result.put(AdoptAgentTwo.KEY_TH, this.TH);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		
		int totalCost=-1;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AdoptAgentTwo.KEY_ID);
			String name_=(String) result.get(AdoptAgentTwo.KEY_NAME);
			int value_=(Integer) result.get(AdoptAgentTwo.KEY_VALUE);
			int LB_=(Integer) result.get(AdoptAgentTwo.KEY_LB);
			int UB_=(Integer) result.get(AdoptAgentTwo.KEY_UB);
			int TH_=(Integer) result.get(AdoptAgentTwo.KEY_TH);
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
		
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AdoptAgentTwo.messageContent(msg);
	}
	
	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case AdoptAgentTwo.TYPE_VALUE_MESSAGE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case AdoptAgentTwo.TYPE_COST_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int LB_=(Integer) msgValue.get(KEY_LB);
			int UB_=(Integer) msgValue.get(KEY_UB);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB="+LB_+" UB="+UB_+" context="+c.toString()+"]";
		}
		case AdoptAgentTwo.TYPE_THRESHOLD_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int TH_=(Integer) msgValue.get(KEY_TH);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "threshold[TH="+TH_+" context="+c.toString()+"]";
		}
		case AdoptAgentTwo.TYPE_TERMINATE_MESSAGE:
		{
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}
}
