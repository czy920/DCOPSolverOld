package com.cqu.bnbmergeadopt;


import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.test.Debugger;

public class BnBMethod extends Method {

	BnBMethod(AgentModel agent) {
		super(agent);

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
		for (int pseudoP : agent.getPseudoParents()) {

			agent.currentContext.addOrUpdate(pseudoP, 0, 0);
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
						AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));
				agent.sendMessage(msg);
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
						AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));
				agent.sendMessage(msg);
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
				AgentModel.TYPE_COST_MESSAGE, new CostMsg(cost,agent.strategy));
		agent.sendMessage(msg);
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
					AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));

			Map<String, Object> mapValue = new HashMap<String, Object>();
			mapValue.put(AgentModel.KEY_CONTEXT, c);
			mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

			Message msg = new Message(agent.getId(), childId,
					AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
			agent.sendMessage(msg);
		}
	}

	@Override
	public void disposeValueMessage(Message msg) {
		if (agent.terminateReceivedFromParent == false) {
			Context temp = new Context(agent.currentContext);
			ValueMsg valuemsg = (ValueMsg) msg.getValue();
			int[] val = valuemsg.getVal();
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

	@Override
	public void disposeCostMessage(Message msg) {
		 CostMsg costmsg = (CostMsg) msg.getValue();
		 Map<String, Object> cost=costmsg.getCost();
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
		agent.disposeMessage(valueMsg);
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
