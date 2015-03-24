package org.cqu.core.control;

import java.util.Map;

import org.cqu.core.control.asynchronous.QueueMessager;
import org.cqu.statistics.StatisticsListener;

public final class MessageMailer implements QueueMessagerListener, MessageSink{
	
	private Map<Integer, ? extends MessageSink> msgSinks;
	private QueueMessager queueMessager;
	private StatisticsListener statisticsListener;
	private final int capacity;
	
	public MessageMailer(StatisticsListener statisticsListener, int capacity) {
		// TODO Auto-generated constructor stub
		this.statisticsListener=statisticsListener;
		this.capacity=capacity;
	}
	
	public void setMessageSinks(Map<Integer, ? extends MessageSink> msgSinks)
	{
		this.msgSinks=msgSinks;
	}
	
	/*public void setResult(Map<String, Object> result)
	{
		synchronized (results) {
			results.add(result);
			if(results.size()>=this.agentManager.getAgentCount())
			{
				this.stopRunning();
			}
		}
	}*/
	
	/*@Override
	protected void finalizeProcess() {
		// TODO Auto-generated method stub
		super.finalizeProcess();
		
		Result resultReturned=(Result) this.agentManager.printResults(results);
		System.out.println(
				"messageQuantity: "+messageQuantity+
				" messageLostQuantity: "+messageLostQuantity+
				" lostRatio: "+FormatUtil.format(messageLostQuantity*100.0/(messageQuantity+messageLostQuantity), "#.0")+"%");
		//Debugger.printValueChanges();
		
		timeEnd=System.currentTimeMillis();
		
		resultReturned.messageQuantity=messageQuantity;
		resultReturned.lostRatio=(int)(messageLostQuantity*100.0/(messageQuantity+messageLostQuantity));
		resultReturned.totalTime+=(timeEnd-timeStart);
		resultReturned.agentValues=agentManager.getAgentValues();
		
		System.out.println("Mailer stopped, totalTime: "+resultReturned.totalTime+"ms");

		for(EventListener el : this.eventListeners)
		{
			el.onFinished(resultReturned);
		}
	}*/
	
	public void start()
	{
		if(this.queueMessager==null)
		{
			this.queueMessager=QueueMessager.newQueueMessager("MessageMailer", this, capacity);
			this.queueMessager.startProcess();
		}
	}

	@Override
	public void messageAdded(Message msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageTaken(Message msg) {
		// TODO Auto-generated method stub
		msgSinks.get(msg.getIdReceiver()).addMessage(msg);
	}

	@Override
	public void messageLost(Message msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addMessage(Message msg) {
		// TODO Auto-generated method stub
		queueMessager.addMessage(msg);
	}

	@Override
	public void initProcess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEnded(Object data) {
		// TODO Auto-generated method stub
	}
}
