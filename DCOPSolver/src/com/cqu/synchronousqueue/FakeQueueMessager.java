package com.cqu.synchronousqueue;

import java.util.LinkedList;
import com.cqu.core.Message;

public abstract class FakeQueueMessager {
	
    private LinkedList<Message> msgQueue;
    protected boolean runFinished=false;
    
	public FakeQueueMessager() {
		super();
		// TODO Auto-generated constructor stub
		this.msgQueue=new LinkedList<Message>();
		this.runFinished=false;
	}
	
	protected void addMessage(Message msg)
	{
		if(this.runFinished==true)
		{
			return;
		}
		msgQueue.add(msg);
	}

	protected void runProcess() {
		// TODO Auto-generated method stub
		if(this.runFinished==true)
		{
			return;
		}
		Message msg=null;
		while(msgQueue.isEmpty()==false)
		{
			msg=msgQueue.removeLast();
			if(msg!=null)
			{
				disposeMessage(msg);
				if(this.isFinished()==true)
				{
					this.msgQueue.clear();
					break;
				}
			}
		}
	}
	
	protected void stopRunning()
	{
		this.runFinished=true;
	}
	
	protected boolean isFinished()
	{
		return this.runFinished;
	}
	
	protected abstract void disposeMessage(Message msg);
}
