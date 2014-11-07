package com.cqu.adopt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.cqu.bnbadopt.Context;
import com.cqu.core.Infinity;
import com.cqu.core.Message;

import java.util.List;

import com.cqu.core.MessageNCCC;
import com.cqu.core.ResultAdopt;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.test.Debugger;

public class AdoptAgentCycle_2 extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_COST_MESSAGE = 1;
	//public final static int TYPE_THRESHOLD_MESSAGE = 2;
	public final static int TYPE_TERMINATE_MESSAGE = Message.TYPE_TERMINATE_MESSAGE;

	public final static String KEY_CONTEXT = "KEY_CONTEXT";
	public final static String KEY_LB = "KEY_LB";
	public final static String KEY_UB = "KEY_UB";
	public final static String KEY_TH = "KEY_TH";
	public final static String KEY_NCCC="KEY_NCCC";
	
	private Map<Integer, int[]> lbs;
	private Map<Integer, int[]> ubs;
	private Map<Integer, int[]> ths;
	private int LB;
	private int UB;
	private int TH;

	private Map<Integer, Context[]> contexts;
	private Context currentContext;
	
	private int valueID;
	private boolean terminateReceivedFromParent = false;
	private boolean Readytermintate = false;
	
	private int nccc;

	public AdoptAgentCycle_2(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		this.nccc = 0;
	}
	
	@Override
	protected void initRun() {
		super.initRun();
		
		TH = 0;
		valueID = 0;
		currentContext = new Context();
		
		if(!isRootAgent())
			currentContext.addOrUpdate(this.parent, 0, 0); //仅仅初始化为第1个取值
		if(this.pseudoParents!=null)
		{
			for(int pseudoP:this.pseudoParents){
			    currentContext.addOrUpdate(pseudoP, 0, 0); //仅仅初始化为第1个取值
			}
		}

		lbs = new HashMap<Integer, int[]>();
		ubs = new HashMap<Integer, int[]>();
		ths = new HashMap<Integer, int[]>();
		contexts = new HashMap<Integer, Context[]>();
		InitChild();
		InitSelf();
		backtrack();
		
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
		}else if(type==AdoptAgent.TYPE_TERMINATE_MESSAGE)
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
	
	private void maintainAllocationInvariant(){
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
	}
	
	private void maintainChildThresholdInvariant(){
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
		result.put(AdoptAgentCycle_2.KEY_ID, this.id);
		result.put(AdoptAgentCycle_2.KEY_NAME, this.name);
		result.put(AdoptAgentCycle_2.KEY_VALUE, this.domain[valueIndex]);
		result.put(AdoptAgentCycle_2.KEY_LB, this.LB);
		result.put(AdoptAgentCycle_2.KEY_UB, this.UB);
		result.put(AdoptAgentCycle_2.KEY_TH, this.TH);
		result.put(AdoptAgentCycle_2.KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public Object printResults(List<Map<String, Object>> results) {
	
		int totalCost=-1;
		int maxNccc=0;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AdoptAgentCycle_2.KEY_ID);
			String name_=(String) result.get(AdoptAgentCycle_2.KEY_NAME);
			int value_=(Integer) result.get(AdoptAgentCycle_2.KEY_VALUE);
			int LB_=(Integer) result.get(AdoptAgentCycle_2.KEY_LB);
			int UB_=(Integer) result.get(AdoptAgentCycle_2.KEY_UB);
			int TH_=(Integer) result.get(AdoptAgentCycle_2.KEY_TH);
			int ncccTemp=(Integer) result.get(AdoptAgentCycle_2.KEY_NCCC);
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
		
		ResultAdopt ret=new ResultAdopt();
		ret.totalCost=totalCost;
		ret.nccc=maxNccc;
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
	
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AdoptAgent.messageContent(msg);
	}
	
	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case AdoptAgentCycle_2.TYPE_VALUE_MESSAGE:
		{
			int valueIndex=(Integer) msg.getValue();
			return "value["+valueIndex+"]";
		}
		case AdoptAgentCycle_2.TYPE_COST_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int LB_=(Integer) msgValue.get(KEY_LB);
			int UB_=(Integer) msgValue.get(KEY_UB);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB="+LB_+" UB="+Infinity.infinityEasy(UB_)+" context="+c.toString()+"]";
		}
		case AdoptAgentCycle_2.TYPE_TERMINATE_MESSAGE:
		{
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}
	
	private void disposeTerminateMessage(Message msg){
		Readytermintate=true;
		if(this.msgQueue.isEmpty())backtrack();	
	}
	
	@SuppressWarnings("unchecked")
	private void disposeCostMessage(Message msg){
		Map<String, Object> cost=(Map<String, Object>) msg.getValue();
		Context c = (Context) cost.get(AdoptAgentCycle_2.KEY_CONTEXT);
		int myValueIndex = c.get(this.id);

		if (myValueIndex == -1) {
			return;
		}			

		Context temp = new Context(currentContext);
		if (terminateReceivedFromParent == false) {
			merge(c);
			currentContext.Remove(this.id);   //因为合并时将自己的取值加入，应该移除
			if (!checkCompatible(currentContext, temp)) {  //不兼容表示引入了新的内容
				checkCompatible();
			}
		}
		if (checkCompatible(c,currentContext) == true) {
			lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
					.get(AdoptAgentCycle_2.KEY_LB);
			ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
					.get(AdoptAgentCycle_2.KEY_UB);
			contexts.get(msg.getIdSender())[myValueIndex] = c;

			//maintainChildThresholdInvariant();
			//maintainThresholdInvariant();
		}
		if (!checkCompatible(currentContext, temp)) {
			InitSelf();
		}
		if(this.msgQueue.isEmpty())backtrack();	
	}
	
	private void disposeValueMessage(Message msg){   //阈值一起处理
		if (terminateReceivedFromParent == false) {
			Context temp = new Context(currentContext);
			int[] val =(int[]) msg.getValue();
			currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
			
			if(!checkCompatible(currentContext,temp))
			{
				checkCompatible();
				InitSelf();
			}
			if (msg.getIdSender() == this.parent && val[2] != (-1)) 
				TH = val[2];
			//maintainThresholdInvariant();  //放在backtrack函数里面一次性做
			if(this.msgQueue.isEmpty())backtrack();
		}	
	}
	
	private void sendTerminateMessages(){

		if (this.isLeafAgent() == true) {
			return;
		}
		terminateReceivedFromParent=true;
		int childId = 0;
		for (int i = 0; i < this.children.length; i++) {
			childId = this.children[i];

			Map<String, Object> mapValue = new HashMap<String, Object>();
			Message msg = new Message(this.id, childId,
					AdoptAgentCycle_2.TYPE_TERMINATE_MESSAGE, mapValue);
			this.sendMessage(constructNcccMessage(msg));
		}	
		
	}
	
	private void sendCostMessage(){
		if (this.isRootAgent() == true) {
			return;
		}

		Map<String, Object> cost = new HashMap<String, Object>();
		Context context = new Context(currentContext);
		context.Remove(this.id);    
		cost.put(AdoptAgentCycle_2.KEY_CONTEXT, new Context(context));
		cost.put(AdoptAgentCycle_2.KEY_LB, LB);
		cost.put(AdoptAgentCycle_2.KEY_UB, UB);

		Message msg = new Message(this.id, this.parent,
				AdoptAgentCycle_2.TYPE_COST_MESSAGE, cost);
		this.sendMessage(constructNcccMessage(msg));
		
		
	}
	
	private void sendValueMessages(){
		if (this.isLeafAgent() == true) {
			return;
		}

		int ChildId = 0;
		
		for (int i = 0; i < this.children.length; i++) {
			int[] val = new int[3];
			val[0] = valueIndex;
			val[1] = valueID;
			ChildId = children[i];
			val[2] = ths.get(ChildId)[valueIndex];
			Message msg = new Message(this.id, ChildId,
					AdoptAgentCycle_2.TYPE_VALUE_MESSAGE, val);
			this.sendMessage(constructNcccMessage(msg));
		}
		if (NoPseudoChild() == true) {
			return;
		}
		int pseudoChildId=0;
		for (int i = 0; i < this.pseudoChildren.length; i++) {
			int[] val = new int[3];
			val[0] = valueIndex;
			val[1] = valueID;
			val[2] = -1;   // a virtural th
			pseudoChildId = this.pseudoChildren[i];
			Message msg = new Message(this.id, pseudoChildId,
					AdoptAgentCycle_2.TYPE_VALUE_MESSAGE,val);
			this.sendMessage(constructNcccMessage(msg));
		}
		
		
	}
	
	private boolean NoPseudoChild() {
		return this.pseudoChildren == null;
	}
		
	private void InitSelf(){
		
		TH=0;
		//int oldvalueIndex=this.valueIndex;
		valueIndex=this.computeMinimalLBAndUB()[0];
		//if(oldvalueIndex!=this.valueIndex||this.valueID==0)
		valueID = valueID + 1;
		Debugger.valueChanges.get(this.name).add(valueIndex);			
	}
	
	private void InitChild(int child,int d)
	{
		if(this.isLeafAgent()==false)
		{
				lbs.get(child)[d] = 0;
				ubs.get(child)[d] = Infinity.INFINITY;
				ths.get(child)[d] = 0;
				contexts.get(child)[d].reset();
		}
	}
	
	private void InitChild() {
		if (this.isLeafAgent() == false) {
			int childId = 0;
			for (int i = 0; i < this.children.length; i++) {
				childId = this.children[i];
				int[] childLbs = new int[this.domain.length];
				int[] childUbs = new int[this.domain.length];
				int[] childThs = new int[this.domain.length];
				Context[] childContexts = new Context[this.domain.length];
				for (int j = 0; j < this.domain.length; j++) {
					childLbs[j] = 0;
					childUbs[j] = Infinity.INFINITY;
					childThs[j] = 0;
					childContexts[j] = new Context();
				}
				lbs.put(childId, childLbs);
				ubs.put(childId, childUbs);
				ths.put(childId, childThs);
				contexts.put(childId, childContexts);
			}
		}
	}
	
	private void merge(Context c) {
		currentContext.union(c);
		//Context temp = new Context(c);
		//temp.remove(agent.neighbours);
		//agent.currentContext.union(temp);
	}

	private void checkCompatible() {  //两个方法可以用一个吗？
		if (this.isLeafAgent() == true) {
			return;
		}
		int childId = 0;
		for (int i = 0; i < this.children.length; i++) {
			childId = this.children[i];
			for (int j = 0; j < this.domain.length; j++) {
				if (contexts.get(childId)[j].compatible(currentContext) == false) {
					InitChild(childId,j);
				}
			}
		}
	}
	
	private boolean checkCompatible(Context c1,Context c2){
		return c1.compatible(c2);	
		}

	private void backtrack() {
		int[] ret = computeMinimalLBAndUB();
		int dMinimizesLB = ret[0];
		int LB_CurValue = ret[1];
		int dMinimizesUB = ret[2];
		
		//do nccc local here
	    increaseNcccLocal();
	    
	   
	    maintainThresholdInvariant();	    
	    int oldValue=valueIndex;
	    
		if (TH == UB) {
			if (valueIndex != dMinimizesUB) {
				Debugger.valueChanges.get(this.name).add(dMinimizesUB);
			}

			valueIndex = dMinimizesUB;
			if(valueIndex!=oldValue)
			valueID = valueID + 1;
		} else if (LB_CurValue > TH) {
			if (valueIndex != dMinimizesLB) {
				Debugger.valueChanges.get(this.name).add(dMinimizesLB);
			}

			valueIndex = dMinimizesLB;
			if(valueIndex!=oldValue)
				valueID = valueID + 1;
		}

		System.out.println("agent"+id+": "+valueIndex+"\t"+valueID+"\t"+TH+"\t"+LB+"\t"+UB);
		maintainChildThresholdInvariant();
		maintainAllocationInvariant();   //必须将这个放在发送VALUE信息之前
		sendValueMessages();
		if (TH == UB) {
			if (Readytermintate == true
					|| this.isRootAgent() == true) {
				sendTerminateMessages();
				this.stopRunning();
			}
		}
		sendCostMessage();
	}

}
