package org.cqu.core.control;


public interface QueueMessagerListener {
	void initProcess();
	void messageAdded(Message msg);
	void messageLost(Message msg);
	void messageTaken(Message msg);
	void processEnded(Object data);
}
