package com.cqu.cyclequeue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cqu.aco.PublicConstants;
import com.cqu.core.EventListener;
import com.cqu.core.Message;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.main.Debugger;
import com.cqu.util.FormatUtil;

public class MessageMailerCycle extends MailerCycleQueueMessager{

    private List<Map<String, Object>> results;
    
    private AgentManagerCycle agentManager;
    
    private long timeStart=0;
    private long timeEnd=0;
	
    private int messageQuantity=0;
    private int messageLostQuantity=0;
    
    private double[] totalCostInCycle;
    private long[] timeCostInCycle;
    private int[] messageQuantityInCycle;
    
    //蚁群算法使用的保存每一轮的信息
    private int[] aco_totalCostInCycle;
    private int[] aco_bestCostInCycle;
	
	private List<EventListener> eventListeners;
	
	private boolean initFinished = false;
	
	public MessageMailerCycle(AgentManagerCycle agentManager) {
		// TODO Auto-generated constructor stub
		super("Mailer", agentManager.getAgentCount());
		this.agentManager=agentManager;
		results=new ArrayList<Map<String, Object>>();
		
		eventListeners=new ArrayList<EventListener>();
		
		this.totalCostInCycle = new double[999];
		this.timeCostInCycle = new long[999];
		this.messageQuantityInCycle = new int[999];
		
		this.aco_totalCostInCycle = new int[999];
		this.aco_bestCostInCycle = new int[999];
	}
	
	public void setResult(Map<String, Object> result)
	{
		synchronized (results) {
			results.add(result);
			if(results.size()>=this.agentManager.getAgentCount())
			{
				this.stopRunning();
			}
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

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		messageLostQuantity++;
		
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost "+this.easyMessageContent(msg));
		}
	}
	
	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		for(AgentCycle agent : this.agentManager.getAgents().values())
		{
			agent.setLocks(cycleBegin, cycleEnd, cycleEndCount, totalAgentCount, totalAgentCountTemp, OperateEndCount);
		}
		
		timeStart=System.currentTimeMillis();
		
		initFinished = true;
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
	
		//Debugger.printValueChanges();
		Result temp = (Result) this.agentManager.printResults(results);
		Result resultReturned;
		if(temp instanceof ResultCycle){
			//通过temp输出totalCostInCycle[]
			ResultCycle tempCycle = (ResultCycle) temp;
			tempCycle.totalCostInCycle = this.totalCostInCycle;
			tempCycle.timeCostInCycle = this.timeCostInCycle;
			tempCycle.messageQuantityInCycle = this.messageQuantityInCycle;
			tempCycle.ant_totalCostInCyle = PublicConstants.aco_totalCostInCycle;
			tempCycle.ant_bestCostInCycle = PublicConstants.aco_bestCostInCycle;
			resultReturned = (Result)tempCycle;
		}
		else
			resultReturned = temp;
		
		
		System.out.println(
				"messageQuantity: "+messageQuantity+
				" messageLostQuantity: "+messageLostQuantity+
				" lostRatio: "+FormatUtil.format(messageLostQuantity*100.0/(messageQuantity+messageLostQuantity), "#.0")+"%");
		//Debugger.printValueChanges();
		
		timeEnd=System.currentTimeMillis();
		
		resultReturned.messageQuantity=messageQuantity;
		resultReturned.lostRatio=(int)(messageLostQuantity*100.0/(messageQuantity+messageLostQuantity));
		resultReturned.totalTime=timeEnd-timeStart;
		resultReturned.agentValues=agentManager.getAgentValues();
		
		System.out.println("Mailer stopped, totalTime: "+resultReturned.totalTime+"ms");
		System.out.println("Cycle Count: "+this.cycleCount);
		
		for(EventListener el : this.eventListeners)
		{
			el.onFinished(resultReturned);
		}
	}

	//保存每个回合的totalCost
	protected void dataInCycleIncrease(){
		if(cycleCount == 0)													//除去初始化时Cost混乱时的统计
			return;
		if(cycleCount > totalCostInCycle.length){
			double[] templist1 = new double[2*totalCostInCycle.length];
			long[] templist2 = new long[2*totalCostInCycle.length];
			int[] templist3 = new int[2*totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++){
				templist1[i] = totalCostInCycle[i];
				templist2[i] = timeCostInCycle[i];
				templist3[i] = messageQuantityInCycle[i];
			}
			totalCostInCycle = templist1;
			timeCostInCycle = templist2;
			messageQuantityInCycle = templist3;
			//System.out.println(totalCostInCycle.length);
		}
		//System.out.println(cycleCount);
		totalCostInCycle[cycleCount-1] = agentManager.getTotalCost();
		timeCostInCycle[cycleCount-1] = System.currentTimeMillis()-timeStart;
		messageQuantityInCycle[cycleCount-1] = this.messageQuantity;
		//System.out.println(totalCostInCycle[cycleCount]+"~~~"+timeCostInCycle[cycleCount]+"~~~"+messageQuantityInCycle[cycleCount]);
	}
	
	//数组长度修正
	protected void dataInCycleCorrection(){
		double[] correctCost = new double[cycleCount-1];
		long[] correctTime = new long[cycleCount-1];
		int[] correctMessages = new int[cycleCount-1];
		for(int i = 0; i < cycleCount-1; i++){
			correctCost[i] = totalCostInCycle[i];
			correctTime[i] = timeCostInCycle[i];
			correctMessages[i] = messageQuantityInCycle[i];
		}
		totalCostInCycle = correctCost;
		timeCostInCycle = correctTime;
		messageQuantityInCycle = correctMessages;
		//System.out.println(totalCostInCycle.length+"~~~~~end");
	}
	
	/**
	 * 让Agent线程等待Mailer初始化完毕
	 */
	public void initWait(){
		while(!initFinished){
			
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
