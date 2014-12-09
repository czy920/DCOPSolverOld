package com.cqu.bnbmergeadopt;

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

public class AgentModel extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_COST_MESSAGE = 1;
	//public final static int TYPE_THRESHOLD_MESSAGE = 2;
	public final static int TYPE_TERMINATE_MESSAGE = Message.TYPE_TERMINATE_MESSAGE;

	public final static String KEY_CONTEXT = "KEY_CONTEXT";
	public final static String KEY_LB = "KEY_LB";
	public final static String KEY_UB = "KEY_UB";
	public final static String KEY_TH = "KEY_TH";
	public final static String KEY_NCCC="KEY_NCCC";
	public final static  String STRATEGY ="STRATEGY";

	public final static String KEY_VALUE_MESSAGE = "KEY_VALUE_MESSAGE";
	

	private Map<Integer, int[]> lbs;
	private Map<Integer, int[]> ubs;
	private Map<Integer, int[]> ths;
	private int LB;
	private int UB;
	private int TH;

	private Map<Integer, Context[]> contexts;
	private Context currentContext;

	//private String strategy;
	private String typeMethod;
	private long boundary;    //方法的分界，可以由比例求得，这里设为3
	private double scaleArg;    //两个方法的比例参数

	private int valueID;
	private boolean terminateReceivedFromParent = false;
	private boolean Readytermintate = false;
	
	private int nccc;

	private Method method;
	
	public AgentModel(int id, String name, int level, int[] domain,long treeDepth) {
		super(id, name, level, domain);
		this.nccc = 0;
		
		//this.boundary=Settings.settings.getBNBmergeADOPTboundArg();	//初始为2 
		this.scaleArg=Settings.settings.getBNBmergeADOPTboundArg();
		this.boundary=(long) Math.ceil(treeDepth*this.scaleArg);
		
		// a sole bnbadopt
//		method = new BnBMethod(this);
//		typeMethod = "bnbadopt";
//		this.strategy="bnbadopt";

		// a sole adopt
//		 method=new AdoptMethod(this);
//		 typeMethod="adopt";
//		 strategy="adopt";


		// union adopt and bnbadopt
		if (this.level < this.boundary) { // 前三层用bnbadopt策略
			method = new BnBMethod(this);
			typeMethod = "bnbadopt";
		} else { // 其他层用adopt策略
			method=new AdoptMethod(this);
			typeMethod = "adopt";
		}		 
		 //strategy="bnbandadopt";
	}

	@Override
	protected void initRun() {	
		super.initRun();
		method.initRun();
	}

	public boolean NoPseudoChild() {
		return this.pseudoChildren == null;
	}

	// return int[]{dMinimizesLB, LB(curValue), dMinimizesUB}
	protected int[] computeMinimalLBAndUB() {
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

	protected int localCost(int di)
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

	@Override
	protected void disposeMessage(Message msg) {

		if (Debugger.debugOn == true) {
			System.out.println(Thread.currentThread().getName()
					+ ": message got in agent " + this.name + " "
					+ this.msgMailer.easyMessageContent(msg) + " | VALUE="
					+ this.domain[valueIndex] + " LB=" + this.LB + " UB="
					+ Infinity.infinityEasy(this.UB) + " TH="
					+ Infinity.infinityEasy(this.TH));
		}
		
		//do nccc message here
		this.increaseNcccFromMessage((MessageNCCC)msg);

		if (msg.getType() == AgentModel.TYPE_VALUE_MESSAGE) {
			method.disposeValueMessage(msg);
		} else if (msg.getType() == AgentModel.TYPE_COST_MESSAGE) {
			method.disposeCostMessage(msg);
		} 
//		else if (msg.getType() == AgentModel.TYPE_THRESHOLD_MESSAGE) {
//			method.disposeThresholdMessage(msg);
//		} 
		else if (msg.getType() == AgentModel.TYPE_TERMINATE_MESSAGE) {
			method.disposeTerminateMessage(msg);
		}
	}

	@Override
	public Object printResults(List<Map<String, Object>> results) {

		int totalCost = -1;
		int maxNccc=0;
		for (Map<String, Object> result : results) {
			int id_ = (Integer) result.get(AgentModel.KEY_ID);
			String name_ = (String) result.get(AgentModel.KEY_NAME);
			int value_ = (Integer) result.get(AgentModel.KEY_VALUE);
			int LB_ = (Integer) result.get(AgentModel.KEY_LB);
			int UB_ = (Integer) result.get(AgentModel.KEY_UB);
			int TH_ = (Integer) result.get(AgentModel.KEY_TH);
			int ncccTemp=(Integer) result.get(AgentModel.KEY_NCCC);
			if(maxNccc<ncccTemp)
			{
				maxNccc=ncccTemp;
			}
			if (totalCost == -1) {
				totalCost = UB_;
			}

			String displayStr = "Agent " + name_ + ": id=" + id_ + " value="
					+ value_ + " LB=" + LB_ + " UB=";
			displayStr += Infinity.infinityEasy(UB_);
			displayStr += " TH=" + Infinity.infinityEasy(TH_);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: " + Infinity.infinityEasy(totalCost)+" NCCC: "+maxNccc);
		
		ResultAdopt ret=new ResultAdopt();
		ret.totalCost=totalCost;
		ret.nccc=maxNccc;
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {

		return "from " + sender.getName() + " to " + receiver.getName()
				+ " type " + AgentModel.messageContent(msg);
	}

	@Override
	protected void runFinished() {

		super.runFinished();

		Map<String, Object> result = new HashMap<String, Object>();
		result.put(AgentModel.KEY_ID, this.id);
		result.put(AgentModel.KEY_NAME, this.name);
		result.put(AgentModel.KEY_VALUE, this.domain[valueIndex]);
		result.put(AgentModel.KEY_LB, this.LB);
		result.put(AgentModel.KEY_UB, this.UB);
		result.put(AgentModel.KEY_TH, this.TH);
		result.put(AgentModel.KEY_NCCC, this.nccc);
		//result.put(AgentModel.STRATEGY, this.strategy);
		
		this.msgMailer.setResult(result);

	    System.out.println("Agent "+this.name+" stopped!");
	}

	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg) {
		switch (msg.getType()) {
		case AgentModel.TYPE_VALUE_MESSAGE: {
			int[] valueIndex = (int[]) msg.getValue();
			return "value[" + valueIndex[0] + "]";
		}
		case AgentModel.TYPE_COST_MESSAGE: {
			Map<String, Object> msgValue = (Map<String, Object>) msg.getValue();
			int LB_ = (Integer) msgValue.get(KEY_LB);
			int UB_ = (Integer) msgValue.get(KEY_UB);
			Context c = (Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB=" + LB_ + " UB=" + UB_ + " context=" + c.toString()
					+ "]";
		}
//		case AgentModel.TYPE_THRESHOLD_MESSAGE: {
//			Map<String, Object> msgValue = (Map<String, Object>) msg.getValue();
//			int TH_ = (Integer) msgValue.get(KEY_TH);
//			Context c = (Context) msgValue.get(KEY_CONTEXT);
//			return "threshold[TH=" + TH_ + " context=" + c.toString() + "]";
//		}
		case AgentModel.TYPE_TERMINATE_MESSAGE: {
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}
	
	@Override
	protected void messageLost(Message msg) {
		if (Debugger.debugOn == true) {
			System.out.println(Thread.currentThread().getName()
					+ ": message lost in agent " + this.name + " "
					+ this.msgMailer.easyMessageContent(msg));
		}
	}
	
	private void increaseNcccLocal()
	{
		this.nccc++;
	}
	
	private void increaseNcccFromMessage(MessageNCCC mn)
	{
		int t=Settings.settings.getCommunicationNCCCInAdopts();
		this.nccc=Math.max(mn.getNccc()+t, this.nccc);
	}
	
	private Message constructNcccMessage(Message msg)
	{
		return new MessageNCCC(msg, this.nccc);
	}
	
	
	
	public abstract class Method {

		protected abstract void disposeValueMessage(Message msg);

		protected abstract void disposeCostMessage(Message msg);

		// public void disposeThresholdMessage(Message msg);

		protected abstract void disposeTerminateMessage(Message msg);

		protected abstract void initRun();

		protected abstract void merge(Context c);

		protected abstract void sendValueMessages();

		protected abstract void sendCostMessage();

		protected abstract void sendTerminateMessages();

		// public abstracted void sendThresholdMessages();
	}

	
	
	public class AdoptMethod extends Method {

		private AgentModel agent;
		AdoptMethod(AgentModel agent) {
			this.agent=agent;
		}

		private void InitSelf(){
			
			agent.TH=0;
			//int oldvalueIndex=this.valueIndex;
			agent.valueIndex=agent.computeMinimalLBAndUB()[0];
			//if(oldvalueIndex!=this.valueIndex||this.valueID==0)
			agent.valueID = agent.valueID + 1;
			Debugger.valueChanges.get(agent.name).add(agent.valueIndex);			
		}
		
		private void InitChild(int child,int d)
		{
			if(agent.isLeafAgent()==false)
			{
					agent.lbs.get(child)[d] = 0;
					agent.ubs.get(child)[d] = Infinity.INFINITY;
					agent.ths.get(child)[d] = 0;
					agent.contexts.get(child)[d].reset();
			}
		}
		
		private void InitChild() {
			if (agent.isLeafAgent() == false) {
				int childId = 0;
				for (int i = 0; i < agent.children.length; i++) {
					childId = agent.children[i];
					int[] childLbs = new int[agent.domain.length];
					int[] childUbs = new int[agent.domain.length];
					int[] childThs = new int[agent.domain.length];
					Context[] childContexts = new Context[agent.domain.length];
					for (int j = 0; j < agent.domain.length; j++) {
						childLbs[j] = 0;
						childUbs[j] = Infinity.INFINITY;
						childThs[j] = 0;
						childContexts[j] = new Context();
					}
					agent.lbs.put(childId, childLbs);
					agent.ubs.put(childId, childUbs);
					agent.ths.put(childId, childThs);
					agent.contexts.put(childId, childContexts);
				}
			}
		}
		
		public void initRun() {

			agent.TH = 0;
			agent.valueID = 0;
			agent.currentContext = new Context();
			
			if(!agent.isRootAgent())
				agent.currentContext.addOrUpdate(agent.parent, 0, 0); //仅仅初始化为第1个取值
			if(agent.pseudoParents!=null)
			{
				for(int pseudoP:agent.pseudoParents){
				    agent.currentContext.addOrUpdate(pseudoP, 0, 0); //仅仅初始化为第1个取值
				}
			}

			agent.lbs = new HashMap<Integer, int[]>();
			agent.ubs = new HashMap<Integer, int[]>();
			agent.ths = new HashMap<Integer, int[]>();
			agent.contexts = new HashMap<Integer, Context[]>();
			InitChild();
			InitSelf();
			backtrack();
		}
		
		protected void merge(Context c) {
			agent.currentContext.union(c);
			//Context temp = new Context(c);
			//temp.remove(agent.neighbours);
			//agent.currentContext.union(temp);
		}

		private void checkCompatible() {  //两个方法可以用一个吗？
			if (agent.isLeafAgent() == true) {
				return;
			}
			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
				for (int j = 0; j < agent.domain.length; j++) {
					if (agent.contexts.get(childId)[j].compatible(agent.currentContext) == false) {
						InitChild(childId,j);
					}
				}
			}
		}
		
		private boolean checkCompatible(Context c1,Context c2){
			return c1.compatible(c2);	
			}

		@Override
		public void sendValueMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int ChildId = 0;
			
			for (int i = 0; i < agent.children.length; i++) {
				int[] val = new int[3];
				val[0] = agent.valueIndex;
				val[1] = agent.valueID;
				ChildId = agent.children[i];
				val[2] = agent.ths.get(ChildId)[agent.valueIndex];
				Message msg = new Message(agent.getId(), ChildId,
						AgentModel.TYPE_VALUE_MESSAGE, val);
				agent.sendMessage(this.agent.constructNcccMessage(msg));
			}
			if (agent.NoPseudoChild() == true) {
				return;
			}
			int pseudoChildId=0;
			for (int i = 0; i < agent.pseudoChildren.length; i++) {
				int[] val = new int[3];
				val[0] = agent.valueIndex;
				val[1] = agent.valueID;
				val[2] = -1;   // a virtural th
				pseudoChildId = agent.pseudoChildren[i];
				Message msg = new Message(agent.getId(), pseudoChildId,
						AgentModel.TYPE_VALUE_MESSAGE,val);
				agent.sendMessage(this.agent.constructNcccMessage(msg));
			}
		}

		@Override
		protected void sendCostMessage() {
			if (agent.isRootAgent() == true) {
				return;
			}

			Map<String, Object> cost = new HashMap<String, Object>();
			Context context = new Context(agent.currentContext);
			context.Remove(agent.getId());    
			cost.put(AgentModel.KEY_CONTEXT, new Context(context));
			cost.put(AgentModel.KEY_LB, agent.LB);
			cost.put(AgentModel.KEY_UB, agent.UB);

			Message msg = new Message(agent.getId(), agent.parent,
					AgentModel.TYPE_COST_MESSAGE, cost);
			agent.sendMessage(agent.constructNcccMessage(msg));
		}

		//don't been used instead of inputing the th into value message
//		@Override
//		protected void sendThresholdMessages() {
//			if (agent.isLeafAgent() == true) {
//				return;
//			}
//
//			int childId = 0;
//			for (int i = 0; i < agent.children.length; i++) {
//				childId = agent.children[i];
//				Map<String, Object> thresh = new HashMap<String, Object>();
//				thresh.put(AgentModel.KEY_CONTEXT,
//						new Context(agent.currentContext));
//				thresh.put(AgentModel.KEY_TH,
//						agent.ths.get(childId)[agent.valueIndex]);
//
//				Message msg = new Message(agent.getId(), childId,
//						AgentModel.TYPE_THRESHOLD_MESSAGE, thresh);
//				agent.sendMessage(agent.constructNcccMessage(msg));
//			}
//		}

		@Override
		protected void sendTerminateMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}
			agent.terminateReceivedFromParent=true;
			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
//				int[] val = new int[3];
//				val[0] = agent.valueIndex;
//				val[1] = agent.valueID;
//				val[2] = agent.ths.get(childId)[agent.valueIndex];
//				Message valueMsg = new Message(agent.getId(), childId,
//						AgentModel.TYPE_VALUE_MESSAGE,val);

				Map<String, Object> mapValue = new HashMap<String, Object>();
//				Context c = new Context(agent.currentContext);
//				c.addOrUpdate(agent.id, agent.valueIndex, agent.valueID);
//				mapValue.put(AgentModel.KEY_CONTEXT, c);
//				mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

				Message msg = new Message(agent.getId(), childId,
						AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
				agent.sendMessage(agent.constructNcccMessage(msg));
			}

			//不发送终止给伪孩子
//			if (agent.pseudoChildren != null) {  
//				int pseudoChildId = 0;
//				for (int i = 0; i < agent.pseudoChildren.length; i++) {
//					pseudoChildId = agent.pseudoChildren[i];
//					int[] val = new int[3];
//					val[0] = agent.valueIndex;
//					val[1] = agent.valueID;
//					val[2] = -1;  //a virtual th
//					Message valueMsg = new Message(agent.getId(), pseudoChildId,
//							AgentModel.TYPE_VALUE_MESSAGE, val);
//
//					Message msg = new Message(agent.getId(), pseudoChildId,
//							AgentModel.TYPE_TERMINATE_MESSAGE, valueMsg);
//					agent.sendMessage(agent.constructNcccMessage(msg));
//				}
//			}
		}

		@Override
		public void disposeValueMessage(Message msg) {   //阈值一起处理
			if (agent.terminateReceivedFromParent == false) {
				Context temp = new Context(agent.currentContext);
				int[] val =(int[]) msg.getValue();
				agent.currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
				
				if(!checkCompatible(agent.currentContext,temp))
				{
					checkCompatible();
					InitSelf();
				}
				if (msg.getIdSender() == agent.parent && val[2] != (-1)) 
					agent.TH = val[2];
				//maintainThresholdInvariant();  //放在backtrack函数里面一次性做
				if(agent.msgQueue.isEmpty())backtrack();
			}
		}

		@SuppressWarnings("unchecked")
		public void disposeCostMessage(Message msg) {
			Map<String, Object> cost=(Map<String, Object>) msg.getValue();
			Context c = (Context) cost.get(AgentModel.KEY_CONTEXT);
			int myValueIndex = c.get(agent.id);

			if (myValueIndex == -1) {
				return;
			}			

			Context temp = new Context(agent.currentContext);
			if (agent.terminateReceivedFromParent == false) {
				merge(c);
				agent.currentContext.Remove(agent.id);   //因为合并时将自己的取值加入，应该移除
				if (!checkCompatible(agent.currentContext, temp)) {  //不兼容表示引入了新的内容
					checkCompatible();
				}
			}
			if (checkCompatible(c,agent.currentContext) == true) {
				if (agent.lbs.get(msg.getIdSender())[myValueIndex] < (Integer) cost
						.get(AgentModel.KEY_LB))
					agent.lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
							.get(AgentModel.KEY_LB);
				if (agent.ubs.get(msg.getIdSender())[myValueIndex] > (Integer) cost
						.get(AgentModel.KEY_UB))
					agent.ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
							.get(AgentModel.KEY_UB);
				agent.contexts.get(msg.getIdSender())[myValueIndex] = c;
				//maintainChildThresholdInvariant();
				//maintainThresholdInvariant();
			}
			if (!checkCompatible(agent.currentContext, temp)) {
				InitSelf();
			}
			if(agent.msgQueue.isEmpty())backtrack();

		}

		//don't been used instead of simple handle during disposing value message
//		@SuppressWarnings("unchecked")
//		public void disposeThresholdMessage(Message msg) {
//			Map<String, Object> thresh = (Map<String, Object>) msg.getValue();
//			if (((Context) thresh.get(AgentModel.KEY_CONTEXT))
//					.compatible(agent.currentContext) == true) {
//				agent.TH = (Integer) thresh.get(AgentModel.KEY_TH);
//				maintainThresholdInvariant();
//				backtrack();
//			}
//		}

		public void disposeTerminateMessage(Message msg) {
			
			agent.Readytermintate=true;
			if(agent.msgQueue.isEmpty())backtrack();
//			Message valueMsg = null;
			//父agent发过来的terminate消息为包含了基本的terminate消息和value消息
//			if (msg.getIdSender() == agent.parent) {
//				Map<String, Object> mapValue = (Map<String, Object>) msg.getValue();
//				agent.currentContext = (Context) mapValue
//						.get(AgentModel.KEY_CONTEXT);
//				valueMsg = (Message) mapValue.get(AgentModel.KEY_VALUE_MESSAGE);
//				agent.disposeMessage(agent.constructNcccMessage(valueMsg));
//
//				agent.terminateReceivedFromParent = true;
//
//				//此处与Adopt:Pragnesh Jay Modi et al.中的伪代码不一样
//	            //terminateReceivedFromParent变为true，再次调用maintainThresholdInvariant()
//	            //防止TH==UB失败导致agent不能终止
//				maintainThresholdInvariant();
//				if(agent.msgQueue.isEmpty())backtrack();
//			} else {
//				//pseudo父agent发过来的terminate消息仅包含了value消息
//				valueMsg = (Message) msg.getValue();
//				agent.disposeMessage(agent.constructNcccMessage(valueMsg));
//			}
		}

		private void backtrack() {
			int[] ret = agent.computeMinimalLBAndUB();
			int dMinimizesLB = ret[0];
			int LB_CurValue = ret[1];
			int dMinimizesUB = ret[2];
			
			//do nccc local here
		    agent.increaseNcccLocal();
		    
		    BDmaintainThresholdInvariant();	    
		    int oldValue=agent.valueIndex;
		    
			if (agent.TH == agent.UB) {
				if (agent.valueIndex != dMinimizesUB) {
					Debugger.valueChanges.get(agent.getName()).add(dMinimizesUB);
				}

				agent.valueIndex = dMinimizesUB;
				if(agent.valueIndex!=oldValue)
				agent.valueID = agent.valueID + 1;
			} else if (LB_CurValue > agent.TH) {
				if (agent.valueIndex != dMinimizesLB) {
					Debugger.valueChanges.get(agent.getName()).add(dMinimizesLB);
				}

				agent.valueIndex = dMinimizesLB;
				if(agent.valueIndex!=oldValue)
				agent.valueID = agent.valueID + 1;
			}

			//System.out.println("agent"+agent.id+": "+agent.valueIndex+"\t"+agent.valueID+"\t"+agent.TH+"\t"+agent.LB+"\t"+agent.UB+"\t"+agent.nccc);
			this.maintainChildThresholdInvariant();
			this.maintainAllocationInvariant();   //必须将这个放在发送VALUE信息之前
			sendValueMessages();
			if (agent.TH == agent.UB) {
				if (agent.Readytermintate == true
						|| agent.isRootAgent() == true) {
					sendTerminateMessages();
					agent.stopRunning();
				}
			}
			sendCostMessage();
		}

//		private void maintainThresholdInvariant() {
//			if (agent.TH < agent.LB) {
//				agent.TH = agent.LB;
//			}
//			if (agent.TH > agent.UB) {
//				agent.TH = agent.UB;
//			}
//		}
		 
		private void BDmaintainThresholdInvariant() {
			if(agent.level==agent.boundary)
			{
				agent.TH=agent.LB;
			}
			else{
			if (agent.TH < agent.LB) {
				agent.TH = agent.LB;
			}
			if (agent.TH > agent.UB) {
				agent.TH = agent.UB;
			}
			}
		}	

		private void maintainAllocationInvariant() {
			if (agent.isLeafAgent() == true) {
				return;
			}
			int diff = agent.TH - computeTH(agent.valueIndex);
			int diffOriginalValue = diff;
			int childId = 0;
			if (diff > 0) {
				while (diff != 0) {
					diffOriginalValue = diff;
					for (int i = 0; i < agent.children.length; i++) {
						childId = agent.children[i];
						int availDiff = Infinity.minus(
								agent.ubs.get(childId)[agent.valueIndex],
								agent.ths.get(childId)[agent.valueIndex]);
						if (availDiff > 0) {
							if ((diff - availDiff) <= 0) {
								agent.ths.get(childId)[agent.valueIndex] = Infinity
										.add(agent.ths.get(childId)[agent.valueIndex],
												diff);
								diff = 0;
								break;
							} else {
								agent.ths.get(childId)[agent.valueIndex] = Infinity
										.add(agent.ths.get(childId)[agent.valueIndex],
												availDiff);
								diff = Infinity.minus(diff, availDiff);
							}
						}
					}
					if (diff == diffOriginalValue) {
						break;  //无法使diff为0，也退出
					}
				}
			} else if (diff < 0) {
				while (diff != 0) {
					diffOriginalValue = diff;
					for (int i = 0; i < agent.children.length; i++) {
						childId = agent.children[i];
						int availDiff = Infinity.minus(
								agent.ths.get(childId)[agent.valueIndex],
								agent.lbs.get(childId)[agent.valueIndex]);
						if (availDiff > 0) {
							if ((diff + availDiff) >= 0) {
								agent.ths.get(childId)[agent.valueIndex] = Infinity
										.minus(agent.ths.get(childId)[agent.valueIndex],
												diff);
								diff = 0;
								break;
							} else {
								agent.ths.get(childId)[agent.valueIndex] = Infinity
										.minus(agent.ths.get(childId)[agent.valueIndex],
												availDiff);
								diff = Infinity.add(diff, availDiff);
							}
						}
					}
					if (diff == diffOriginalValue) {
						break;       //无法使diff为0，也退出
					}
				}
			}
			// sendThresholdMessages();    //不发送癫狂的阈值信息，而是放阈值在VALUE信息里
		}

		private void maintainChildThresholdInvariant() {
			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
				for (int j = 0; j < agent.domain.length; j++) {
					if (agent.lbs.get(childId)[j] > agent.ths.get(childId)[j]) {
						agent.ths.get(childId)[j] = agent.lbs.get(childId)[j];
					}
					if (agent.ths.get(childId)[j] > agent.ubs.get(childId)[j]) {
						agent.ths.get(childId)[j] = agent.ubs.get(childId)[j];
					}
				}
			}
		}

		private int computeTH(int di) {
			int localCost_ = agent.localCost(di);

			if (agent.isLeafAgent() == true) {
				return localCost_;
			}

			int TH_di = 0;
			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
				TH_di = Infinity.add(TH_di, agent.ths.get(childId)[di]);
			}
			TH_di = Infinity.add(TH_di, localCost_);

			return TH_di;
		}
		
		

	}
	
	public class BnBMethod extends Method {

		private AgentModel agent;

		BnBMethod(AgentModel agent) {
			this.agent = agent;

		}

		@Override
		protected void initRun() {
			// TODO Auto-generated method stub

			agent.valueID = 0;
			agent.currentContext = new Context();
			if (!agent.isRootAgent())
				agent.currentContext.addOrUpdate(agent.parent, 0, 0); // 仅仅初始化为第1个取值
			if (agent.pseudoParents != null) {
				for (int pseudoP : agent.pseudoParents) {
					agent.currentContext.addOrUpdate(pseudoP, 0, 0); // 仅仅初始化为第1个取值
				}
			}
			agent.lbs = new HashMap<Integer, int[]>();
			agent.ubs = new HashMap<Integer, int[]>();
			agent.contexts = new HashMap<Integer, Context[]>();
			InitChild();
			InitSelf();
			backtrack();
		}

		private void InitChild() {
			if (agent.isLeafAgent() == false) {
				int childId = 0;
				for (int i = 0; i < agent.children.length; i++) {
					childId = agent.children[i];
					int[] childLbs = new int[agent.domain.length];
					int[] childUbs = new int[agent.domain.length];
					Context[] childContexts = new Context[agent.domain.length];
					for (int j = 0; j < agent.domain.length; j++) {
						childLbs[j] = 0;
						childUbs[j] = Infinity.INFINITY;
						childContexts[j] = new Context();
					}
					agent.lbs.put(childId, childLbs);
					agent.ubs.put(childId, childUbs);
					agent.contexts.put(childId, childContexts);
				}
			}
		}

		private void InitChild(int child, int d) {
			if (agent.isLeafAgent() == false) {
				agent.lbs.get(child)[d] = 0;
				agent.ubs.get(child)[d] = Infinity.INFINITY;
				agent.contexts.get(child)[d].reset();
			}
		}

		private void InitSelf() {

			agent.TH = Infinity.INFINITY;
			// int oldvalueIndex=this.valueIndex;
			agent.valueIndex = agent.computeMinimalLBAndUB()[0];
			// if(oldvalueIndex!=this.valueIndex||this.valueID==0)
			agent.valueID = valueID + 1;
			Debugger.valueChanges.get(agent.name).add(agent.valueIndex);

		}
		
		private void maintainTHInvariant()
		{
			if(agent.TH<agent.LB)
			{
				agent.TH=agent.LB;
			}
			if(agent.TH>agent.UB)
			{
				agent.TH=agent.UB;
			}
		}
			

		private void backtrack() {

			int[] compute = computeMinimalLBAndUB();

			// do nccc local here
			agent.increaseNcccLocal();

			int oldValueIndex = agent.valueIndex;
			maintainTHInvariant();
			if (agent.Readytermintate == true && agent.TH == agent.UB) {
				agent.valueIndex = compute[2];
				if (agent.valueIndex != oldValueIndex) {
					agent.valueID = agent.valueID + 1;
					Debugger.valueChanges.get(agent.name).add(agent.valueIndex);
				}
			} else {
				if (compute[1] >= agent.TH) {
					agent.valueIndex = compute[0];
					if (agent.valueIndex != oldValueIndex) {
						agent.valueID = agent.valueID + 1;
						Debugger.valueChanges.get(agent.name).add(
								agent.valueIndex);
					}
				}
			}
			//System.out.println("agent"+agent.id+": "+agent.valueIndex+"\t"+agent.valueID+"\t"+agent.TH+"\t"+agent.LB+"\t"+agent.UB+"\t"+agent.nccc);
			if (((isRootAgent() == true) && (agent.UB <= agent.LB))
					|| agent.Readytermintate == true &&agent.TH==agent.UB) {
				sendTerminateMessages();
				agent.stopRunning();
			}
			sendValueMessages();
			sendCostMessage();

		}

		protected void sendValueMessages() {
			if (agent.isLeafAgent() == true && agent.NoPseudoChild() == true) {
				return;
			}
			if (agent.isLeafAgent() == false) {
				int childId = 0;
				for (int i = 0; i < agent.children.length; i++) {
					int[] val = new int[3]; // 一个取值，一个ID，一个TH
					val[0] = agent.valueIndex;
					val[1] = agent.valueID;
					childId =agent.children[i];
					if(agent.Readytermintate==true&&agent.TH==agent.UB)
						val[2]=computeTH2(agent.valueIndex,childId);
					else
						val[2] = computeTH(agent.valueIndex, childId);
					Message msg = new Message(agent.id, childId,
							AgentModel.TYPE_VALUE_MESSAGE, val);
					agent.sendMessage(agent.constructNcccMessage(msg));
				}
			}
			if (agent.NoPseudoChild() == false) {
				int pseudoChildId = 0;
				for (int i = 0; i < agent.pseudoChildren.length; i++) {
					pseudoChildId = agent.pseudoChildren[i];
					int[] val = new int[3]; // 一个取值，一个ID，一个TH
					val[0] = agent.valueIndex;
					val[1] = agent.valueID;
					val[2] = Infinity.INFINITY;
					Message msg = new Message(agent.id, pseudoChildId,
							AgentModel.TYPE_VALUE_MESSAGE, val);
					agent.sendMessage(this.agent.constructNcccMessage(msg));
				}
			}
		}

		protected void sendCostMessage() {
			if (agent.isRootAgent() == true) {
				return;
			}

			Map<String, Object> cost = new HashMap<String, Object>();
			Context context = new Context(agent.currentContext);
			context.Remove(agent.id);
			cost.put(AgentModel.KEY_CONTEXT, context);
			cost.put(AgentModel.KEY_LB, agent.LB);
			cost.put(AgentModel.KEY_UB, agent.UB);

			Message msg = new Message(agent.id, agent.parent,
					AgentModel.TYPE_COST_MESSAGE, cost);
			agent.sendMessage(agent.constructNcccMessage(msg));
		}

		protected void sendTerminateMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}
			agent.terminateReceivedFromParent = true;
			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
//				int[] val = new int[3];
//				val[0] = agent.valueIndex;
//				val[1] = agent.valueID;
//				val[2] = computeTH(agent.valueIndex, childId);
//				Message valueMsg = new Message(agent.id, childId,
//						AgentModel.TYPE_VALUE_MESSAGE, val);

				Map<String, Object> mapValue = new HashMap<String, Object>();
//				Context c = new Context(agent.currentContext);
//				mapValue.put(AgentModel.KEY_CONTEXT, c);
//				mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

				Message msg = new Message(this.agent.id, childId,
						AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
				this.agent.sendMessage(this.agent.constructNcccMessage(msg));
			}
		}
		
		protected int computeTH(int di,int child)
		{
			int localCost_=localCost(di);
			
			if(agent.isLeafAgent()==true)
			{
				return localCost_;
			}
			
			int TH_di=0;
			int childId=0;
			for(int i=0;i<agent.children.length;i++)
			{
				childId=agent.children[i];
				if(childId!=child)TH_di=TH_di+agent.lbs.get(childId)[di];
			}
			TH_di=Infinity.add(TH_di, localCost_);
			
			return (agent.TH>agent.UB)?(agent.UB-TH_di):(agent.TH-TH_di);
		}
		
		private int computeTH2(int di,int child)
		{
			int localCost_=localCost(di);
			
			if(agent.isLeafAgent()==true)
			{
				return localCost_;
			}
			
			int TH_di=0;
			int childId=0;
			for(int i=0;i<agent.children.length;i++)
			{
				childId=agent.children[i];
				if(childId!=child)TH_di=TH_di+agent.ubs.get(childId)[di];
			}
			TH_di=Infinity.add(TH_di, localCost_);
			
			return (agent.TH-TH_di);
		}

		protected void disposeValueMessage(Message msg) {
			if (agent.terminateReceivedFromParent == false) {
				Context temp = new Context(agent.currentContext);
				int[] val = (int[]) msg.getValue();
				agent.currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);

				if (!checkCompatible(agent.currentContext, temp)) {
					checkCompatible();
					InitSelf();
				}
				if (msg.getIdSender() == agent.parent) {
					agent.TH = val[2];
				}
				if (agent.msgQueue.isEmpty() == true)
					backtrack();
			}
		}

		private boolean checkCompatible(Context c1, Context c2) {
			return c1.compatible(c2);
		}

		private void checkCompatible() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int childId = 0;
			for (int i = 0; i < agent.children.length; i++) {
				childId = agent.children[i];
				for (int j = 0; j < agent.domain.length; j++) {
					if (agent.currentContext.compatible(agent.contexts.get(childId)[j]) == false) {
						InitChild(childId, j);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		protected void disposeCostMessage(Message msg) {
			Map<String, Object> cost = (Map<String, Object>) msg.getValue();
			Context c = (Context) cost.get(AgentModel.KEY_CONTEXT);
			int myValueIndex = c.get(agent.id);

			if (myValueIndex == -1) {
				return;
			}
			Context temp = new Context(agent.currentContext);
			merge(c);
			agent.currentContext.Remove(agent.id); // 因为合并时将自己的取值加入，应该移除

			if (!checkCompatible(agent.currentContext, temp)) { // 不兼容表示引入了新的内容
				checkCompatible();

			}
			if (checkCompatible(c, agent.currentContext)) { // 兼容表示这个信息可以利用
				if (agent.lbs.get(msg.getIdSender())[myValueIndex] < (Integer) cost
						.get(AgentModel.KEY_LB))
					agent.lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
							.get(AgentModel.KEY_LB);
				if (agent.ubs.get(msg.getIdSender())[myValueIndex] > (Integer) cost
						.get(AgentModel.KEY_UB))
					agent.ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
							.get(AgentModel.KEY_UB);
				agent.contexts.get(msg.getIdSender())[myValueIndex] = c;
			}

			if (!checkCompatible(agent.currentContext, temp)) {
				InitSelf();

			}
			if (agent.msgQueue.isEmpty() == true)
				backtrack();
		}

		// 仅仅作一个记录，准备去停止，但并不是要停止。
		protected void disposeTerminateMessage(Message msg) {
			// Message valueMsg = null;
			agent.Readytermintate = true;
			// Map<String, Object> mapValue = (Map<String, Object>)
			// msg.getValue();
			// currentContext = (Context) mapValue.get(KEY_CONTEXT);
			// //不应该加入里，而要自己去处理判断。
			// valueMsg = (Message) mapValue.get(KEY_VALUE_MESSAGE);
			// disposeMessage(this.constructNcccMessage(valueMsg));
			// this.terminateReceivedFromParent = true;
			if (agent.msgQueue.isEmpty())
				backtrack();
		}

		protected void merge(Context c) {
			agent.currentContext.union(c);
		}			

	}

}
