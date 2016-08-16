package com.cqu.mgm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class MgmAgent extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_GAIN_MESSAGE=1;
	private static int cycleCountEnd;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	
	public final static int CYCLE_VALUE=567;
	public final static int CYCLE_GAIN=568;
	private int cycleTag = CYCLE_VALUE;
	private int nccc = 0;
	private int gainValue;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;	
	private int neighboursGain[];
	private int[] neighboursValueIndex;
	
	public MgmAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		neighboursQuantity=neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		neighboursGain=new int[neighboursQuantity];
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
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];		
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
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		/*
		if(cycleCount == 8){
			if(neighboursValueIndex.get(senderIndex) != msg.getValue()){
				System.out.println("agent"+this.id+"_______"+"neighbour_changed"+"________"+neighbours[senderIndex]);
			}
		}
		*/

		neighboursValueIndex[senderIndex] = (Integer)(msg.getValue());
		
		if(receivedQuantity==0){
			
		}
	}
	
	private void cycleValue(){
		if(cycleCount>=cycleCountEnd){
			stopRunning();
		}
		else{
			cycleCount++;	
			localCost=localCost();
			
			int[] selectMinCost=new int[domain.length];
			for(int i=0; i<domain.length; i++){
				selectMinCost[i]=0;
			}
			for(int i=0; i<domain.length; i++){
				for(int j=0; j<neighboursQuantity; j++){
//					if(this.id < neighbours[j])
						selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];		
//					else
//						selectMinCost[i]+=constraintCosts.get(neighbours[j])[neighboursValueIndex.get(j)][i];		
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
			increaseNccc();
			//System.out.println("agent"+this.id+"_______"+cycleCount+"_______"+gainValue+"________");
			sendGainMessages();
		}
	}
	
	private void disposeGainMessage(Message msg) {
		// TODO 自动生成的方法存根
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
			
		}
	}
	
	private void cycleGain(){
		for(int i=0; i<neighboursQuantity; i++){
			if(neighboursGain[i]>=gainValue){
//				sendValueMessages();
				return;
			}
		}
		valueIndex=selectValueIndex;
		//if(cycleCount == 9){
		//	System.out.println("agent"+this.id+"_______"+"Gain_ready"+"________"+gainValue);
		//}
		sendValueMessages();
	}

	protected void allMessageDisposed(){
		if(cycleTag == CYCLE_VALUE){
			cycleTag = CYCLE_GAIN;
			cycleValue();
		}
		else{
			cycleTag = CYCLE_VALUE;
			cycleGain();
		}
	}
	
	protected void localSearchCheck(){
		while(msgQueue.size() == 0){
			try {
				Thread.sleep(1);
				System.out.println("!!! sleep(1) !!!!!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(msgQueue.isEmpty() == true){
			System.out.println("!!!!! IsEmpty Judged Wrong !!!!!");
		}
	}
	
	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		result.put(KEY_LOCALCOST, this.localCost);
		result.put(KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
//		System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		double totalCost=0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
//			int id_=(Integer)result.get(KEY_ID);
//			String name_=(String)result.get(KEY_NAME);
//			int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			totalCost+=((double)((Integer)result.get(KEY_LOCALCOST)))/2;
			
//			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
//			System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost)+
				" nccc: "+Infinity.infinityEasy((int)ncccTemp));
		
		ResultCycle ret=new ResultCycle();
		ret.nccc=(int)ncccTemp;
		ret.totalCost=(int)totalCost;
		return ret;
	}

	private void increaseNccc(){
		nccc++;
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
