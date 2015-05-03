package com.cqu.mgm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.Result;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class MgmAgent extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_GAIN_MESSAGE=1;
	private static int cycleCountEnd;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	
	private int gainValue;
	private int localCost;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;	
	private int neighboursGain[];
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	public MgmAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		
		valueIndex=0;
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		neighboursGain=new int[neighboursQuantity];
		for(int i=0; i<neighbours.length; i++)
			neighboursValueIndex.put((Integer)i, (Integer)0);	
		localCost=localCost();
		sendValueMessages();
	}
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], MgmAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], MgmAgent.TYPE_GAIN_MESSAGE, gainValue);
			this.sendMessage(msg);
		}
	}
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighboursQuantity; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
		}
		return localCostTemp;
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO 自动生成的方法存根
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" gainValue="+Infinity.infinityEasy(this.gainValue));
		}
		if(msg.getType()==MgmAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==MgmAgent.TYPE_GAIN_MESSAGE)
		{
			disposeGainMessage(msg);
		}
	}
	
	private void disposeValueMessage(Message msg) {
		// TODO 自动生成的方法存根
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndex.put((Integer)senderIndex, (Integer)msg.getValue());
		
		if(receivedQuantity==0){
			localCost=localCost();
			
			if(cycleCount>=cycleCountEnd){
				stopRunning();
			}else{
				int[] selectMinCost=new int[domain.length];
				for(int i=0; i<domain.length; i++){
					selectMinCost[i]=0;
				}
				for(int i=0; i<domain.length; i++){
					for(int j=0; j<neighboursQuantity; j++){
						selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];		
					}					
				}
				int newLocalCost=localCost;
				for(int i=0; i<domain.length; i++){
					if(selectMinCost[i]<newLocalCost){
						newLocalCost=selectMinCost[i];
						selectValueIndex=i;
					}
				}
				gainValue=localCost-newLocalCost;
				
				cycleCount++;
				sendGainMessages();
			}
		}
	}
	
	private void disposeGainMessage(Message msg) {
		// TODO 自动生成的方法存根
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursGain[senderIndex]=(Integer)msg.getValue();
		
		if(receivedQuantity==0){
			for(int i=0; i<neighboursQuantity; i++){
				if(neighboursGain[i]>=gainValue){
					sendValueMessages();
					return;
				}
			}
			valueIndex=selectValueIndex;
			sendValueMessages();
		}
	}

	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		result.put(KEY_LOCALCOST, this.localCost);
		
		this.msgMailer.setResult(result);
		System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO 自动生成的方法存根
		double totalCost=0;
		for(Map<String, Object> result : results){
			
			int id_=(Integer)result.get(KEY_ID);
			String name_=(String)result.get(KEY_NAME);
			int value_=(Integer)result.get(KEY_VALUE);
			totalCost+=((double)((Integer)result.get(KEY_LOCALCOST)))/2;
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost));
		
		Result ret=new Result();
		ret.totalCost=(int)totalCost;
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+MgmAgent.messageContent(msg);
	}
	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case MgmAgent.TYPE_VALUE_MESSAGE :
			int val=(Integer) msg.getValue();
			int valueIndex=val;
			return "value["+valueIndex+"]";
		case MgmAgent.TYPE_GAIN_MESSAGE :
			int gainValue=(Integer) msg.getValue();
			return "gain["+gainValue+"]";
		default:
			return "unknown";
		}
	}
	
	@Override
	protected void messageLost(Message msg) {
		// TODO 自动生成的方法存根
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}
	
}
