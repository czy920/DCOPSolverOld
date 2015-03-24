package org.cqu.core.control.asynchronous;

import org.cqu.core.control.Message;
import org.cqu.core.control.QueueMessagerListener;


public final class QueueMessagerLimited extends QueueMessager{
	
	private final int capacity;
	
	public QueueMessagerLimited(String name, QueueMessagerListener listener, int capacity) {
		super(name, listener);
		// TODO Auto-generated constructor stub
		this.capacity=capacity;
	}

	@Override
	public void addMessage(Message msg) {
		// TODO Auto-generated method stub
		synchronized (msgQueue) {
			if(msgQueue.size()<capacity)
			{
				msgQueue.add(msg);
				listener.messageAdded(msg);
				msgQueue.notifyAll();
			}else
			{
				listener.messageLost(msg);
			}
		}
	}
}
