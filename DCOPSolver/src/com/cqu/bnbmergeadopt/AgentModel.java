package com.cqu.bnbmergeadopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.test.Debugger;

public class AgentModel extends Agent {

	public AgentModel(int id, String name,int level, int[] domain ) {
		super(id, name, level, domain);
		this.height = level;
	}

	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_COST_MESSAGE = 1;
	public final static int TYPE_THRESHOLD_MESSAGE = 2;
	public final static int TYPE_TERMINATE_MESSAGE = Message.TYPE_TERMINATE_MESSAGE;

	public final static String KEY_CONTEXT = "KEY_CONTEXT";
	public final static String KEY_LB = "KEY_LB";
	public final static String KEY_UB = "KEY_UB";
	public final static String KEY_TH = "KEY_TH";
	public final static String STRATEGY = "Strategy";

	public final static String KEY_ID = "KEY_ID";
	public final static String KEY_NAME = "KEY_NAME";
	public final static String KEY_VALUE = "KEY_VALUE";

	public final static String KEY_VALUE_MESSAGE = "KEY_VALUE_MESSAGE";

	public Map<Integer, int[]> lbs;
	public Map<Integer, int[]> ubs;
	public Map<Integer, int[]> ths;
	public int LB;
	public int UB;
	public int TH;

	public Map<Integer, Context[]> contexts;
	public Context currentContext;

	public int height;
	public String strategy;
	public String typeMethod;

	public int valueIndex;
	public int valueID;
	public boolean terminateReceivedFromParent = false;
	public boolean Readytermintate = false;

	public Method method;

	@Override
	protected void initRun() {
		// a sole bnbadopt
		method = new BnBMethod(this);
		typeMethod="bnbadopt";
		strategy = "bnbadopt";

		// a sole adopt
		 //method=new AdoptMethod(this);
		// typeMethod="adopt";
		// strategy="adopt";

		// union adopt and bnbadopt
		/* if(this.height==3||this.height==4||this.height==5){
			 method=new AdoptMethod(this);
			 typeMethod="adopt";
		 }
		 else {
			 method=new BnBMethod(this);
			 typeMethod="bnbadopt";
		 }
		 strategy="bnbandadopt";*/
		super.initRun();
		method.initRun();
	}

	protected boolean isLeafAgent() {
		return this.children == null;
	}

	protected boolean isRootAgent() {
		return this.parent == -1;
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
	public void printResults(List<Map<String, Object>> results) {

		int totalCost = -1;
		for (Map<String, Object> result : results) {
			int id_ = (Integer) result.get(AgentModel.KEY_ID);
			String name_ = (String) result.get(AgentModel.KEY_NAME);
			int value_ = (Integer) result.get(AgentModel.KEY_VALUE);
			int LB_ = (Integer) result.get(AgentModel.KEY_LB);
			int UB_ = (Integer) result.get(AgentModel.KEY_UB);
			int TH_ = (Integer) result.get(AgentModel.KEY_TH);
			if (totalCost == -1) {
				totalCost = UB_;
			}

			String displayStr = "Agent " + name_ + ": id=" + id_ + " value="
					+ value_ + " LB=" + LB_ + " UB=";
			displayStr += Infinity.infinityEasy(UB_);
			displayStr += " TH=" + Infinity.infinityEasy(TH_);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: " + Infinity.infinityEasy(totalCost));
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
		result.put(AgentModel.STRATEGY, this.strategy);
		
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

}
