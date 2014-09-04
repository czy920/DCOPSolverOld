package com.cqu.bnbmergeadopt;

import com.cqu.core.Message;

public abstract class Method {
	
	AgentModel agent;
	Method(AgentModel agent){
		this.agent=agent;
	}
	
	 public abstract void disposeValueMessage(Message msg);

	 public abstract void disposeCostMessage(Message msg);

	 public abstract void disposeThresholdMessage(Message msg);

	 public abstract void disposeTerminateMessage(Message msg);

	public abstract void initRun();
	 
	public abstract void sendValueMessages();
	
	public abstract void sendCostMessages();
	
	public abstract void sendTerminateMessages();
	
	public abstract void sendThresholdMessages();
	

}
