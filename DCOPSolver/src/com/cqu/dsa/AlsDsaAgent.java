package com.cqu.dsa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class AlsDsaAgent extends AgentCycle {
	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_ALSCOST_MESSAGE=1;
	public final static int TYPE_ALSBEST_MESSAGE=2;
	
	private static int cycleCountEnd;
	private static double p;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	public final static String YES="Yes";
	public final static String NO="NO";
	
	private int nccc = 0;
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	private int bestCost = 2147483647;
	private int bestValue = 0;
	private int accumulativeCost = 0;
	private String isChanged = NO; 
	private boolean enoughReceived = false;
	private LinkedList<Integer> localCostList = new LinkedList<Integer>();
	private LinkedList<Integer> valueIndexList = new LinkedList<Integer>();
	private HashMap<Integer, LinkedList<Integer>> childrenMessageList = new HashMap<Integer, LinkedList<Integer>>();
	
	public AlsDsaAgent(int id, String name, int level, int[] domain) {
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
			Message msg=new Message(this.id, neighbours[neighbourIndex], AlsDsaAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendAlsCostMessage(){
		Message msg=new Message(this.id, this.parent, AlsDsaAgent.TYPE_ALSCOST_MESSAGE, this.accumulativeCost);
		this.sendMessage(msg);
	}
	
	private void sendAlsBestMessage(){
		for(int i = 0; i < children.length; i++){
			Message msg=new Message(this.id, children[i], AlsDsaAgent.TYPE_ALSBEST_MESSAGE, isChanged);
			this.sendMessage(msg);
		}
	}
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(msg.getType() == AlsDsaAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType() == AlsDsaAgent.TYPE_ALSCOST_MESSAGE)
		{
			disposeAlsCostMessage(msg);
		}else if(msg.getType() == AlsDsaAgent.TYPE_ALSBEST_MESSAGE)
		{
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}
	
	private void disposeValueMessage(Message msg){
		if(receivedQuantity==0)
			cycleCount++;
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
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
				//stopRunning();
			}else{
				//进行ALS框架操作
				AlsWork();
				
				if(Math.random()<p){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
							if(this.id < neighbours[j])
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];		
							else
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[neighboursValueIndex.get(j)][i];	
						}					
					}				
					for(int i=0; i<domain.length; i++){
						if(selectMinCost[i]<localCost){
							valueIndex=i;
						}
					}
					nccc++;
				}
				sendValueMessages();
			}
		}
	}
	
	private void disposeAlsCostMessage(Message msg){
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<children.length; i++){
			if(children[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		if(childrenMessageList.containsKey(senderIndex) == true){
			LinkedList<Integer> temp = childrenMessageList.remove(senderIndex);
			temp.add((Integer) msg.getValue());
			childrenMessageList.put(senderIndex, temp);
		}
		else{
			LinkedList<Integer> temp = new LinkedList<Integer>();
			temp.add((Integer) msg.getValue());
			childrenMessageList.put(senderIndex, temp);
		}
		
		enoughReceived = true;
		for(int i = 0; i < children.length; i++){
			if(childrenMessageList.containsKey(i) == false){
				enoughReceived = false;
				break;
			}
		}
		
		if(enoughReceived == true){
			
			accumulativeCost = localCostList.removeFirst();
			
			for(int i = 0; i < children.length; i++){
				LinkedList<Integer> temp = childrenMessageList.remove(i);
				accumulativeCost = accumulativeCost + temp.remove();
				if(temp.isEmpty() == false)
					childrenMessageList.put(i, temp);
			}
			
			if(this.isRootAgent() == false)
				sendAlsCostMessage();
			else{
				if(accumulativeCost < bestCost){
					bestCost = accumulativeCost;
					bestValue = valueIndexList.removeFirst();
					isChanged = YES;
					sendAlsBestMessage();
				}
				else{
					valueIndexList.removeFirst();
					isChanged = NO;
					sendAlsBestMessage();
				}
				//System.out.println("cycleCount~~~"+cycleCount+"~~~bestCost~~~"+bestCost/2);
			}
		}
		if(valueIndexList.isEmpty() == true){
			bestCost = bestCost/2;
			stopRunning();
		}
		//System.out.println("Agent "+this.name+"~~~cost~~~"+cycleCount);
	}
	
	private void disposeAlsBestMessage(Message msg){
		if((String)msg.getValue() == YES){
			bestValue = valueIndexList.remove();
			isChanged = YES;
		}
		else{
			valueIndexList.remove();
			isChanged = NO;
		}
		if(this.isLeafAgent() == false)
			sendAlsBestMessage();
		
		if(valueIndexList.isEmpty() == true)
			stopRunning();
		//System.out.println("Agent "+this.name+"~~~best~~~"+cycleCount);
	}
		
	private void AlsWork(){
		localCostList.add(localCost);
		valueIndexList.add(valueIndex);
		
		if(this.isLeafAgent() == true){
			accumulativeCost  = localCost;
			sendAlsCostMessage();
		}
		//System.out.println("Agent "+this.name+"~~~~~"+cycleCount);
	};
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighbours.length; i++){
			if(this.id < neighbours[i])
				localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
			else
				localCostTemp+=constraintCosts.get(neighbours[i])[neighboursValueIndex.get(i)][valueIndex];	
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
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AlsDsaAgent.messageContent(msg);
	}

	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case AlsDsaAgent.TYPE_VALUE_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			return "value["+val+"]";
		}case AlsDsaAgent.TYPE_ALSCOST_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			return "accumulativeCost["+val+"]";
		}case AlsDsaAgent.TYPE_ALSBEST_MESSAGE:
		{
			int[] val=(int[]) msg.getValue();
			return "bestStep["+val[0]+", bestValue["+val[1]+"]";
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
