package org.cqu.core.control.asynchronous;

import org.cqu.core.control.Message;
import org.cqu.core.control.QueueMessagerListener;


public final class QueueMessagerUnlimited extends QueueMessager{

	public QueueMessagerUnlimited(String name, QueueMessagerListener listener) {
		super(name, listener);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addMessage(Message msg) {
		// TODO Auto-generated method stub
		synchronized (msgQueue) {
			//消息不可丢失
			msgQueue.add(msg);
			listener.messageAdded(msg);
			msgQueue.notifyAll();
		}
	}
}
