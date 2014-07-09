package com.cqu.dpop;

import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;

public class DPOPAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_UTIL_MESSAGE=1;
	
	public DPOPAgent(int id, String name, int[] domain) {
		super(id, name, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		if(this.isLeafAgent()==true)
		{
			sendUtilMessage();
		}
	}

	@Override
	public void printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String easyMessageContent(Message msg, Agent sender, Agent receiver) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		int type=msg.getType();
		if(type==TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(type==TYPE_UTIL_MESSAGE)
		{
			disposeUtilMessage(msg);
		}
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
	private void sendUtilMessage()
	{
		if(this.isRootAgent()==true)
		{
			
		}else
		{
			
		}
	}
	
	private void disposeUtilMessage(Message msg)
	{
		if(this.isRootAgent()==true)
		{
			sendValueMessage();
		}
	}
	
	private void sendValueMessage()
	{
		
	}
	
	private void disposeValueMessage(Message msg)
	{
		
	}

}
