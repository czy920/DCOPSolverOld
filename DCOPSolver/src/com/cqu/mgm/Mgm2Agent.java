package com.cqu.mgm;

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

public class Mgm2Agent extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_OFFER_MESSAGE=1;
	public final static int TYPE_WAIT_MESSAGE=2;
	public final static int TYPE_ACCEPT_MESSAGE=3;
	public final static int TYPE_REJECT_MESSAGE=4;
	public final static int TYPE_WAITGAIN_MESSAGE=5;
	public final static int TYPE_GAIN_MESSAGE=6;
	public final static int TYPE_COGAIN_MESSAGE=7;
	public final static int TYPE_DECIDEGO_MESSAGE=8;
	public final static int TYPE_WAITAGAIN_MESSAGE=9;
	
	public final static String TYPE_OFFER="type_offer";
	public final static String TYPE_RECEIVER="type_receiver";
	public final static String TYPE_UNKNOW="type_unknow";
	
	private static double p;
	private static int cycleCountEnd;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	
	private int wrong;
	private int wrongNumber;
	private int receivedWrongNumber = 0;
	
	private int nccc = 0;
	private int gainValue;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;	
	private int neighboursGain[];
	private int[] neighboursValueIndex;	
	
	private String isAbleToGo="not";
	private int coordinate;
	private LinkedList<int[]> selectValueGroup;
	private LinkedList<int[]> selectOfferGroup;
	private String ownType;
	
	public Mgm2Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	protected void initRun() {
		super.initRun();

		cycleCountEnd = Settings.settings.getCycleCountEnd();
		p = Settings.settings.getSelectProbability();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		coordinate=-1;
		ownType=TYPE_UNKNOW;
		neighboursQuantity=neighbours.length;

		neighboursValueIndex = new int[neighboursQuantity];
		selectValueGroup=new LinkedList<int[]>();
		selectOfferGroup=new LinkedList<int[]>();
		neighboursGain=new int[neighboursQuantity];
		
		sendValueMessages();
	}
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendOfferMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], Mgm2Agent.TYPE_OFFER_MESSAGE, selectValueGroup);
		this.sendMessage(msg);
	}
	
	private void sendWaitMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_WAIT_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendAcceptMessages(int[] temp){
		Message msg=new Message(this.id, neighbours[coordinate], Mgm2Agent.TYPE_ACCEPT_MESSAGE, temp);
		this.sendMessage(msg);
	}
	
	private void sendRejectMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_REJECT_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendWaitGainMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_WAITGAIN_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity ; neighbourIndex++){
			if(neighbourIndex!=coordinate){
				Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_GAIN_MESSAGE, gainValue);
				this.sendMessage(msg);
			}
		}
	}
	
	private void sendCoGainMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], Mgm2Agent.TYPE_COGAIN_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	private void sendDecideGoMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], Mgm2Agent.TYPE_DECIDEGO_MESSAGE, isAbleToGo);
		this.sendMessage(msg);
	}
	
	private void sendWaitAgainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			if(neighbourIndex!=coordinate){
				Message msg=new Message(this.id, neighbours[neighbourIndex], Mgm2Agent.TYPE_WAITAGAIN_MESSAGE, valueIndex);
				this.sendMessage(msg);
			}
		}
	}
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighboursQuantity; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];		
		}
		return localCostTemp;
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
		// TODO 自动生成的方法存根
		
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		//System.out.println(cycleCount+"____"+this.id);
		
		//纠错部分，找到message未收全的Agent
		if(wrong == 1){
			receivedWrongNumber++;
			System.out.println("Agent "+this.id+"____"+"cycleCount "+cycleCount+"____"+"neighbour数 "+neighboursQuantity+"____"+"邻居"+msg.getIdSender()+"____"+"收到 "+wrongNumber+"____"+
					"第 "+receivedQuantity+"____"+"类型 "+msg.getType());
			if(receivedWrongNumber == wrongNumber){
				int ii = 1;
				ii = ii/0;
			}
		}
		
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" gainValue="+Infinity.infinityEasy(this.gainValue));
		}
		if(msg.getType()==Mgm2Agent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_OFFER_MESSAGE)
		{
			disposeOfferMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_WAIT_MESSAGE)
		{
			disposeWaitMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_ACCEPT_MESSAGE)
		{
			disposeAcceptMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_REJECT_MESSAGE)
		{
			disposeRejectMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_WAITGAIN_MESSAGE)
		{
			disposeWaitGainMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_GAIN_MESSAGE)
		{
			disposeGainMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_COGAIN_MESSAGE)
		{
			disposeCoGainMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_DECIDEGO_MESSAGE)
		{
			disposeDecideGoMessage(msg);
		}else if(msg.getType()==Mgm2Agent.TYPE_WAITAGAIN_MESSAGE)
		{
			disposeWaitAgainMessage(msg);
		}
	}
	
	private void disposeValueMessage(Message msg) {
		//System.out.println("value");
		if(receivedQuantity==0)
			cycleCount++;
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndex[senderIndex] = (Integer)(msg.getValue());
		
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
						selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];		
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
				
				isAbleToGo="not";
				coordinate=-1;
				ownType=TYPE_UNKNOW;
				selectValueGroup.clear();
				selectOfferGroup.clear();
				
				if(Math.random()<p){
					ownType=TYPE_OFFER;
					coordinate=(int)(Math.random()*neighboursQuantity);
					
					int selectGroupCost[] = new int[domain.length];
					for(int i=0; i<neighbourDomains.get(neighbours[coordinate]).length; i++){
						for(int j=0; j<domain.length; j++){
							selectGroupCost[j]=selectMinCost[j]-constraintCosts.get(neighbours[coordinate])[j][neighboursValueIndex[coordinate]]
									+constraintCosts.get(neighbours[coordinate])[j][i];
						}
						int findTheMin=0;
						for(int j=0; j<domain.length; j++){
							if(selectGroupCost[findTheMin] > selectGroupCost[j])
								findTheMin=j;
						}
						if(selectGroupCost[findTheMin]<newLocalCost){
							int[] temp=new int[3];
							temp[0]=i;
							temp[1]=findTheMin;
							temp[2]=localCost-selectGroupCost[findTheMin];
							selectValueGroup.add(temp);
						}
					}
					if(selectValueGroup.isEmpty()==false)
						sendOfferMessages();
					else{
						coordinate=-1;
						ownType=TYPE_UNKNOW;						
					}
				}
				for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
					if(neighbourIndex!=coordinate){
						sendWaitMessages(neighbourIndex);
					}
				}
			}
		}
		//System.out.println("value_end");
	}
	
	private void disposeOfferMessage(Message msg) {
		//System.out.println("offer");
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		if(ownType!=TYPE_OFFER){
			
			LinkedList<int[]> tempMap = ((LinkedList<int[]>)(msg.getValue()));
			int tempList[][] = new int[tempMap.size()][4];
			for(int i=0; i<tempMap.size(); i++){
				tempList[i][0]=tempMap.get(i)[0];
				tempList[i][1]=tempMap.get(i)[1];
				tempList[i][2]=tempMap.get(i)[2];
				tempList[i][3]=senderIndex;
			}
			
			for(int i=0; i<tempList.length; i++){
				tempList[i][2]+=localCost;
				tempList[i][2]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
				
				for(int j=0; j<neighbours.length; j++){
					if(j!=senderIndex)
						tempList[i][2]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
				}
				increaseNccc();
			}
			int selectTemp=0;
			for(int i=1; i<tempList.length; i++){
				if(tempList[selectTemp][2] < tempList[i][2])
					selectTemp=i;
			}
			if(tempList[selectTemp][2] > 0){
				selectOfferGroup.add(tempList[selectTemp]);
			}
			else
				sendRejectMessages(senderIndex);
				
			if(receivedQuantity==0 && selectOfferGroup.isEmpty() != true){
				gather();
			}
			
		}else
			sendRejectMessages(senderIndex);
		//System.out.println("offer_end");
	}
	
	private void disposeWaitMessage(Message msg) {
		//System.out.println("wait");
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		sendWaitGainMessages(senderIndex);
		
		if(receivedQuantity==0 && selectOfferGroup.isEmpty() != true){
			gather();
		}
		//System.out.println("wait_end");
	}
	
	private void gather() {
		//System.out.println("gather");
		int temp[]=selectOfferGroup.get(0);
		for(int i=1; i<selectOfferGroup.size(); i++){
			if(temp[2]<selectOfferGroup.get(i)[2]){
				sendRejectMessages(temp[3]);
				temp=selectOfferGroup.get(i);
			}
			else{
				sendRejectMessages(selectOfferGroup.get(i)[3]);
			}
		}
		ownType=TYPE_RECEIVER;
		selectValueIndex=temp[0];
		gainValue=temp[2];
		coordinate=temp[3];
		sendAcceptMessages(temp);
		//System.out.println("gather_end");
	}
	
	private void disposeAcceptMessage(Message msg) {
		//System.out.println("accept");
		selectValueIndex=((int[])(msg.getValue()))[1];
		gainValue=((int[])(msg.getValue()))[2];
		
		if(receivedQuantity==0){
			sendGainMessages();
			if(coordinate!=-1){
				sendCoGainMessages();
			}
		}
		//System.out.println("accept_end");
	}
	
	private void disposeRejectMessage(Message msg) {
		//System.out.println("reject");
		ownType=TYPE_UNKNOW;
		coordinate=-1;
		if(receivedQuantity==0){
			sendGainMessages();
		}
		//System.out.println("reject_end");
	}
	
	private void disposeWaitGainMessage(Message msg) {
		//System.out.println("waitgain");
		if(receivedQuantity==0){
			if(coordinate!=-1){	
				sendCoGainMessages();
			}
			sendGainMessages();
		}		
		//System.out.println("waitgain_end");
	}
	
	private void disposeGainMessage(Message msg) {
		//System.out.println("gain");
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
				if(i!=coordinate){
					if(neighboursGain[i]>=gainValue){
						isAbleToGo="not";
						if(coordinate!=-1){	
							sendDecideGoMessages();
						}
						sendWaitAgainMessages();
						return;
					}
				}
			}
			isAbleToGo="go";
			if(coordinate!=-1){	
				sendDecideGoMessages();
			}
			sendWaitAgainMessages();
		}

		//System.out.println("gain_end");
	}
	private void disposeCoGainMessage(Message msg){
		//System.out.println("cogain");
		if(receivedQuantity==0){
			for(int i=0; i<neighboursQuantity; i++){
				if(i!=coordinate){
					if(neighboursGain[i]>=gainValue){
						isAbleToGo="not";
						if(coordinate!=-1){	
							sendDecideGoMessages();
						}
						sendWaitAgainMessages();
						return;
					}
				}
			}
			isAbleToGo="go";
			if(coordinate!=-1){	
				sendDecideGoMessages();
			}
			sendWaitAgainMessages();
		}
		//System.out.println("cogain_end");
	}
	private void disposeDecideGoMessage(Message msg) {
		//System.out.println("decide");
		if((String)(msg.getValue()) == "go" && isAbleToGo == "go")
			valueIndex=selectValueIndex;
		if(receivedQuantity==0){
			sendValueMessages();
		}
		//System.out.println("decide_end");
	}
	
	private void disposeWaitAgainMessage(Message msg) {
		//System.out.println("waitagain");
		if(receivedQuantity==0){
			if(isAbleToGo=="go" && coordinate == -1)
				valueIndex=selectValueIndex;
			sendValueMessages();
		}
		//System.out.println("waitagain_end");
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
		//System.out.println("Agent "+this.name+" stopped!");
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
			
			//String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			//System.out.println(displayStr);
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
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+Mgm2Agent.messageContent(msg);
	}
	
	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case Mgm2Agent.TYPE_VALUE_MESSAGE :
			int val=(Integer) msg.getValue();
			int valueIndex=val;
			return "value["+valueIndex+"]";
			
		case Mgm2Agent.TYPE_OFFER_MESSAGE :
			return "offer[ ]";
			
		case Mgm2Agent.TYPE_WAIT_MESSAGE :
			return "wait[ ]";
			
		case Mgm2Agent.TYPE_ACCEPT_MESSAGE :
			return "accept[ ]";
			
		case Mgm2Agent.TYPE_REJECT_MESSAGE :
			return "reject[ ]";
			
		case Mgm2Agent.TYPE_WAITGAIN_MESSAGE:
			return "wait_gain[ ]";
			
		case Mgm2Agent.TYPE_GAIN_MESSAGE :
			int gainValue=(Integer) msg.getValue();
			return "gain["+gainValue+"]";
			
		case Mgm2Agent.TYPE_COGAIN_MESSAGE :
			return "co_gain[ ]";
			
		case Mgm2Agent.TYPE_DECIDEGO_MESSAGE :
			return "decide_go["+(String)(msg.getValue())+"]";
			
		case Mgm2Agent.TYPE_WAITAGAIN_MESSAGE :
			return "wait_again[ ]";
			
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
