package com.cqu.aco;

import java.util.LinkedList;

public class OneValueMsgContext {
	LinkedList<ValueMsgContext> msgContext;
	
	public OneValueMsgContext() {
		super();
		msgContext = new LinkedList<ValueMsgContext>();
	}

	public LinkedList<ValueMsgContext> getMsgContext() {
		return msgContext;
	}

	public void setMsgContext(LinkedList<ValueMsgContext> msgContext) {
		this.msgContext = msgContext;
	}

}
