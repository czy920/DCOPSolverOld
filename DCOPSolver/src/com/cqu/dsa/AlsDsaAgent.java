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
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

//Anytime固定框架的DSA算法
//改良具备反馈机制的ALS框架固定算法

public class AlsDsaAgent extends AgentCycleAls {
	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_ALSCOST_MESSAGE=1;
	public final static int TYPE_ALSBEST_MESSAGE=2;
	
	private static int cycleCountEnd;
	private static double p;
	private static int bestValueBox;
	private static double degradation;
	
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
	private int newBestCost = 2147483647;
	private int[] valueWeight;
	private int bestValue;
	private int[][] topBestValue;
	private int bestValueNumber = 0;
	//private int decisionMax;
	private int accumulativeCost = 0;
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
		bestValueBox = 5;
		degradation = 0.01;
			
		localCost=2147483647;
		//decisionMax = (int)(bestValueBox*(1-degradation)/degradation)+1;
		valueWeight = new int[domain.length];
		topBestValue = new int[2][bestValueBox];
		valueIndex=(int)(Math.random()*(domain.length));
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		
		for(int i = 0; i < bestValueBox; i++)
			topBestValue[0][i] = 2147483647;
		for(int i = 0;i < domain.length; i++)
			valueWeight[i] = 5000;
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
	
	protected void sendAlsCostMessage(){
		Message msg=new Message(this.id, this.parent, AlsDsaAgent.TYPE_ALSCOST_MESSAGE, this.accumulativeCost);
		this.sendMessage(msg);
	}
	
	protected void sendAlsBestMessage(){
		for(int i = 0; i < children.length; i++){
			Message msg=new Message(this.id, children[i], AlsDsaAgent.TYPE_ALSBEST_MESSAGE, newBestCost);
			this.sendMessage(msg);
		}
	}
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(msg.getType() == AlsDsaAgent.TYPE_VALUE_MESSAGE){
			disposeValueMessage(msg);
		}
		else if(msg.getType() == AlsDsaAgent.TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == AlsDsaAgent.TYPE_ALSBEST_MESSAGE){
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
			//进行ALS框架操作
			AlsWork();
			
			if(cycleCount>=cycleCountEnd){
				STOPRUNNING = true;
				//stopRunning();
				//!!!!!!!!!!!!!!!!!!!!在Als框架下，线程终止操作不在这里进行!!!!!!!!!!!!!!!!!!!!
			}else{
				
				if(Math.random()<p){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];
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
					
					int max=0,min=0;
					for(int i = 1; i < domain.length; i++){
						if(valueWeight[i] > valueWeight[max])
							max = i;
						else if(valueWeight[i] < valueWeight[min])
							min = i;
					}
					int defference = valueWeight[max] - valueWeight[min];
					double preDecision = defference/valueWeight[max];
					
					if(Math.random() > preDecision){
						if(selectOneMinCost < localCost)
							valueIndex = selectValueIndex;
					}
					else{
						int decision = 0;
						for(int i = 0; i < domain.length; i++){
							decision += valueWeight[i];
						}
						decision = (int)(decision * Math.random());
						for(int i = 0; i < domain.length; i++){
							if(decision < valueWeight[i]){
								valueIndex = i;
								break;
							}
							else{
								decision -= valueWeight[i];
							}
						}
					}
					nccc++;
				}
				sendValueMessages();
			}
		}
	}
	
	protected void disposeAlsCostMessage(Message msg){
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
				accumulativeCost = accumulativeCost/2;
				if(accumulativeCost < bestCost){
					bestCost = accumulativeCost;
					bestValue = valueIndexList.removeFirst();
					newBestCost = bestCost;
				}
				else{
					valueIndexList.removeFirst();
					newBestCost = 2147483647;
				}
				sendAlsBestMessage();
				//System.out.println("cycleCount~~~"+cycleCount+"~~~bestCost~~~"+bestCost);
			}
		}
		if(valueIndexList.isEmpty() == true && STOPRUNNING == true){
			//bestCost = bestCost/2;
			stopRunning();
		}
		//System.out.println("Agent "+this.name+"~~~~~~"+cycleCount);
	}
	
	protected void disposeAlsBestMessage(Message msg){
		if((Integer)msg.getValue() != 2147483647){
			bestValue = valueIndexList.remove();
			newBestCost = (Integer)msg.getValue();
			if(bestValueNumber == 0){
				topBestValue[0][0] = bestValue;
				topBestValue[1][0] = newBestCost;
			}
			else{
				if(bestValueNumber == bestValueBox){
					for(int i = bestValueNumber - 1; i > 0; i--){
						topBestValue[0][i] = topBestValue[0][i-1];
						topBestValue[1][i] = topBestValue[1][i-1];
					}
				}
				else{
					for(int i = bestValueNumber; i > 0; i--){
						topBestValue[0][i] = topBestValue[0][i-1];
						topBestValue[1][i] = topBestValue[1][i-1];
					}
				}
				topBestValue[0][0] = bestValue;
				topBestValue[1][0] = newBestCost;
			}
		}
		else{
			if(bestValueNumber == bestValueBox){
				for(int i = bestValueNumber - 1; i > 0; i--){
					topBestValue[0][i] = topBestValue[0][i-1];
					topBestValue[1][i] = topBestValue[1][i-1];
				}
			}
			else{
				for(int i = bestValueNumber; i > 0; i--){
					topBestValue[0][i] = topBestValue[0][i-1];
					topBestValue[1][i] = topBestValue[1][i-1];
				}
			}
			topBestValue[0][0] = topBestValue[0][1];
			topBestValue[1][0] = topBestValue[1][1];
			valueIndexList.remove();
			newBestCost = 2147483647;
		}
		if(bestValueNumber < bestValueBox)
			bestValueNumber++;
		
		for(int i = 0; i < domain.length; i++)
			if(valueWeight[i] > 1)
				valueWeight[i] = (int)(valueWeight[i]*(1-degradation));
		for(int i = 0; i < bestValueNumber; i++)
			valueWeight[topBestValue[0][i]]++;
		
		if(this.isLeafAgent() == false)
			sendAlsBestMessage();
		
		if(valueIndexList.isEmpty() == true)
			stopRunning();
		//System.out.println("Agent "+this.name+"~~~best~~~"+cycleCount);
	}
		
	protected void AlsWork(){
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
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
		}
		return localCostTemp;
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
		result.put(KEY_VALUE, this.bestValue);
		result.put(KEY_LOCALCOST, this.bestCost);
		result.put(KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
		//System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		
		double totalCost=0;
		int ncccTemp = 0;
		int tag = 0;
		for(Map<String, Object> result : results){
			
			int id_=(Integer)result.get(KEY_ID);
			String name_=(String)result.get(KEY_NAME);
			int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			if(tag == 0){
				totalCost = ((double)((Integer)result.get(KEY_LOCALCOST)));
				tag ++;
			}
			
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
