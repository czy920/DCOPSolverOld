package com.cqu.bnbadopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.MessageNCCC;
import com.cqu.core.ResultAdopt;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class ADOPT_K extends AgentCycle {
	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_COST_MESSAGE = 1;
	public final static int TYPE_TERMINATE_MESSAGE = Message.TYPE_TERMINATE_MESSAGE;

	public final static String KEY_CONTEXT = "KEY_CONTEXT";
	public final static String KEY_LB = "KEY_LB";
	public final static String KEY_UB = "KEY_UB";
	public final static String KEY_TH_A = "KEY_TH_A";
	public final static String KEY_TH_B = "KEY_TH_B";
	public final static String KEY_NCCC = "KEY_NCCC";

	//public final static String KEY_VALUE_MESSAGE = "KEY_VALUE_MESSAGE";

	private Map<Integer, int[]> lbs;
	private Map<Integer, int[]> ubs;
	private Map<Integer, int[]> ths;
	private int LB;
	private int UB;
	private int TH_A;
	private int TH_B;
	private int TH_curr;
	private int UB_curr;
	private int LB_curr;

	private Map<Integer, Context[]> contexts;
	private Context currentContext;
	private boolean terminateReceivedFromParent = false;

	private int valueID;
	private boolean Readytermintate = false;
	private int K;

	private int nccc;

	public ADOPT_K(int id, String name, int level, int[] domain,int k) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		this.K=k;
		this.nccc = 0;
	}

	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();

		valueID = 0;
		currentContext = new Context();
		if (!this.isRootAgent())
			currentContext.addOrUpdate(this.parent, 0, 1); // 仅仅初始化为第1个取值
		if (this.pseudoParents != null) {
			for (int pseudoP : this.pseudoParents) {
				currentContext.addOrUpdate(pseudoP, 0, 1); // 仅仅初始化为第1个取值
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
	
	private void maintainTHInvariant()
	{
		if(this.TH_A<this.LB)
		{
			this.TH_A=this.LB;
		}
		if(this.TH_A>this.UB)
		{
			this.TH_A=this.UB;
		}
	}
	 
	private void maintainCurrentTHInvariant(){
		this.UB_curr=computeCurrentUB();
		this.LB_curr=computeCurrentLB();
		this.TH_curr=this.TH_A;
		if(this.TH_curr<this.LB_curr)
			this.TH_curr=this.LB_curr;
		if(this.TH_curr>this.UB_curr)
			this.TH_curr=this.UB_curr;
	}
	
	private void maintainAllocationInvariant(){
		if(this.isLeafAgent()==true)
		{
			return;
		}
		int diff=this.TH_curr-computeTH1(valueIndex);
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

	private void backtrack() {
		int[] compute = computeMinimalLBAndUB();
		this.increaseNcccLocal();
		int oldValueIndex = valueIndex;
		maintainTHInvariant();
		if (this.Readytermintate) {
			if (this.TH_A == this.UB||this.TH_B==this.UB) {
				this.valueIndex = compute[2];			
			} else if (compute[1] > this.TH_A + (this.K - 1)) {
				this.valueIndex = compute[0];	
			} else if (compute[1] >= Math.min(this.TH_B, this.UB)){
				this.valueIndex = compute[0];
			}
		} else {
			if (this.TH_A == this.UB) {
				this.valueIndex = compute[2];
				if (compute[1] >= Math.min(this.TH_B, this.UB)){
					this.valueIndex = compute[0];
				}		
			} else if (compute[1] > this.TH_A + this.K) {
				this.valueIndex = compute[0];
			}else if (compute[1] >= Math.min(this.TH_B, this.UB)){
				this.valueIndex = compute[0];
			}	
		}
		if(valueIndex!=oldValueIndex){
			valueID++;
			Debugger.valueChanges.get(this.name).add(valueIndex);
		}
		this.maintainChildThresholdInvariant();
		this.maintainCurrentTHInvariant();
		this.maintainAllocationInvariant();
		System.out.println("agent"+this.id+": "+this.valueIndex+"\t"+this.valueID+"\t"+this.TH_A+"\t"+this.TH_B+"\t"+this.LB+"\t"+this.UB);
		if(this.TH_A==this.UB||this.TH_B==this.UB)
			if(this.isRootAgent()||this.Readytermintate==true){
				System.out.println("agent"+this.id+": "+this.valueIndex+"\t"+this.valueID+"\t"+this.TH_A+"\t"+this.TH_B+"\t"+this.LB+"\t"+this.UB);
				sendTerminateMessages();
				this.stopRunning();
			}
		sendValueMessages();
		sendCostMessage();
	}

	private void sendCostMessage() {
		if(this.isRootAgent()==true)
		{
			return;
		}
		
		Map<String, Object> cost=new HashMap<String, Object>();
		Context context=new Context(currentContext);
		context.Remove(this.id);
		cost.put(ADOPT_K.KEY_CONTEXT, context);
		cost.put(ADOPT_K.KEY_LB, LB);
		cost.put(ADOPT_K.KEY_UB, UB);
		
		Message msg=new Message(this.id, this.parent, ADOPT_K.TYPE_COST_MESSAGE, cost);
		this.sendMessage(this.constructNcccMessage(msg));
		
	}

	private void sendTerminateMessages() {
		if(this.isLeafAgent()==true)
		{
			return;
		}
	    this.terminateReceivedFromParent=true;
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];		
			Map<String, Object> mapValue=new HashMap<String, Object>();
			//Context c=new Context(currentContext);
			//mapValue.put(KEY_CONTEXT, c);
			
			Message msg=new Message(this.id, childId, ADOPT_K.TYPE_TERMINATE_MESSAGE, mapValue);
			this.sendMessage(this.constructNcccMessage(msg));
		}			
		
	}

	private void sendValueMessages() {
		if (this.isLeafAgent() == true && this.NoPseudoChild() == true) {
			return;
		}
		if(this.isLeafAgent() ==false)
		{
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			int[] val = new int[4];  //一个取值，一个ID，一个TH
			val[0]=valueIndex;
			val[1]=valueID;
			childId=this.children[i];
			val[2]=ths.get(childId)[valueIndex];
			val[3]=computeTH2(valueIndex,childId);
			Message msg=new Message(this.id, childId, ADOPT_K.TYPE_VALUE_MESSAGE, val);
			this.sendMessage(this.constructNcccMessage(msg));
		}
		}
		if(this.NoPseudoChild()==false)
		{
		int pseudoChildId=0;
		for(int i=0;i<this.pseudoChildren.length;i++)
		{
			pseudoChildId=this.pseudoChildren[i];
			int[] val = new int[4];  //一个取值，一个ID，一个TH
			val[0]=valueIndex;
			val[1]=valueID;
			val[2]=Infinity.INFINITY;
			val[3]=Infinity.INFINITY;
			Message msg=new Message(this.id, pseudoChildId, ADOPT_K.TYPE_VALUE_MESSAGE, val);
			this.sendMessage(this.constructNcccMessage(msg));
			}
		}
		
	}

	private boolean NoPseudoChild() {
		return this.pseudoChildren==null;
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

	private void InitChild(int child, int d) {
		if(this.isLeafAgent()==false)
		{
				lbs.get(child)[d] = 0;
				ubs.get(child)[d] = Infinity.INFINITY;
				ths.get(child)[d] = 0;
				contexts.get(child)[d].reset();
		}
	}

	void InitSelf() {

		int oldvalueIndex=this.valueIndex;
		valueIndex = this.computeMinimalLBAndUB()[0];
		if(oldvalueIndex!=this.valueIndex||this.valueID==0)
		valueID = valueID + 1;
		TH_A=0;
		TH_B = Infinity.INFINITY;
		Debugger.valueChanges.get(this.name).add(valueIndex);

	}

	@Override
	public Object printResults(List<Map<String, Object>> results) {
		int totalCost = -1;
		int maxNccc = 0;
		for (Map<String, Object> result : results) {
			int id_ = (Integer) result.get(ADOPT_K.KEY_ID);
			String name_ = (String) result.get(ADOPT_K.KEY_NAME);
			int value_ = (Integer) result.get(ADOPT_K.KEY_VALUE);
			int LB_ = (Integer) result.get(ADOPT_K.KEY_LB);
			int UB_ = (Integer) result.get(ADOPT_K.KEY_UB);
			int TH_A = (Integer) result.get(ADOPT_K.KEY_TH_A);
			int TH_B = (Integer) result.get(ADOPT_K.KEY_TH_B);
			int ncccTemp = (Integer) result.get(ADOPT_K.KEY_NCCC);
				
			if (maxNccc < ncccTemp) {
				maxNccc = ncccTemp;
			}
			if (totalCost < UB_) {
				totalCost = UB_;
			}

			String displayStr = "Agent " + name_ + ": id=" + id_ + " value="
					+ value_ + " LB=" + LB_ + " UB=";
			displayStr += Infinity.infinityEasy(UB_);
			displayStr += " TH_A=" + Infinity.infinityEasy(TH_A);
			displayStr += " TH_B=" + Infinity.infinityEasy(TH_B);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: " + Infinity.infinityEasy(totalCost)
				+ " NCCC: " + maxNccc);

		ResultAdopt ret = new ResultAdopt();
		ret.totalCost = totalCost;
		ret.nccc = maxNccc;
		return ret;
	}
	
	@Override
	protected void runFinished() {
	
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(ADOPT_K.KEY_ID, this.id);
		result.put(ADOPT_K.KEY_NAME, this.name);
		result.put(ADOPT_K.KEY_VALUE, this.domain[valueIndex]);
		result.put(ADOPT_K.KEY_LB, this.LB);
		result.put(ADOPT_K.KEY_UB, this.UB);
		result.put(ADOPT_K.KEY_TH_A, this.TH_A);
		result.put(ADOPT_K.KEY_TH_B, this.TH_B);
		result.put(ADOPT_K.KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		return "from " + sender.getName() + " to " + receiver.getName()
				+ " type " + ADOPT_K.messageContent(msg);
	}

	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg) {
		switch (msg.getType()) {
		case ADOPT_K.TYPE_VALUE_MESSAGE: {
			int[] val = (int[]) msg.getValue();
			return "value[" + val[0] + "]";
		}
		case ADOPT_K.TYPE_COST_MESSAGE: {
			Map<String, Object> msgValue = (Map<String, Object>) msg.getValue();
			int LB_ = (Integer) msgValue.get(KEY_LB);
			int UB_ = (Integer) msgValue.get(KEY_UB);
			Context c = (Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB=" + LB_ + " UB=" + Infinity.infinityEasy(UB_)
					+ " context=" + c.toString() + "]";
		}
		case ADOPT_K.TYPE_TERMINATE_MESSAGE: {
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}

	private void increaseNcccLocal() {
		this.nccc++;
	}

	private void increaseNcccFromMessage(MessageNCCC mn) {
		int t = Settings.settings.getCommunicationNCCCInAdopts();
		this.nccc = Math.max(mn.getNccc() + t, this.nccc);
	}

	private Message constructNcccMessage(Message msg) {
		return new MessageNCCC(msg, this.nccc);
	}

	@Override
	protected void disposeMessage(Message msg) {
		if (Debugger.debugOn == true) {
			System.out.println(Thread.currentThread().getName()
					+ ": message got in agent " + this.name + " "
					+ this.msgMailer.easyMessageContent(msg) + " | VALUE="
					+ this.domain[valueIndex] + " LB=" + this.LB + " UB="
					+ Infinity.infinityEasy(this.UB) + " TH_A="
					+ Infinity.infinityEasy(this.TH_A) + " TH_B="
					+ Infinity.infinityEasy(this.TH_B));
		}

		// do nccc message here
		this.increaseNcccFromMessage((MessageNCCC) msg);

		int type = msg.getType();
		if (type == ADOPT_K.TYPE_VALUE_MESSAGE) {
			disposeValueMessage(msg);
		} else if (type == ADOPT_K.TYPE_COST_MESSAGE) {
			disposeCostMessage(msg);
		} else if (type == ADOPT_K.TYPE_TERMINATE_MESSAGE) {
			disposeTerminateMessage(msg);
		}

	}

	private void disposeTerminateMessage(Message msg) {
		Readytermintate = true;
		if (this.msgQueue.isEmpty())
			backtrack();
	}

	@SuppressWarnings("unchecked")
	private void disposeCostMessage(Message msg) {
		Map<String, Object> cost = (Map<String, Object>) msg.getValue();
		Context c = (Context) cost.get(ADOPT_K.KEY_CONTEXT);
		int myValueIndex = c.get(this.id);

		if (myValueIndex == -1) {
			return;
		}
		Context temp = new Context(currentContext);
		merge(c);
		currentContext.Remove(this.id); // 因为合并时将自己的取值加入，应该移除

		if (!checkCompatible(currentContext, temp)) { // 不兼容表示引入了新的内容
			checkCompatible();

		}
		if (checkCompatible(c, currentContext)) { // 兼容表示这个信息可以利用
			if (lbs.get(msg.getIdSender())[myValueIndex] < (Integer) cost
					.get(ADOPT_K.KEY_LB))
				lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(ADOPT_K.KEY_LB);
			if (ubs.get(msg.getIdSender())[myValueIndex] > (Integer) cost
					.get(ADOPT_K.KEY_UB))
				ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(ADOPT_K.KEY_UB);
			contexts.get(msg.getIdSender())[myValueIndex] = c;
		}

		if (!checkCompatible(currentContext, temp)) {
			InitSelf();

		}
		if (this.msgQueue.isEmpty() == true)
			backtrack();

	}

	private void merge(Context c) {
		currentContext.union(c);

	}

	private void checkCompatible() {
		if (this.isLeafAgent() == true) {
			return;
		}
		int childId = 0;
		for (int i = 0; i < this.children.length; i++) {
			childId = this.children[i];
			for (int j = 0; j < this.domain.length; j++) {
				if (contexts.get(childId)[j].compatible(currentContext) == false) {
					InitChild(childId, j);
				}
			}
		}

	}

	private boolean checkCompatible(Context c1, Context c2) {
		return c1.compatible(c2);
	}

	private void disposeValueMessage(Message msg) {
		if(this.terminateReceivedFromParent==false)
		{
			Context temp = new Context(currentContext);
			int[] val =(int[]) msg.getValue();
			currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
			
			if(!checkCompatible(currentContext,temp))
			{
				checkCompatible();
				InitSelf();
			}
			if(msg.getIdSender() == this.parent)
			{
				TH_A=val[2];
				TH_B=val[3];
			}
			if(this.msgQueue.isEmpty()==true)backtrack();
		}

	}
	
	private int computeCurrentUB(){
		int UBTEMP=this.localCost(valueIndex);
		int childId,sumub=0;
		for (int j = 0; j < this.children.length; j++) {
			childId = this.children[j];
			sumub = Infinity.add(sumub, this.ubs.get(childId)[valueIndex]);
		}
		UBTEMP=Infinity.add(sumub, UBTEMP);
		return UBTEMP;
	}
	
	private int computeCurrentLB(){
		int LBTEMP=this.localCost(valueIndex);
		int childId,sumlb=0;
		for (int j = 0; j < this.children.length; j++) {
			childId = this.children[j];
			sumlb += this.lbs.get(childId)[valueIndex];
		}
		LBTEMP=Infinity.add(sumlb, LBTEMP);
		return LBTEMP;
	}

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
					ret[j] += this.constraintCosts.get(parentId)[j][oppositeAgentValueIndex];
				}
			}
		}
		return ret;
	}
	
	private int computeTH1(int di)
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
	
	private int computeTH2(int di,int child)
	{
		int localCost_=localCost(di);
		
		if(this.isLeafAgent()==true)
		{
			return localCost_;
		}
		
		int TH_di=0;
		int childId=0;
		if(this.Readytermintate&&this.TH_B==this.UB){
			for(int i=0;i<this.children.length;i++)
			{
				childId=this.children[i];
				if(childId!=child)TH_di=TH_di+this.ubs.get(childId)[di];
			}
		} else {
			for (int i = 0; i < this.children.length; i++) {
				childId = this.children[i];
				if (childId != child)
					TH_di = TH_di + this.lbs.get(childId)[di];
			}
		}
		TH_di=Infinity.add(TH_di, localCost_);
		
		//return (TH2>UB)?(UB-TH_di):(TH2-TH_di);
		return Infinity.minus(Math.min(TH_B, UB),TH_di);
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
				ret += this.constraintCosts.get(parentId)[di][oppositeAgentValueIndex];
			}
		}
		return ret;
	}

	@Override
	protected void messageLost(Message msg) {
		if (Debugger.debugOn == true) {
			System.out.println(Thread.currentThread().getName()
					+ ": message lost in agent " + this.name + " "
					+ this.msgMailer.easyMessageContent(msg));
		}

	}

}
