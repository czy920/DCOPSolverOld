package com.cqu.maxsum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.cqu.core.Message;

public abstract class AbstractNode {
	public static final int MSG_TYPE_FUNCTION_TO_VARIABLE = 1;
	public static final int MSG_TYPE_VARIABLE_TO_FUNCTION = 2;
	public static final int MSG_TYPE_START = 4;

	public static final int MSG_TYPE_AD_REVERSE_REQ = 8;
	public static final int MSG_TYPE_AD_REVERSE_ACK = 16;
	public static final int MSG_TYPE_PUNCH_DOMAIN = 32;


	protected static final String NODE_TYPE_VARIABLE_NODE = "VARIABLE";
	protected static final String NODE_TYPE_FUNCTION_NODE = "FUNCTION";

	protected ParentInfoProvider parent;
	protected Map<Integer,HyperCube> comingMessages;
	protected Map<Integer,Boolean> sendFlag;
	protected Map<Integer,Boolean> receiveFlag;
	protected String nodeType;
	protected boolean blocking;
	protected int targetId;

	public AbstractNode(ParentInfoProvider parent,boolean blocking){
		this.parent = parent;
		comingMessages = new HashMap<>();
		this.blocking = blocking;
		targetId = -1;
		receiveFlag = new HashMap<>();
		sendFlag = new HashMap<>();
	}

	public void addMessage(Message message){
		HyperCube cube = ((MessageContent)message.getValue()).getCube();
		comingMessages.put(message.getIdSender(),cube);
	}

	public void addNeighbours(int id,int domainSize){
		if (nodeType.equals(NODE_TYPE_FUNCTION_NODE))
			comingMessages.put(id,HyperCube.createZeroHyperCube(id,domainSize));
		else if (nodeType.equals(NODE_TYPE_VARIABLE_NODE))
			comingMessages.put(id,HyperCube.createZeroHyperCube(parent.getId(),parent.getDomainSize()));
		if (blocking){
			sendFlag.put(id,false);
			receiveFlag.put(id,false);
		}
	}

	protected boolean evaluateHandleCondition(){
		if (!blocking)
			return true;
		int notReceiveId = -1;
		int notReceiveCount = 0;
		for (int id : receiveFlag.keySet()){
			if (!receiveFlag.get(id)) {
				notReceiveCount++;
				notReceiveId = id;
			}
		}
		if (notReceiveCount > 1)
			return false;
		if (notReceiveCount > 0 && sendFlag.get(notReceiveId))
			return false;
		if (notReceiveCount > 0)
			targetId = notReceiveId;
		return true;
	}

	public abstract Message[] handle();

	protected Set<Integer> getDest(){
		return comingMessages.keySet();
	}

	protected Set<Integer> getSource() {
		return comingMessages.keySet();
	}

	protected boolean evaluateFindOptimalCondition(){
		return targetId == -1;
	}
}
