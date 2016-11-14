package com.cqu.mus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.settings.Settings;

//双个体的分布式遗传算法
public class AlsDgaFBAgent extends AgentCycleAls {
	
	public final static int TYPE_VALUE_COST_MESSAGE=0;
	public final static int TYPE_START_MESSAGE=111;
	public final static int TYPE_RESET_MESSAGE = 112;
	
	private static int cycleCountEnd;
	private static int stayCountInterval;
	private static double p;
	private static double pCross;
	private static double pMutate;
	
	
	private int neighboursQuantity;
	private int receivedQuantity = 0;
	private int cycleCount = 0;
//	private boolean crossoverLock = true;
//	private boolean crossoverMessage = false;
//	private int completeTag = 0;
	
	private int bestIndividual;
	private final static int NONE = 99999;
	private int[] accumulativeCost;
	protected LinkedList<int[]> localCostList = new LinkedList<int[]>();
	protected LinkedList<int[]> valueIndexList = new LinkedList<int[]>();
	protected HashMap<Integer, LinkedList<int[]>> childrenMessageList = new HashMap<Integer, LinkedList<int[]>>();
	
	private int population = 10;
	private int[] valueIndexGA;
	private int[] localCostGA;
	private int[] neighboursLocalCostGA;
//	private int areaCostGA;
//	private int[] neighboursAreaCostGA;
	private int[][] neighboursValueIndexGA;
	private int prepareToStart = 2147483647;
	
	private boolean crossTag0 = false;
	private boolean crossTag = false;
	private boolean[] resetTagPopulation;
	private int min = 0;
	private boolean wait = false;
	private boolean resetLock = false;
	private boolean resetTag = false;
	private int stayUnchanged = 0;
	private int prepareToReset = 2147483647;
	private int bestCostTemp = 2147483647;

	public AlsDgaFBAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}

	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		stayCountInterval = Settings.settings.getSelectInterval();
		p = Settings.settings.getSelectProbability();
		pCross = Settings.settings.getSelectProbabilityA();
		pMutate = Settings.settings.getSelectProbabilityB();
		
		neighboursQuantity=neighbours.length;
		valueIndexGA = new int[population];
		localCostGA = new int[population];
		neighboursLocalCostGA = new int[neighboursQuantity];
//		areaCostGA = 2147483647;
//		neighboursAreaCostGA = new int[neighboursQuantity];
		neighboursValueIndexGA = new int[population][neighboursQuantity];
		resetTagPopulation = new boolean[population];
		for(int i = 0; i < population; i++){
			localCostGA[i] = 2147483647;
			valueIndexGA[i] = (int)(Math.random()*(domain.length));
			resetTagPopulation[i] = false;
		}
		
		sendValueCostMessages();
	}
	
	private void sendValueCostMessages(){
		int[] box;
		if(resetTag == false){
			box = new int[population+2];
			
			//value。。。
			for(int i = 0; i< population; i++){
				box[i] = valueIndexGA[i];
			}
			
			//竞争
			min = 0;
			for(int i = 0; i < population; i++){
				if(localCostGA[i] < localCostGA[min])
					min = i;
			}
			box[population] = (localCostGA[min]*1000/neighboursQuantity);
			//标记
			
			if(crossTag0 == true){
				if(Math.random() < pCross){
					crossTag = true;
					box[population+1] = 1;
				}
				else{
					crossTag = false;
					box[population+1] = 0;
				}
			}
			else{
					crossTag = false;
					box[population+1] = 0;
			}
			
	//		if(Math.random() < pCross){
	//			crossTag = true;
	//			box[1] = 1;
	//		}
	//		else{
	//			crossTag = false;
	//			box[1] = 0;
	//		}
			
		}
		else{
			box = new int[population*2];
			for(int i = 0; i < population; i++){
				box[i] = valueIndexGA[i];
				if(Math.random() < 0.01){
					resetTagPopulation[i] = true;
					box[i+population] = 1;
				}
				else{
					resetTagPopulation[i] = false;
				}
			}
		}
		
		for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
			Message msg = new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_COST_MESSAGE, box);
			this.sendMessage(msg);
		}
	}
	
	protected void sendAlsCostMessage(){
		Message msg=new Message(this.id, this.parent, AgentCycleAls.TYPE_ALSCOST_MESSAGE, accumulativeCost);
		this.sendMessage(msg);
	}
	
	protected void sendAlsBestMessage(){
		for(int i = 0; i < children.length; i++){
			Message msg=new Message(this.id, children[i], AgentCycleAls.TYPE_ALSBEST_MESSAGE, bestIndividual);
			this.sendMessage(msg);
		}
	}
	
	private void sendStartMessages(){
		for(int i = 0; i < children.length; i++){
			Message msg = new Message(this.id, children[i], TYPE_START_MESSAGE, prepareToStart - 1);
			this.sendMessage(msg);
		}
	}
	
	private void sendResetMessages(){
		for(int i = 0; i < children.length; i++){
			Message msg = new Message(this.id, children[i], TYPE_RESET_MESSAGE, prepareToReset - 1);
			this.sendMessage(msg);
		}
	}
	
	protected void disposeMessage(Message msg) {
		if(msg.getType() == TYPE_VALUE_COST_MESSAGE){
			disposeValueCostMessage(msg);
		}
		else if(msg.getType() == TYPE_START_MESSAGE){
			disposeStartMessage(msg);
		}
		else if(msg.getType() == TYPE_RESET_MESSAGE){
			disposeResetMessage(msg);
		}
		else if(msg.getType() == TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}
		else
			System.out.println("wrong!!!!!!!!");
	}
	
	private void disposeValueCostMessage(Message msg){
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		for(int i = 0; i < population; i++){
			neighboursValueIndexGA[i][senderIndex] = ((int[])(msg.getValue()))[i];
		}
		neighboursLocalCostGA[senderIndex] =  ((int[])(msg.getValue()))[population];
		if(resetTag == false){
			if(((int[])(msg.getValue()))[population+1] == 1)
				crossTag = true;
		}
		else{
			for(int i = 0; i < population; i++){
				if(((int[])(msg.getValue()))[population+i] == 1){
					resetTagPopulation[i] = true;
				}
			}
		}
	}
	
	protected void allMessageDisposed(){
		if(cycleCount <= cycleCountEnd){
			if(prepareToStart == 0){
				crossTag0 = true;
				for(int i = 0; i < neighboursQuantity;i++){
					if(localCostGA[min]*1000/neighboursQuantity > neighboursLocalCostGA[i]){
						crossTag0 = false;
						break;
					}
				}
			}
			localCost();
			AlsWork();
			cycleCount++;

			if(prepareToStart > 0)
				prepareToStart--;
			if(prepareToStart == 0){
				
				prepareToReset--;
				if(prepareToReset > 0){
					if(resetTag == false){
						if(crossTag == true){
							int temp = valueIndexGA[0];
							for(int i = 0; i < population-1; i++){
								valueIndexGA[i] = valueIndexGA[i+1];
							}
							valueIndexGA[population-1] = temp;
						}
					}
					else{
						for(int i = 0; i < population; i++){
							if(resetTagPopulation[i] == true){
								valueIndexGA[i] = bestValue;
							}
						}
						resetTag = false;
						
//						for(int i = 0; i < population; i++){
//							valueIndexGA[i] = (int)(domain.length*Math.random());
//						}
//						resetTag = false;
						
					}
				}
				else{
//					if(crossTag == true){
//						for(int i = 0; i < population; i++){
//							valueIndexGA[i] = bestValue;
//						}
//					}
					resetTag = true;
					
					prepareToReset = 2147483647;
					resetLock = false;
				}
			}
			
			mutate();
			DsaWork();
			
			crossTag = false;
			sendValueCostMessages();
		}
		else{
			AlsStopRunning();
		}
	}
	
	private void DsaWork(){
		if(Math.random()<p){
			int[][] selectMinCost=new int[population][domain.length];
			for(int ip = 0; ip < population; ip++){
				for(int i=0; i<domain.length; i++){
					for(int j=0; j<neighbours.length; j++){
						selectMinCost[ip][i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndexGA[ip][j]];	
					}
				}
				int selectValueIndex=0;
				for(int i = 1; i < domain.length; i++){
					if(selectMinCost[ip][selectValueIndex] > selectMinCost[ip][i]){
						selectValueIndex = i;
					}
				}
				
				if(selectMinCost[ip][selectValueIndex] < localCostGA[ip]){
					valueIndexGA[ip] = selectValueIndex;
				}
			}
		}
	}
	
	private void mutate(){
		for(int ip = 0; ip < population; ip++){
			if(Math.random()<pMutate){
				int randomValueIndex = (int)(Math.random()*(domain.length));
				int newLocalCost = 0;
				
				for (int neighbourindex=1; neighbourindex < neighboursQuantity; neighbourindex++){
					newLocalCost+=constraintCosts.get(neighbours[neighbourindex])[randomValueIndex][neighboursValueIndexGA[ip][neighbourindex]];		
				}
				
				if (newLocalCost <= localCostGA[ip]){
					valueIndexGA[ip] = randomValueIndex;
				}
				else{
					if(Math.random() < Math.exp(((localCost - newLocalCost)*(cycleCount^2))/1000)){
						valueIndexGA[ip] = randomValueIndex;
					}
				}
			}
		}
	}
	
	protected void AlsWork(){
		warning = 0;
		
		valueIndexList.add(valueIndexGA);
		if(this.isLeafAgent() == false)
			localCostList.add(localCostGA.clone());
		else{
			accumulativeCost = localCostGA.clone();
			sendAlsCostMessage();
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
			LinkedList<int[]> temp = childrenMessageList.remove(senderIndex);
			temp.add((int[])msg.getValue());
			childrenMessageList.put(senderIndex, temp);
		}
		else{
			LinkedList<int[]> temp = new LinkedList<int[]>();
			temp.add((int[])msg.getValue());
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
			warning++;
			
			accumulativeCost = localCostList.removeFirst();
			
			for(int i = 0; i < children.length; i++){
				LinkedList<int[]> temp = childrenMessageList.remove(i);
				
				int[] oneChildMessage = temp.remove();
				for(int j = 0; j < population; j++)
					accumulativeCost[j] = accumulativeCost[j] + oneChildMessage[j];
				
				if(temp.isEmpty() == false)
					childrenMessageList.put(i, temp);
			}
			
			if(this.isRootAgent() == false){
				sendAlsCostMessage();
			}
			else{
				int min = 0;
				for(int i = 0; i < population; i++){
					accumulativeCost[i] = accumulativeCost[i]/2;
					if(accumulativeCost[min] > accumulativeCost[i])
						min = i;
				}
				
				if(resetLock == false){
					if(accumulativeCost[min] < bestCostTemp){
						bestCostTemp = accumulativeCost[min];
						stayUnchanged = 0;
						//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
					}
					else{
						stayUnchanged++;
						//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
						if(stayUnchanged >= stayCountInterval){
							bestCostTemp = 2147483647;
							stayUnchanged = 0;
							prepareToReset = totalHeight + 1;
							resetLock = true;
							sendResetMessages();
						}
					}
				}
				
				if(accumulativeCost[min] < bestCost){
					bestValue = valueIndexList.removeFirst()[min];
					bestCost = accumulativeCost[min];
					bestIndividual = min;
				}
				else{
					valueIndexList.removeFirst();
					bestIndividual = NONE;
				}
				
				if(AlsCycleCount == 0){
					prepareToStart = totalHeight + 1;
					sendStartMessages();
				}
				
				if(bestCostInCycle.length > AlsCycleCount){
					bestCostInCycle[AlsCycleCount] = bestCost;
				}
				else{
					double temp[] = new double[2*bestCostInCycle.length];
					for(int i = 0; i < bestCostInCycle.length; i++){
						temp[i] = bestCostInCycle[i];
					}
					bestCostInCycle = temp;
					bestCostInCycle[AlsCycleCount] = bestCost;
				}
				AlsCycleCount++;
				sendAlsBestMessage();
				
			}
		}
	}
	
	
	protected void disposeAlsBestMessage(Message msg){
		int tempBestIndividual = (Integer)msg.getValue();
		if(tempBestIndividual != NONE){
			bestValue = valueIndexList.remove()[tempBestIndividual];
			bestIndividual = tempBestIndividual;
		}
		else{
			valueIndexList.remove();
			bestIndividual = NONE;
		}
		if(this.isLeafAgent() == false)
			sendAlsBestMessage();
		
		//System.out.println("Agent "+this.name+"~~~best~~~"+AlsCycleCount);
		
		//if(id == 40){
		//	System.out.println("Agent "+this.id+"~~~~bestMessage~~~"+cycleCount);
		//	if(cycleCount > 10){
		//		cycleCount++;
		//	}
		//}
	}
	
	protected void AlsStopRunning(){
		if(valueIndexList.isEmpty() == true){
			if(level == 0){
				double temp[] = new double[AlsCycleCount];
				for(int i = 0; i < AlsCycleCount; i++){
					temp[i] = bestCostInCycle[i];
				}
				bestCostInCycle = temp;						//更正数组长度
			}
			valueIndex = bestValue;
			stopRunning();
		}
		//System.out.println("Agent "+this.name+"~~~~~~"+AlsCycleCount);
	}
	
	private void disposeStartMessage(Message msg){
		prepareToStart = (Integer)msg.getValue();
		sendStartMessages();
	}
	
	private void disposeResetMessage(Message msg){
		prepareToReset = (Integer)msg.getValue();
		sendResetMessages();
	}
	
	private void localCost(){
		for(int ip = 0; ip < population; ip++){
			int localCostTemp = 0;
			for(int i=0; i<neighbours.length; i++){
				localCostTemp+=constraintCosts.get(neighbours[i])[valueIndexGA[ip]][neighboursValueIndexGA[ip][i]];
			}
			localCostGA[ip] = localCostTemp;
		}
	}
	
	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[bestValue]);
		result.put(KEY_BESTCOST, this.bestCost);
		result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);

		this.msgMailer.setResult(result);
		//System.out.println("Agent "+this.name+" stopped!");
	}
	
	public Object printResults(List<Map<String, Object>> results) {
		ResultCycleAls ret=new ResultCycleAls();
		int tag = 0;
		int totalCost = 0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
			//int id_=(Integer)result.get(KEY_ID);
			//String name_=(String)result.get(KEY_NAME);
			//int value_=(Integer)result.get(KEY_VALUE);
			
			if(tag == 0){
				totalCost = ((Integer)result.get(KEY_BESTCOST));
				ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
				tag = 1;
			}
			
			//String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			//System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost)+
				" nccc: "+Infinity.infinityEasy((int)ncccTemp));
		
		ret.nccc=(int)ncccTemp;
		ret.totalCost=(int)totalCost;
		return ret;
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
	
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		return null;
	}

	protected void messageLost(Message msg) {
		
	}

}
