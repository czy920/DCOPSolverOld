package org.cqu.core.control.asynchronous;

import java.util.LinkedList;

import org.cqu.core.control.Message;
import org.cqu.core.control.MessageSink;
import org.cqu.core.control.ProcessThread;
import org.cqu.core.control.QueueMessagerListener;


/**
 * 封装了消息队列中消息的存取逻辑
 * @author hz
 *
 */
public abstract class QueueMessager extends ProcessThread implements MessageSink{

	protected LinkedList<Message> msgQueue;
	protected QueueMessagerListener listener;
	
	public QueueMessager(String name, QueueMessagerListener listener) {
		super(name);
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
		this.listener=listener;
	}
	
	public static QueueMessager newQueueMessager(String name, QueueMessagerListener listener, int capacity)
	{
		if(capacity<1)
		{
			return new QueueMessagerUnlimited(name, listener);
		}else
		{
			return new QueueMessagerLimited(name, listener, capacity);
		}
	}

	@Override
	protected final void runProcess() {
		// TODO Auto-generated method stub
		if(listener!=null)
		{
			listener.initProcess();
		}
		while(isRunning()==true)
		{
			Message msg=null;
			synchronized (msgQueue) {
				while(msgQueue.isEmpty()==true)
				{
					try {
						msgQueue.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Thread.currentThread().interrupt();
						//当检测到中断消息时，认为是结束线程的通知，所以直接跳出循环
						break;
					}
				}
				msg=msgQueue.removeFirst();
			}
			if(listener!=null)
			{
				listener.messageTaken(msg);
			}
		}
		if(listener!=null)
		{
			listener.processEnded(null);
		}
	}
}
