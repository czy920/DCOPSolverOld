package com.cqu.synchronousqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cqu.core.EventListener;
import com.cqu.core.Message;
import com.cqu.util.FormatUtil;

public class MessageMailerSynchronous extends FakeQueueMessager{

	private AgentManagerSynchronous agentManager;
	private List<Map<String, Object>> results;
	
	private long timeStart=0;
	private long timeEnd=0;
	
	private int messageQuantity;
	private int messageLostQuantity;
	
	private List<EventListener> eventListeners;
	
	public MessageMailerSynchronous(AgentManagerSynchronous agentManager) {
		// TODO Auto-generated constructor stub
		super();
		this.agentManager=agentManager;
		results=new ArrayList<Map<String, Object>>();
		
		messageQuantity=0;
		messageLostQuantity=0;
		
		eventListeners=new ArrayList<EventListener>();
	}
	
	public void execute()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initRun();
				while(isFinished()==false)
				{
					runProcess();
					for(AgentSynchronous agent : agentManager.getAgents().values())
					{
						agent.runProcess();
					}
				}
				runFinished();
			}
		}).start();
	}
	
	public void setResult(Map<String, Object> result)
	{
		results.add(result);
		if(results.size()>=this.agentManager.getAgentCount())
		{
			this.stopRunning();
		}
	}
	
	public List<Map<String, Object>> getResults()
	{
		return this.results;
	}
	
	public String easyMessageContent(Message msg)
	{
		return this.agentManager.easyMessageContent(msg);
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		messageQuantity++;
		
		agentManager.getAgent(msg.getIdReceiver()).addMessage(msg);
	}
	
	private void initRun() {
		timeStart=System.currentTimeMillis();
		agentManager.initAgents(this);
	}
	
	private void runFinished() {
		this.agentManager.printResults(results);
		System.out.println(
				"messageQuantity: "+messageQuantity+
				" messageLostQuantity: "+messageLostQuantity+
				" lostRatio: "+FormatUtil.format(messageLostQuantity*100.0/(messageQuantity+messageLostQuantity), "#.0")+"%");
		//Debugger.printValueChanges();
		
		timeEnd=System.currentTimeMillis();
		System.out.println("Mailer stopped, totalTime: "+(timeEnd-timeStart)+"ms");
		
		for(EventListener el : this.eventListeners)
		{
			el.onFinished();
		}
	}
	
	public void addEventListener(EventListener el)
	{
		this.eventListeners.add(el);
	}
	
	public void removeEventListener(EventListener el)
	{
		this.eventListeners.remove(el);
	}
}
