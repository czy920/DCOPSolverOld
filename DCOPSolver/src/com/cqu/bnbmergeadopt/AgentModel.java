package com.cqu.bnbmergeadopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.adopt.AdoptAgent;
import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.MessageNCCC;
import com.cqu.core.ResultAdopt;
import com.cqu.test.Debugger;

public class AgentModel extends Agent {

	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_COST_MESSAGE = 1;
	public final static int TYPE_THRESHOLD_MESSAGE = 2;
	public final static int TYPE_TERMINATE_MESSAGE = Message.TYPE_TERMINATE_MESSAGE;

	public final static String KEY_CONTEXT = "KEY_CONTEXT";
	public final static String KEY_LB = "KEY_LB";
	public final static String KEY_UB = "KEY_UB";
	public final static String KEY_TH = "KEY_TH";
	public final static String KEY_NCCC="KEY_NCCC";
	//public final static String STRATEGY = "Strategy";

	public final static String KEY_VALUE_MESSAGE = "KEY_VALUE_MESSAGE";

	public Map<Integer, int[]> lbs;
	public Map<Integer, int[]> ubs;
	public Map<Integer, int[]> ths;
	public int LB;
	public int UB;
	public int TH;

	public Map<Integer, Context[]> contexts;
	public Context currentContext;

	//public String strategy;
	public String typeMethod;

	public int valueID;
	public boolean terminateReceivedFromParent = false;
	public boolean Readytermintate = false;
	
	private int nccc;

	public Method method;
	
	public AgentModel(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		this.nccc = 0;
		
		// a sole bnbadopt
		method = new BnBMethod(this);
		typeMethod = "bnbadopt";
		// strategy = "bnbadopt";

		// a sole adopt
		// method=new AdoptMethod(this);
		// typeMethod="adopt";
		// strategy="adopt";

		// union adopt and bnbadopt
		/*
		 * if(this.level==0||this.level==1||this.level==2){ //前三层用bnbadopt策略
		 * method=new BnBMethod(this); typeMethod="bnbadopt";
		 * 
		 * } else { //其他层用adopt策略 method=new AdoptMethod(this);
		 * typeMethod="adopt"; }
		 */
		// strategy="bnbandadopt";
	}

	@Override
	protected void initRun() {	
		super.initRun();
		method.initRun();
	}

	public boolean NoPseudoChild() {
		return this.pseudoChildren == null;
	}

	protected void merge(Context c) {
		Context temp = new Context(c);
		temp.remove(this.neighbours);
		currentContext.union(temp);
	}
	
	protected void merge2(Context c){
		currentContext.union(c);
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
		} else if (msg.getType() == AgentModel.TYPE_THRESHOLD_MESSAGE) {
			method.disposeThresholdMessage(msg);
		} else if (msg.getType() == AgentModel.TYPE_TERMINATE_MESSAGE) {
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
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {

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
		result.put(AdoptAgent.KEY_NCCC, this.nccc);
		//result.put(AgentModel.STRATEGY, this.strategy);
		
		this.msgMailer.setResult(result);

		// System.out.println("Agent "+this.name+" stopped!");
	}

	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg) {
		switch (msg.getType()) {
		case AgentModel.TYPE_VALUE_MESSAGE: {
			int valueIndex = (Integer) msg.getValue();
			return "value[" + valueIndex + "]";
		}
		case AgentModel.TYPE_COST_MESSAGE: {
			Map<String, Object> msgValue = (Map<String, Object>) msg.getValue();
			int LB_ = (Integer) msgValue.get(KEY_LB);
			int UB_ = (Integer) msgValue.get(KEY_UB);
			Context c = (Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB=" + LB_ + " UB=" + UB_ + " context=" + c.toString()
					+ "]";
		}
		case AgentModel.TYPE_THRESHOLD_MESSAGE: {
			Map<String, Object> msgValue = (Map<String, Object>) msg.getValue();
			int TH_ = (Integer) msgValue.get(KEY_TH);
			Context c = (Context) msgValue.get(KEY_CONTEXT);
			return "threshold[TH=" + TH_ + " context=" + c.toString() + "]";
		}
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
		int t=0;
		this.nccc=Math.max(mn.getNccc()+t, this.nccc);
	}
	
	private Message constructNcccMessage(Message msg)
	{
		return new MessageNCCC(msg, this.nccc);
	}
	
	
	
	public static interface Method {
		
		 public  void disposeValueMessage(Message msg);

		 public  void disposeCostMessage(Message msg);

		 public  void disposeThresholdMessage(Message msg);

		 public  void disposeTerminateMessage(Message msg);

		public  void initRun();
		 
		public  void sendValueMessages();
		
		public  void sendCostMessages();
		
		public  void sendTerminateMessages();
		
		public  void sendThresholdMessages();
	}
	
	public int[] getChildren()
	{
		return this.children;
	}
	
	public int[] getPseudoChildren()
	{
		return this.pseudoChildren;
	}
	
	public int[] getDomain()
	{
		return this.domain;
	}
	
	public int getParent()
	{
		return this.parent;
	}
	
	public int[] getPseudoParents()
	{
		return this.pseudoParents;
	}
	
	public class AdoptMethod implements Method {

		private AgentModel agent;
		AdoptMethod(AgentModel agent) {
			this.agent=agent;
		}

		public void initRun() {

			agent.TH = 0;
			agent.valueID = 0;
			agent.currentContext = new Context();

			agent.lbs = new HashMap<Integer, int[]>();
			agent.ubs = new HashMap<Integer, int[]>();
			agent.ths = new HashMap<Integer, int[]>();
			agent.contexts = new HashMap<Integer, Context[]>();

			if (agent.isLeafAgent() == false) {
				int childId = 0;
				for (int i = 0; i < agent.getChildren().length; i++) {
					childId = agent.getChildren()[i];
					int[] childLbs = new int[agent.getDomain().length];
					int[] childUbs = new int[agent.getDomain().length];
					int[] childThs = new int[agent.getDomain().length];
					Context[] childContexts = new Context[agent.getDomain().length];
					for (int j = 0; j < agent.getDomain().length; j++) {
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

			agent.valueIndex = agent.computeMinimalLBAndUB()[0];
			//agent.valueID = agent.valueID + 1;
			Debugger.valueChanges.get(agent.getName()).add(agent.valueIndex);

			backtrack();
		}

		private void checkCompatible() {
			if (agent.isLeafAgent() == true) {
				return;
			}
			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				for (int j = 0; j < agent.getDomain().length; j++) {
					if (agent.contexts.get(childId)[j].compatible(agent.currentContext) == false) {
						agent.lbs.get(childId)[j] = 0;
						agent.ths.get(childId)[j] = 0;
						agent.ubs.get(childId)[j] = Infinity.INFINITY;
						agent.contexts.get(childId)[j].reset();
					}
				}
			}
		}

		@Override
		public void sendValueMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int ChildId = 0;
			int[] val = new int[3];
			val[0] = agent.valueIndex;
			val[1] = agent.valueID;
			val[2] = -1; // a virtural th
			for (int i = 0; i < agent.children.length; i++) {
				ChildId = agent.children[i];
				val[2] = agent.ths.get(ChildId)[agent.valueIndex];
				Message msg = new Message(agent.getId(), ChildId,
						AgentModel.TYPE_VALUE_MESSAGE, val);
				agent.sendMessage(msg);
			}
			if (agent.NoPseudoChild() == true) {
				return;
			}
			int pseudoChildId=0;
			for (int i = 0; i < agent.getPseudoChildren().length; i++) {
				pseudoChildId = agent.getPseudoChildren()[i];
				val[2]=-1;
				Message msg = new Message(agent.getId(), pseudoChildId,
						AgentModel.TYPE_VALUE_MESSAGE,val);
				agent.sendMessage(this.agent.constructNcccMessage(msg));
			}
		}

		@Override
		public void sendCostMessages() {
			if (agent.isRootAgent() == true) {
				return;
			}

			Map<String, Object> cost = new HashMap<String, Object>();
			Context context = new Context(agent.currentContext);
			context.Remove(agent.getId());
			cost.put(AgentModel.KEY_CONTEXT, new Context(context));
			cost.put(AgentModel.KEY_LB, agent.LB);
			cost.put(AgentModel.KEY_UB, agent.UB);

			Message msg = new Message(agent.getId(), agent.getParent(),
					AgentModel.TYPE_COST_MESSAGE, cost);
			agent.sendMessage(agent.constructNcccMessage(msg));
		}

		//don't been used instead of inputing the th into value message
		@Override
		public void sendThresholdMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				Map<String, Object> thresh = new HashMap<String, Object>();
				thresh.put(AgentModel.KEY_CONTEXT,
						new Context(agent.currentContext));
				thresh.put(AgentModel.KEY_TH,
						agent.ths.get(childId)[agent.valueIndex]);

				Message msg = new Message(agent.getId(), childId,
						AgentModel.TYPE_THRESHOLD_MESSAGE, thresh);
				agent.sendMessage(agent.constructNcccMessage(msg));
			}
		}

		@Override
		public void sendTerminateMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				Context c = new Context(agent.currentContext);
				c.addOrUpdate(agent.getId(), agent.valueIndex, agent.valueID);
				int[] val = new int[3];
				val[0] = agent.valueIndex;
				val[1] = agent.valueID;
				val[2] = -1;
				Message valueMsg = new Message(agent.getId(), childId,
						AgentModel.TYPE_VALUE_MESSAGE,val);

				Map<String, Object> mapValue = new HashMap<String, Object>();
				mapValue.put(AgentModel.KEY_CONTEXT, c);
				mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

				Message msg = new Message(agent.getId(), childId,
						AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
				agent.sendMessage(agent.constructNcccMessage(msg));
			}

			if (agent.getPseudoChildren() != null) {
				int pseudoChildId = 0;
				for (int i = 0; i < agent.getPseudoChildren().length; i++) {
					pseudoChildId = agent.getPseudoChildren()[i];
					int[] val = new int[3];
					val[0] = agent.valueIndex;
					val[1] = agent.valueID;
					val[2] = -1;
					Message valueMsg = new Message(agent.getId(), pseudoChildId,
							AgentModel.TYPE_VALUE_MESSAGE, val);

					Message msg = new Message(agent.getId(), pseudoChildId,
							AgentModel.TYPE_TERMINATE_MESSAGE, valueMsg);
					agent.sendMessage(agent.constructNcccMessage(msg));
				}
			}
		}

		@Override
		public void disposeValueMessage(Message msg) {
			if (agent.terminateReceivedFromParent == false) {
				int[] val = new int[3];
				val=(int[]) msg.getValue();
				if (msg.getIdSender() == agent.getParent() && val[2] != (-1)) {
					agent.TH = val[2];
					maintainThresholdInvariant();
				}

				agent.currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
				checkCompatible();
				maintainThresholdInvariant();
				//checkThresholdInvariant(valuemsg.gettypeMethod());
				backtrack();
			}
		}

		@SuppressWarnings("unchecked")
		public void disposeCostMessage(Message msg) {
			Map<String, Object> cost=(Map<String, Object>) msg.getValue();
			Context c = (Context) cost.get(AgentModel.KEY_CONTEXT);
			int myValueIndex = c.Remove(agent.getId());

			if (myValueIndex == -1) {
				return;
			}

			if (agent.terminateReceivedFromParent == false) {
				agent.merge(c);
				checkCompatible();
			}
			if (c.compatible(agent.currentContext) == true) {
				agent.lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(AgentModel.KEY_LB);
				agent.ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(AgentModel.KEY_UB);
				agent.contexts.get(msg.getIdSender())[myValueIndex] = c;

				maintainChildThresholdInvariant();
				maintainThresholdInvariant();
				//checkThresholdInvariant(costmsg.gettypeMethod());
			}
			backtrack();

		}

		//don't been used instead of simple handle during disposing value message
		@SuppressWarnings("unchecked")
		public void disposeThresholdMessage(Message msg) {
			Map<String, Object> thresh = (Map<String, Object>) msg.getValue();
			if (((Context) thresh.get(AgentModel.KEY_CONTEXT))
					.compatible(agent.currentContext) == true) {
				agent.TH = (Integer) thresh.get(AgentModel.KEY_TH);
				maintainThresholdInvariant();
				backtrack();
			}
		}

		@SuppressWarnings("unchecked")
		public void disposeTerminateMessage(Message msg) {
			Message valueMsg = null;
			// 鐖禷gent鍙戣繃鏉ョ殑terminate娑堟伅涓哄寘鍚簡鍩烘湰鐨則erminate娑堟伅鍜寁alue娑堟伅
			if (msg.getIdSender() == agent.getParent()) {
				Map<String, Object> mapValue = (Map<String, Object>) msg.getValue();
				agent.currentContext = (Context) mapValue
						.get(AgentModel.KEY_CONTEXT);
				valueMsg = (Message) mapValue.get(AgentModel.KEY_VALUE_MESSAGE);
				agent.disposeMessage(agent.constructNcccMessage(valueMsg));

				agent.terminateReceivedFromParent = true;

				// 姝ゅ涓嶢dopt:Pragnesh Jay Modi et al.涓殑浼唬鐮佷笉涓�牱
				// terminateReceivedFromParent鍙樹负true锛屽啀娆¤皟鐢╩aintainThresholdInvariant()
				// 闃叉TH==UB澶辫触瀵艰嚧agent涓嶈兘缁堟
				maintainThresholdInvariant();
				backtrack();
			} else {
				// pseudo鐖禷gent鍙戣繃鏉ョ殑terminate娑堟伅浠呭寘鍚簡value娑堟伅
				valueMsg = (Message) msg.getValue();
				agent.disposeMessage(agent.constructNcccMessage(valueMsg));
			}

		}

		private void backtrack() {
			int[] ret = agent.computeMinimalLBAndUB();
			int dMinimizesLB = ret[0];
			int LB_CurValue = ret[1];
			int dMinimizesUB = ret[2];
			
			//do nccc local here
		    agent.increaseNcccLocal();
		    
			if (agent.TH == agent.UB) {
				if (agent.valueIndex != dMinimizesUB) {
					Debugger.valueChanges.get(agent.getName()).add(dMinimizesUB);
				}

				agent.valueIndex = dMinimizesUB;
				//agent.valueID = agent.valueID + 1;
			} else if (LB_CurValue > agent.TH) {
				if (agent.valueIndex != dMinimizesLB) {
					Debugger.valueChanges.get(agent.getName()).add(dMinimizesLB);
				}

				agent.valueIndex = dMinimizesLB;
				//agent.valueID = agent.valueID + 1;
			}

			maintainAllocationInvariant();
			sendValueMessages();
			if (agent.TH == agent.UB) {
				if (agent.terminateReceivedFromParent == true
						|| agent.isRootAgent() == true) {
					sendTerminateMessages();
					agent.stopRunning();
				}
			}
			sendCostMessages();
		}

		private void maintainThresholdInvariant() {
			if (agent.TH < agent.LB) {
				agent.TH = agent.LB;
			}
			if (agent.TH > agent.UB) {
				agent.TH = agent.UB;
			}
		}
		 
		/*private void checkThresholdInvariant(String typeMethod){
			if(typeMethod.equals("bnbadopt"))
			{
				//System.out.println("have execute this part");
				if (agent.TH < agent.LB) {
					System.out.println(agent.getName()+" parent is bnbadopt " + "TH= " + agent.TH + " LB= " + agent.LB + " TH<LB");
					agent.TH = agent.LB;
				}
				if (agent.TH > agent.UB) {
					System.out.println(agent.getName()+" parent is bnbadopt " + "TH= " + agent.TH + " UB= " + agent.UB + " TH>UB");
					agent.TH = agent.UB;
				}
			}
			else{
			if (agent.TH < agent.LB) {
				agent.TH = agent.LB;
			}
			if (agent.TH > agent.UB) {
				agent.TH = agent.UB;
			}
			}
		}*/

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
					for (int i = 0; i < agent.getChildren().length; i++) {
						childId = agent.getChildren()[i];
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
						break;// 鏃犳硶浣縟iff涓�锛屼篃閫�嚭
					}
				}
			} else if (diff < 0) {
				while (diff != 0) {
					diffOriginalValue = diff;
					for (int i = 0; i < agent.getChildren().length; i++) {
						childId = agent.getChildren()[i];
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
						break;// 鏃犳硶浣縟iff涓�锛屼篃閫�嚭
					}
				}
			}
			// sendThresholdMessages();
		}

		private void maintainChildThresholdInvariant() {
			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				for (int j = 0; j < agent.getDomain().length; j++) {
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
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				TH_di = Infinity.add(TH_di, agent.ths.get(childId)[di]);
			}
			TH_di = Infinity.add(TH_di, localCost_);

			return TH_di;
		}
		
		

	}
	
	public class BnBMethod implements Method {

		private AgentModel agent;
		BnBMethod(AgentModel agent) {
			this.agent=agent;

		}

		@Override
		public void disposeThresholdMessage(Message msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendThresholdMessages() {
			// TODO Auto-generated method stub

		}

		@Override
		public void initRun() {
			agent.valueID = 0;
			agent.currentContext = new Context();
			if(agent.pseudoParents!=null){
			for (int pseudoP : agent.getPseudoParents()) {

				agent.currentContext.addOrUpdate(pseudoP, 0, 0);
			}
			}
			agent.lbs = new HashMap<Integer, int[]>();
			agent.ubs = new HashMap<Integer, int[]>();
			agent.contexts = new HashMap<Integer, Context[]>();
			InitChild();
			InitSelf();
			backtrack();
		}

		private void checkCompatible() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				for (int j = 0; j < agent.getDomain().length; j++) {
					if (agent.currentContext.compatible(agent.contexts.get(childId)[j]) == false) {
						InitChild(childId);
					}
				}
			}
		}

		private void InitChild() {
			if (agent.isLeafAgent() == false) {
				int childId = 0;
				for (int i = 0; i < agent.getChildren().length; i++) {
					childId = agent.getChildren()[i];
					int[] childLbs = new int[agent.getDomain().length];
					int[] childUbs = new int[agent.getDomain().length];
					Context[] childContexts = new Context[agent.getDomain().length];
					for (int j = 0; j < agent.getDomain().length; j++) {
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

		private void InitChild(int child) {
			if (agent.isLeafAgent() == false) {
				for (int j = 0; j < agent.getDomain().length; j++) {
					agent.lbs.get(child)[j] = 0;
					agent.ubs.get(child)[j] = Infinity.INFINITY;
					agent.contexts.get(child)[j].reset();
				}
			}
		}

		private void InitSelf() {

			agent.TH = Infinity.INFINITY;
			agent.valueIndex = agent.computeMinimalLBAndUB()[0];
			
			agent.valueID = agent.valueID + 1;
			// Debugger.valueChanges.get(agent.getName()).add(agent.valueIndex);
			agent.currentContext.addOrUpdate(agent.getId(), agent.valueIndex,    
					agent.valueID);
		}

		private void backtrack() {
			int[] compute = agent.computeMinimalLBAndUB();
			
			//do nccc local here
			agent.increaseNcccLocal();
					
			int oldValue = agent.valueIndex;
			int min = (agent.TH > agent.UB) ? agent.UB : agent.TH;
			if (compute[1] >= min) {
				agent.valueIndex = compute[0];
				if (agent.valueIndex != oldValue) {
					agent.valueID = agent.valueID + 1;
					Debugger.valueChanges.get(agent.getName())
							.add(agent.valueIndex);
				}

				agent.currentContext.addOrUpdate(agent.getId(), agent.valueIndex,
						agent.valueID);
			}
			if (((agent.isRootAgent() == true) && (agent.UB <= agent.LB))
					|| agent.terminateReceivedFromParent == true) {
				sendTerminateMessages();
				agent.stopRunning();
			}
			sendValueMessages();
			sendCostMessages();

		}

		@Override
		public void sendValueMessages() {
			if (agent.isLeafAgent() == true && agent.NoPseudoChild() == true) {
				return;
			}
			int[] val = new int[3]; // 一个取值，一个ID，一个TH
			val[0] = agent.valueIndex;
			val[1] = agent.valueID;
			if (agent.isLeafAgent() == false) {

				int childId = 0;

				for (int i = 0; i < agent.getChildren().length; i++) {
					childId = agent.getChildren()[i];
					val[2] = computeTH(agent.valueIndex, childId);
					
					//to tracking the relation TH,LB,UB
					//System.out.println(agent.getName() + " send a end to a child " + childId +" = " + val[2]);
					
					Message msg = new Message(agent.getId(), childId,
							AgentModel.TYPE_VALUE_MESSAGE, val);
					agent.sendMessage(agent.constructNcccMessage(msg));
				}
			}
			if (agent.NoPseudoChild() == false) {
				int pseudoChildId = 0;
				for (int i = 0; i < agent.getPseudoChildren().length; i++) {
					pseudoChildId = agent.getPseudoChildren()[i];
					val[2] = Infinity.INFINITY;
					
					//to tracking the relation TH,LB,UB
					//System.out.println(agent.getName() + " send a end to a child " + pseudoChildId + " = " + val[2]);
					
					Message msg = new Message(agent.getId(), pseudoChildId,
							AgentModel.TYPE_VALUE_MESSAGE, val);
					agent.sendMessage(agent.constructNcccMessage(msg));
				}
			}
		}

		@Override
		public void sendCostMessages() {
			if (agent.isRootAgent() == true) {
				return;
			}

			Map<String, Object> cost = new HashMap<String, Object>();
			Context context = new Context(agent.currentContext);
			context.Remove(agent.getId());
			cost.put(AgentModel.KEY_CONTEXT, context);
			cost.put(AgentModel.KEY_LB, agent.LB);
			cost.put(AgentModel.KEY_UB, agent.UB);

			Message msg = new Message(agent.getId(), agent.getParent(),
					AgentModel.TYPE_COST_MESSAGE, cost);
			agent.sendMessage(agent.constructNcccMessage(msg));
		}

		@Override
		public void sendTerminateMessages() {
			if (agent.isLeafAgent() == true) {
				return;
			}

			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				Context c = new Context(agent.currentContext);
				int[] val = new int[3];
				val[0] = agent.valueIndex;
				val[1] = agent.valueID;
				val[2] = computeTH(agent.valueIndex, childId);
				
				//to tracking the relation TH,LB,UB
				//System.out.println(agent.getName() + " send a end to a child " + childId +" = " + val[2]);
				
				Message valueMsg = new Message(agent.getId(), childId,
						AgentModel.TYPE_VALUE_MESSAGE, val);

				Map<String, Object> mapValue = new HashMap<String, Object>();
				mapValue.put(AgentModel.KEY_CONTEXT, c);
				mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

				Message msg = new Message(agent.getId(), childId,
						AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
				agent.sendMessage(agent.constructNcccMessage(msg));
			}
		}

		@Override
		public void disposeValueMessage(Message msg) {
			if (agent.terminateReceivedFromParent == false) {
				Context temp = new Context(agent.currentContext);
				int[] val =(int[]) msg.getValue();
				agent.currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);

				if (!checkCompatible(agent.currentContext, temp)) {  //新上下文的与旧的上下文是否兼容
					checkCompatible();
					InitSelf();
				}
				if (msg.getIdSender() == agent.getParent()) {
					agent.TH = val[2];
				}
				if (agent.Readytermintate == false)
					backtrack();
			}
		}

		private boolean checkCompatible(Context c1, Context c2) {
			return c1.compatible(c2);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void disposeCostMessage(Message msg) {
			Map<String, Object> cost=(Map<String, Object>) msg.getValue();
			Context c = (Context) cost.get(AgentModel.KEY_CONTEXT);
			int myValueIndex = c.get(agent.getId());

			if (myValueIndex == -1) {
				return;
			}
			Context temp =new Context(agent.currentContext);
			
			agent.merge2(c);

			if (!checkCompatible(agent.currentContext, temp)) {
				checkCompatible();    //是否会出现问题？如蚨有一个与自己无关的上下文不兼容，也是初始化吗？
			}
			if(checkCompatible(c,temp))
			{
				if (agent.lbs.get(msg.getIdSender())[myValueIndex] < (Integer) cost.get(AgentModel.KEY_LB))
				{
					agent.lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost.get(AgentModel.KEY_LB);
				}
				if (agent.ubs.get(msg.getIdSender())[myValueIndex] > (Integer) cost.get(AgentModel.KEY_UB))
				{
					agent.ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost.get(AgentModel.KEY_UB);
				}
				agent.contexts.get(msg.getIdSender())[myValueIndex] = c;
			}
			if(!checkCompatible(agent.currentContext,temp)){
				InitSelf();
			}
			backtrack();

		}

		@SuppressWarnings("unchecked")
		@Override
		public void disposeTerminateMessage(Message msg) {
			Message valueMsg = null;
			agent.Readytermintate = true;
			Map<String, Object> mapValue = (Map<String, Object>) msg.getValue();
			agent.currentContext = (Context) mapValue.get(AgentModel.KEY_CONTEXT);
			valueMsg = (Message) mapValue.get(AgentModel.KEY_VALUE_MESSAGE);
			agent.disposeMessage(agent.constructNcccMessage(valueMsg));
			agent.terminateReceivedFromParent = true;
			backtrack();

		}

		private int computeTH(int di, int child) {
			int localCost_ = agent.localCost(di);

			if (agent.isLeafAgent() == true) {
				return localCost_;
			}

			int TH_di = 0;
			int childId = 0;
			for (int i = 0; i < agent.getChildren().length; i++) {
				childId = agent.getChildren()[i];
				if (childId != child)
					TH_di = TH_di + agent.lbs.get(childId)[di];
			}
			TH_di = Infinity.add(TH_di, localCost_);
			return (agent.TH > agent.UB) ? (agent.UB - TH_di) : (agent.TH - TH_di);
		}
	}



}
