package com.cqu.dsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class DsaA_Agent extends AgentCycle {
	
	public final static int TYPE_VALUE_MESSAGE=0;
	private static int cycleCountEnd;
	private static double p;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";

	private int nccc = 0;
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	private int wrong;
	private int wrongNumber;
	private int receivedWrongNumber = 0;
		
	public DsaA_Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		p = Settings.settings.getSelectProbability();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		for(int i=0; i<neighbours.length; i++)
			neighboursValueIndex.put((Integer)i, (Integer)0);
		sendValueMessages();
	}
	
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], DsaA_Agent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}


	protected void work(int i){
		wrong = 0;
		if(i != neighboursQuantity){
			wrong = 1;
			wrongNumber = i;
		}
	}
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub		
		if(receivedQuantity==0)
			cycleCount++;
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
		//纠错部分，找到message未收全的Agent
		if(wrong == 1){
			receivedWrongNumber++;
			System.out.println("Agent "+this.id+"____"+"cycleCount "+cycleCount+"____"+"neighbour数 "+neighboursQuantity+"____"+"邻居"+msg.getIdSender()+"____"+"收到 "+wrongNumber+"____"+
					"第 "+receivedQuantity+"____");
			if(receivedWrongNumber == wrongNumber){
				int ii = 1;
				ii = ii/0;
			}
		}
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
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
				if(Math.random()<p){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
							//if(this.id < neighbours[j])
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];		
							//else
							//	selectMinCost[i]+=constraintCosts.get(neighbours[j])[neighboursValueIndex.get(j)][i];	
						}					
					}
					int selectValueIndex=0;
					int selectOneMinCost=selectMinCost[0];
					for(int i = 1; i < domain.length; i++){
						if(selectOneMinCost > selectMinCost[i]){
							selectOneMinCost = selectMinCost[i];
							selectValueIndex = i;
						}
					}
					if(selectOneMinCost < localCost){
						valueIndex = selectValueIndex;
					}
					nccc++;
				}
				sendValueMessages();
			}
		}
	}
	
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighbours.length; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
		}
		return localCostTemp;
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
		System.out.println("Agent "+this.name+" stopped!");
	}
	
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
		double totalCost=0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
			int id_=(Integer)result.get(KEY_ID);
			String name_=(String)result.get(KEY_NAME);
			int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			totalCost+=((double)((Integer)result.get(KEY_LOCALCOST)))/2;
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost)+
				" nccc: "+Infinity.infinityEasy((int)ncccTemp));
		
		ResultCycle ret=new ResultCycle();
		ret.nccc=(int)ncccTemp;
		ret.totalCost=(int)totalCost;
		return ret;
	}

	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+DsaA_Agent.messageContent(msg);
	}

	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case DsaA_Agent.TYPE_VALUE_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			int valueIndex=val;
			return "value["+valueIndex+"]";
		}
		default:
			return "unknown";
		}
	}
	
	
	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}

}
