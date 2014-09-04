package com.cqu.bnbmergeadopt;

import java.util.HashMap;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.test.Debugger;

public class AdoptMethod extends Method {

	AdoptMethod(AgentModel agent) {
		super(agent);
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

		int pseudoChildId = 0;
		int[] val = new int[3];
		val[0] = agent.valueIndex;
		val[1] = agent.valueID;
		val[2] = -1; // a virtural th
		for (int i = 0; i < agent.getChildren().length; i++) {
			pseudoChildId = agent.getChildren()[i];
			val[2] = agent.ths.get(pseudoChildId)[agent.valueIndex];
			Message msg = new Message(agent.getId(), pseudoChildId,
					AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));
			agent.sendMessage(msg);
		}
		if (agent.NoPseudoChild() == true) {
			return;
		}
		for (int i = 0; i < agent.getPseudoChildren().length; i++) {
			pseudoChildId = agent.getPseudoChildren()[i];
			val[2]=-1;
			Message msg = new Message(agent.getId(), pseudoChildId,
					AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));
			agent.sendMessage(msg);
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
				AgentModel.TYPE_COST_MESSAGE, new CostMsg(cost,agent.strategy));
		agent.sendMessage(msg);
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
			agent.sendMessage(msg);
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
					AgentModel.TYPE_VALUE_MESSAGE,new ValueMsg(val,agent.typeMethod));

			Map<String, Object> mapValue = new HashMap<String, Object>();
			mapValue.put(AgentModel.KEY_CONTEXT, c);
			mapValue.put(AgentModel.KEY_VALUE_MESSAGE, valueMsg);

			Message msg = new Message(agent.getId(), childId,
					AgentModel.TYPE_TERMINATE_MESSAGE, mapValue);
			agent.sendMessage(msg);
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
						AgentModel.TYPE_VALUE_MESSAGE, new ValueMsg(val,agent.typeMethod));

				Message msg = new Message(agent.getId(), pseudoChildId,
						AgentModel.TYPE_TERMINATE_MESSAGE, valueMsg);
				agent.sendMessage(msg);
			}
		}
	}

	@Override
	public void disposeValueMessage(Message msg) {
		if (agent.terminateReceivedFromParent == false) {
			int[] val = new int[3];
			ValueMsg valuemsg =  (ValueMsg) msg.getValue();
			val=valuemsg.getVal();
			if (msg.getIdSender() == agent.getParent() && val[2] != (-1)) {
				agent.TH = val[2];
			}

			agent.currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
			checkCompatible();
			//maintainThresholdInvariant();
			checkThresholdInvariant(valuemsg.gettypeMethod());
			backtrack();
		}
	}

	public void disposeCostMessage(Message msg) {
		 CostMsg costmsg = (CostMsg) msg.getValue();
		 Map<String, Object> cost = costmsg.getCost();
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
			//maintainThresholdInvariant();
			checkThresholdInvariant(costmsg.gettypeMethod());
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
			agent.disposeMessage(valueMsg);

			agent.terminateReceivedFromParent = true;

			// 姝ゅ涓嶢dopt:Pragnesh Jay Modi et al.涓殑浼唬鐮佷笉涓�牱
			// terminateReceivedFromParent鍙樹负true锛屽啀娆¤皟鐢╩aintainThresholdInvariant()
			// 闃叉TH==UB澶辫触瀵艰嚧agent涓嶈兘缁堟
			//maintainThresholdInvariant();
			backtrack();
		} else {
			// pseudo鐖禷gent鍙戣繃鏉ョ殑terminate娑堟伅浠呭寘鍚簡value娑堟伅
			valueMsg = (Message) msg.getValue();
			agent.disposeMessage(valueMsg);
		}

	}

	private void backtrack() {
		int[] ret = agent.computeMinimalLBAndUB();
		int dMinimizesLB = ret[0];
		int LB_CurValue = ret[1];
		int dMinimizesUB = ret[2];
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
	 
	private void checkThresholdInvariant(String typeMethod){
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
