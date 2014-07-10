package com.cqu.dpop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Agent;
import com.cqu.core.Message;
import com.cqu.util.ArrayIndexComparator;

public class DPOPAgent extends Agent{

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_UTIL_MESSAGE=1;
	
	private int[] neighbourLevelSortIndexes;
	
	public DPOPAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		ArrayIndexComparator<Integer> comparator=new ArrayIndexComparator<Integer>(this.neighbourLevels);
		Arrays.sort(this.neighbourLevels, comparator);
		this.neighbourLevelSortIndexes=comparator.getSortIndexes();
		
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
			return;
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
		if(this.isLeafAgent()==true)
		{
			return;
		}
		int dataLength=1;
		Map<String, Integer> dimentionNames=new HashMap<String, Integer>();
		int[] dimentionLengths=new int[allParents.length];
		for(int i=0;i<allParents.length;i++)
		{
			int parentId=allParents[neighbourLevelSortIndexes[i]];
			dimentionNames.put(neighbourNames[neighbourLevelSortIndexes[i]], i);
			dimentionLengths[i]=neighbourDomains.get(parentId).length;
			dataLength=dataLength*dimentionLengths[i];
		}
		int[] data=new int[dataLength];
		//待续
		
		MulitiDimentionalData multiDimentionalData=new MulitiDimentionalData(data, dimentionLengths, dimentionNames);
		Message utilMsg=new Message(this.id, this.parent, TYPE_UTIL_MESSAGE, multiDimentionalData);
		this.sendMessage(utilMsg);
	}
	
	private void disposeValueMessage(Message msg)
	{
		
	}

}
