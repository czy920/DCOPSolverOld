package com.cqu.mgm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class AlsMgmAgent extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_GAIN_MESSAGE=1;
	public final static int TYPE_ALSCOST_MESSAGE=3;
	public final static int TYPE_ALSBEST_MESSAGE=4;
	private static int cycleCountEnd;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	public final static String YES="Yes";
	public final static String NO="NO";
	
	private int nccc = 0;
	private int gainValue;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;	
	private int neighboursGain[];
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	private int bestCost = 2147483647;
	private int bestValue = 0;
	private int accumulativeCost = 0;
	private String isChanged = NO; 
	private boolean enoughReceived = false;
	private LinkedList<Integer> localCostList = new LinkedList<Integer>();
	private LinkedList<Integer> valueIndexList = new LinkedList<Integer>();
	private HashMap<Integer, LinkedList<Integer>> childrenMessageList = new HashMap<Integer, LinkedList<Integer>>();
	
	public AlsMgmAgent(int id, String name, int level, int[] domain) {
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
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		neighboursGain=new int[neighboursQuantity];
		for(int i=0; i<neighbours.length; i++)
			neighboursValueIndex.put((Integer)i, (Integer)0);
		sendValueMessages();
	}
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMgmAgent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMgmAgent.TYPE_GAIN_MESSAGE, gainValue);
			this.sendMessage(msg);
		}
	}

	private void sendAlsCostMessage(){
		Message msg=new Message(this.id, this.parent, AlsMgmAgent.TYPE_ALSCOST_MESSAGE, this.accumulativeCost);
		this.sendMessage(msg);
	}
	
	private void sendAlsBestMessage(){
		for(int i = 0; i < children.length; i++){
			Message msg=new Message(this.id, children[i], AlsMgmAgent.TYPE_ALSBEST_MESSAGE, isChanged);
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
		if(msg.getType()==AlsMgmAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==AlsMgmAgent.TYPE_GAIN_MESSAGE)
		{
			disposeGainMessage(msg);
		}else if(msg.getType() == AlsMgmAgent.TYPE_ALSCOST_MESSAGE)
		{
			disposeAlsCostMessage(msg);
		}else if(msg.getType() == AlsMgmAgent.TYPE_ALSBEST_MESSAGE)
		{
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}
	
	private void disposeValueMessage(Message msg) {
		// TODO 自动生成的方法存根
		if(receivedQuantity==0)
			cycleCount++;
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
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
		
		neighboursValueIndex.put((Integer)senderIndex, (Integer)msg.getValue());
		
		if(receivedQuantity==0){
			/*
			if(cycleCount == 10){
				if(localCost < localCost()){
					System.out.println("agent"+this.id+"_______"+"Lost"+"________"+(localCost()-localCost));
				}
				else{
					System.out.println("agent"+this.id+"_______"+"Gain"+"________"+(localCost-localCost()));
				}
			}
			*/
			//System.out.println("agent"+this.id+"_______"+this.valueIndex);
			
			localCost=localCost();
			
			if(cycleCount>=cycleCountEnd){
				//stopRunning();
			}else{
				//进行ALS框架操作
				AlsWork();
				
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
				increaseNccc();
				//System.out.println("agent"+this.id+"_______"+cycleCount+"_______"+gainValue+"________");
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
			AlsWork();			//进行ALS框架操作
			
			for(int i=0; i<neighboursQuantity; i++){
				if(neighboursGain[i]>=gainValue){
					sendValueMessages();
					return;
				}
			}
			valueIndex=selectValueIndex;
			//if(cycleCount == 9){
			//	System.out.println("agent"+this.id+"_______"+"Gain_ready"+"________"+gainValue);
			//}
			sendValueMessages();
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
				System.out.println("cycleCount~~~"+cycleCount+"~~~bestCost~~~"+bestCost/2);
			}
		}
		if(valueIndexList.isEmpty() == true){
			bestCost = bestCost/2;
			stopRunning();
		}
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

	private void increaseNccc(){
		nccc++;
	}
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AlsMgmAgent.messageContent(msg);
	}
	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case AlsMgmAgent.TYPE_VALUE_MESSAGE :
			int val=(Integer) msg.getValue();
			int valueIndex=val;
			return "value["+valueIndex+"]";
		case AlsMgmAgent.TYPE_GAIN_MESSAGE :
			int gainValue=(Integer) msg.getValue();
			return "gain["+gainValue+"]";
		case AlsMgmAgent.TYPE_ALSCOST_MESSAGE:
			int val1=(Integer) msg.getValue();
			return "accumulativeCost["+val1+"]";
		case AlsMgmAgent.TYPE_ALSBEST_MESSAGE:
			int va2=(Integer) msg.getValue();
			return "bestValue["+va2+"]";
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
