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
import com.cqu.main.Debugger;
import com.cqu.mgm.MgmAgent;
import com.cqu.settings.Settings;

public class AlsDsaMgmAgent extends AgentCycleAls{
	
	public final static int TYPE_DSA_VALUE_MESSAGE = 0;
	public final static int TYPE_MGM_VALUE_MESSAGE = 1;
	public final static int TYPE_GAIN_MESSAGE = 2;
	public final static int TYPE_RESET_MESSAGE = 5;
	
	public final static int DSA = 0;
	public final static int MGM = 1;
	
	private static int cycleCountEnd;
	private static double p;
	private static int stayDsaCountInterval;
	
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int gainValue;
	private int selectValueIndex;
	private int neighboursGain[];
	private int[] neighboursValueIndex;								//[neighbour 的 Index, neighbourValue 的  Index]
	//private HashMap<Integer, Integer> neighboursValueIndex;		//<neighbour 的 Index, neighbourValue 的  Index>

	private int bestCostTemp1 = 2147483645;
	private int bestCostTemp2 = 2147483646;
	private int bestCostTemp3 = 2147483647;
	private int STEP = MGM;
	private boolean resetLock = false;
	private int stayUnchanged = 0;
	private int prepareToReset = 2147483647;
	protected LinkedList<int[]> localCostList = new LinkedList<int[]>();
	
	public AlsDsaMgmAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}

	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		p = Settings.settings.getSelectProbability();
		stayDsaCountInterval = Settings.settings.getSelectInterval();
			
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		neighboursQuantity=neighbours.length;
		neighboursGain=new int[neighboursQuantity];
		neighboursValueIndex = new int[neighboursQuantity];
		for(int i = 0; i<neighbours.length; i++)
			neighboursValueIndex[i] = 0;
		sendDsaValueMessages();
	}
	
	private void sendDsaValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_DSA_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendMgmValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_MGM_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_GAIN_MESSAGE, gainValue);
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
		
		if(msg.getType() == TYPE_DSA_VALUE_MESSAGE){
			disposeDsaValueMessage(msg);
		}
		else if(msg.getType() == TYPE_MGM_VALUE_MESSAGE){
			disposeMgmValueMessage(msg);
		}
		else if(msg.getType() == TYPE_GAIN_MESSAGE){
			disposeGainMessage(msg);
		}
		else if(msg.getType() == TYPE_RESET_MESSAGE){
			disposeAlsResetMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsCostMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsBestMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}

	
	private void disposeDsaValueMessage(Message msg){
			
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndex[senderIndex] = (int)((Integer)msg.getValue());
		
		if(receivedQuantity==0){
			
			prepareToReset--;
			if(STEP == MGM){
				cycleCount++;
				STEP = DSA;
				bestCostTemp1 = 2147483645;
				bestCostTemp2 = 2147483646;
				bestCostTemp3 = 2147483647;
				if(cycleCount > cycleCountEnd){
					return;
				}
			}
			
			if(prepareToReset > 0){

				localCost=localCost();
				//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!!
				//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!!
				AlsWork();
				
				if(Math.random()<p){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
							if(this.id < neighbours[j])
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];		
							else
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[neighboursValueIndex[j]][i];	
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
				}
				sendDsaValueMessages();
			}
			else{
				//valueIndex = (int)(Math.random()*(domain.length));
				prepareToReset = 2147483647;
				sendMgmValueMessages();
				//valueIndex = (int)(Math.random()*(domain.length));				//不要重置，效果不好
			}
		}
	}
	
	
	private void disposeMgmValueMessage(Message msg) {
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		neighboursValueIndex[senderIndex] = (int)((Integer)msg.getValue());
		
		if(receivedQuantity==0){

			STEP = MGM;
			prepareToReset--;
			
			if(prepareToReset > 0){
				localCost=localCost();
				AlsWork();
				
				int[] selectMinCost=new int[domain.length];
				for(int i=0; i<domain.length; i++){
					selectMinCost[i]=0;
				}
				for(int i=0; i<domain.length; i++){
					for(int j=0; j<neighboursQuantity; j++){
						if(this.id < neighbours[j])
							selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];		
						else
							selectMinCost[i]+=constraintCosts.get(neighbours[j])[neighboursValueIndex[j]][i];		
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
				//System.out.println("agent"+this.id+"_______"+cycleCount+"_______"+gainValue+"________");
				sendGainMessages();
			}
			else{
				//valueIndex = (int)(Math.random()*(domain.length));
				prepareToReset = 2147483647;
				sendDsaValueMessages();
				//valueIndex = (int)(Math.random()*(domain.length));				//不要重置，效果不好
			}
		}
	}
	
	private void disposeGainMessage(Message msg) {
		
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
			prepareToReset--;
			if(prepareToReset > 0){
				AlsWork();
				for(int i=0; i<neighboursQuantity; i++){
					if(neighboursGain[i]>=gainValue){
						sendMgmValueMessages();
						return;
					}
				}
				valueIndex=selectValueIndex;
				//if(cycleCount == 9){
				//	System.out.println("agent"+this.id+"_______"+"Gain_ready"+"________"+gainValue);
				//}
				sendMgmValueMessages();
			}
			else{
				//valueIndex = (int)(Math.random()*(domain.length));
				prepareToReset = 2147483647;
				sendDsaValueMessages();
				resetLock = false;
				//valueIndex = (int)(Math.random()*(domain.length));				//不要重置，效果不好
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
			
			warning++;
			
			accumulativeCost = localCostList.getFirst()[0];
			int theSTEP =  localCostList.removeFirst()[1];
			for(int i = 0; i < children.length; i++){
				LinkedList<Integer> temp = childrenMessageList.remove(i);
				accumulativeCost = accumulativeCost + temp.remove();
				if(temp.isEmpty() == false)
					childrenMessageList.put(i, temp);
			}
			
			if(this.isRootAgent() == false){
				sendAlsCostMessage();
			}
			else{
				accumulativeCost = accumulativeCost/2;
				
				if(theSTEP == DSA){
					if(accumulativeCost < bestCostTemp1){
						bestCostTemp1 = accumulativeCost;
						stayUnchanged = 0;
					}
					else{
						if(resetLock == false){
							stayUnchanged++;
							if(stayUnchanged >= stayDsaCountInterval){
								stayUnchanged = -2147483646;
								prepareToReset = totalHeight + 1;
								resetLock = true;
								sendResetMessages();
							}
						}
					}
				}
				else{
					bestCostTemp3 = bestCostTemp2;
					bestCostTemp2 = bestCostTemp1;
					bestCostTemp1 = accumulativeCost;
					if(bestCostTemp3 == bestCostTemp2 && bestCostTemp2 == bestCostTemp1){
						stayUnchanged = -2147483646;
						prepareToReset = totalHeight + 1;
						resetLock = true;
						sendResetMessages();
					}
				}
				
				if(accumulativeCost < bestCost){
					bestValue = valueIndexList.removeFirst();
					bestCost = accumulativeCost;
					isChanged = YES;
				}
				else{
					valueIndexList.removeFirst();
					isChanged = NO;
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
				//System.out.println("cycleCount~~~"+AlsCycleCount+"~~~bestCost~~~"+bestCost);
				
				//if(valueIndexList.size() == 0){
				//	System.out.println("cycleCount~~~"+cycleCount);
				//}
			}
		}
		if(valueIndexList.isEmpty() == true){
			if(level == 0){
				double temp[] = new double[AlsCycleCount];
				for(int i = 0; i < AlsCycleCount; i++){
					temp[i] = bestCostInCycle[i];
				}
				bestCostInCycle = temp;
			}
			valueIndex = bestValue;
			stopRunning();
		}
		//System.out.println("Agent "+this.name+"~~~~~~"+AlsCycleCount);
	}
	
	protected void AlsWork(){
		
		warning = 0;
		
		valueIndexList.add(valueIndex);
		if(this.isLeafAgent() == false){
			int[] a = new int[2];
			a[0] = localCost;
			a[1] = STEP;
			localCostList.add(a);
		}
		else{
			accumulativeCost  = localCost;
			sendAlsCostMessage();
		}
		//System.out.println("Agent "+this.name+"~~~~~"+cycleCount);
		
		//if(id == 40){
		//	System.out.println("Agent "+this.id+"~~~costMessage~~~"+cycleCount);
		//}
	};
	
	private void disposeAlsResetMessage(Message msg){
		
		prepareToReset = (Integer)msg.getValue();
		sendResetMessages();
		
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
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighbours.length; i++){
			if(this.id < neighbours[i])
				localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];
			else
				localCostTemp+=constraintCosts.get(neighbours[i])[neighboursValueIndex[i]][valueIndex];
		}
		return localCostTemp;
	}
	
	
	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		result.put(KEY_BESTCOST, this.bestCost);
		result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);
		
		this.msgMailer.setResult(result);
		//System.out.println("Agent "+this.name+" stopped!");
	}
	
	
	@Override
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

	
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {

		return "from "+sender.getName()+" to "+receiver.getName()+" type "+messageContent(msg);
	}
	
	public static String messageContent(Message msg){
		return "unknown";
	}
	
	protected void messageLost(Message msg) {
		
	}
}
