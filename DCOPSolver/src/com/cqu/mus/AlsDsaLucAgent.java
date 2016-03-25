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
import com.cqu.dsa.AlsDsa_Agent;
import com.cqu.settings.Settings;

public class AlsDsaLucAgent extends AgentCycleAls{
	
	public final static int TYPE_VALUE_MESSAGE = 1;
	public final static int TYPE_SUGGEST_MESSAGE = 2;
	public final static int TYPE_RESET_MESSAGE = 5;
	
	private static int cycleCountEnd;
	private static int stayDsaCountInterval;						//设置DSA操作若干轮无优化效果及重启
	private static double p;
	private static double utilityPercentage;
	private static double abandonProbability;
	
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int[] neighboursValueIndex;	
	
	private int bestCostTemp = 2147483647;
	private int mySuggestValueTag;									//给自己的建议值标记
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	private int mySuggestersNumber;									//周围建议者的数量
	private int mySuggesters[];										//各建议者的标号
	private int mySuggestedValue[];									//建议者发来的建议值
	private int myMaxCost = 0;
	private int myMinCost = 2147483647;
	private boolean resetLock = false;
	private int stayUnchanged = 0;
	private int prepareToReset = 2147483647;
	private boolean newCycle = true;
	private boolean localAct = false;
	private boolean localActCoordinate = false;
	private int[] step;
	
	private int[] abandonGuggestGain;
	private int suggestValue;
	private int suggestGain;
	private int wait = 0;
	private int suggestTag = 0;
	private int suggester;
	
	public AlsDsaLucAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		stayDsaCountInterval = Settings.settings.getSelectInterval();
		p = Settings.settings.getSelectProbability();
		utilityPercentage = Settings.settings.getSelectNewProbability();
		
		localCost = 2147483647;
		valueIndex = (int)(Math.random()*(domain.length));
		neighboursQuantity = neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		for(int i = 0; i<neighbours.length; i++)
			neighboursValueIndex[i] = 0;
		step = new int[neighboursQuantity];
		
		mySuggestersNumber = 0;
		mySuggesters = new int[neighboursQuantity];
		mySuggestValueTag = 0;
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		mySuggestedValue = new int[neighboursQuantity];
		abandonGuggestGain = new int[2];
		buildMyTable();
		//valueIndex = mySuggestValue[0];
		sendValueMessages();
	}
	
	private void buildMyTable(){
		int localMaxCost = 0;
		int[] localMinCost = new int[domain.length];
		int[][] localMinTable = new int[domain.length][neighboursQuantity];
		for(int i = 0; i < domain.length; i++)
			localMinCost[i] = 2147483647;
		
		for(int i = 0; i < domain.length; i++){
			int tempLocalMaxCost = 0;
			int tempLocalCost = 0;
			int[] tempLocalMinTable = new int[neighboursQuantity];
			for(int j = 0; j < neighboursQuantity; j++){
				
				int oneMinCost,oneMaxCost;
				oneMinCost = constraintCosts.get(neighbours[j])[i][0];
				oneMaxCost = constraintCosts.get(neighbours[j])[i][0];
				tempLocalMinTable[j] = 0;
				
				for(int k = 1; k < neighbourDomains.get(neighbours[j]).length; k++){
					if(oneMinCost > constraintCosts.get(neighbours[j])[i][k]){
						oneMinCost = constraintCosts.get(neighbours[j])[i][k];
						tempLocalMinTable[j] = k;
					}
					
					if(oneMaxCost < constraintCosts.get(neighbours[j])[i][k]){
						oneMaxCost = constraintCosts.get(neighbours[j])[i][k];
					}
				}
				tempLocalCost += oneMinCost;
				tempLocalMaxCost += oneMaxCost;
			}
			
			if(localMaxCost < tempLocalMaxCost)
				localMaxCost = tempLocalMaxCost;
			
			localMinCost[i] = tempLocalCost;
			for(int j = 0; j < neighboursQuantity; j++){
				localMinTable[i][j] = tempLocalMinTable[j];
			}
			
		}
		myMaxCost = localMaxCost;
		
		for(int i = 0; i < domain.length; i++){
			int minId = 0, minCost = 2147483647;
			for(int j = 0; j < domain.length; j++){
				if(localMinCost[j] <= minCost && localMinCost[j] != 2147483647){
					minId = j;
					minCost = localMinCost[j];
				}
			}
			mySuggestValue[i] = minId;
			if(i == 0)
				myMinCost = localMinCost[minId];
			for(int j = 0; j < neighboursQuantity; j++)
				myNeighboursSuggestTable[i][j] = localMinTable[minId][j];
			localMinCost[minId] = 2147483647;
		}
	}
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendSuggestMessages(int neighbourIndex) {
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_SUGGEST_MESSAGE, 
				abandonGuggestGain);;
		this.sendMessage(msg);
	}
	
	private void sendResetMessages(){
		for(int i = 0; i < children.length; i++){
			Message msg = new Message(this.id, children[i], TYPE_RESET_MESSAGE, prepareToReset - 1);
			this.sendMessage(msg);
		}
	}
	
	protected void disposeMessage(Message msg) {
		if(msg.getType() == TYPE_VALUE_MESSAGE){
			disposeValueMessage(msg);
		}
		else if(msg.getType() == TYPE_SUGGEST_MESSAGE){
			disposeSuggestMessage(msg);
		}
		else if(msg.getType() == TYPE_RESET_MESSAGE){
			disposeAlsResetMessage(msg);
		}
		else if(msg.getType() == AlsDsa_Agent.TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == AlsDsa_Agent.TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}
	
	private void disposeValueMessage(Message msg){
		
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
			localCost=localCost();
			if(newCycle == true){
				cycleCount++;
				newCycle = false;
			}
			if(cycleCount <= cycleCountEnd){
				AlsWork();
				if(prepareToReset > 0){
					
					//localUtilityAct();
					if(wait == 0 && suggestTag == 0)
						DsaWork();
					else if(suggestTag == 1){
						suggestTag = 0;
						
						int localCostTemp=0;
						for(int i=0; i<neighbours.length; i++){
							localCostTemp+=constraintCosts.get(neighbours[i])[suggestValue][neighboursValueIndex[i]];
						}
						if(localCostTemp < localCost){
							valueIndex = suggestValue;
							System.out.println("accept!!!!!!!!");
						}
						else{
							System.out.println("reject!!!!!!!!");
							if(prepareToReset > 1){
								abandonChain();
							}
						}
					}
					else
						wait = 0;
					sendValueMessages();
				}
				else{
					prepareToReset = 2147483647;
					resetLock = false;
					newCycle = true;
					valueIndex = (int)(Math.random()*(domain.length));
					sendValueMessages();
				}
			}
		}
	}
	
	private void DsaWork(){
		if(Math.random()<p){
			int[] selectMinCost=new int[domain.length];
			for(int i=0; i<domain.length; i++){
				selectMinCost[i]=0;
			}
			for(int i=0; i<domain.length; i++){
				for(int j=0; j<neighbours.length; j++){
					selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];	
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
			
//			if(selectOneMinCost < localCost)
//				valueIndex = selectValueIndex;
//			else{
//				double myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
//				if(myPercentage > 0.2 && prepareToReset > 2 && suggestTag == 0){
//					abandon(selectOneMinCost);
//				}
//			}
			
			if(selectOneMinCost < localCost)
				valueIndex = selectValueIndex;
			double myPercentage = ((double)(selectOneMinCost-myMinCost))/((double)(myMaxCost-myMinCost));
			if(myPercentage > 0.3 && prepareToReset > 2 && suggestTag == 0){
				abandon(selectOneMinCost);
			}
		}
	}
	
	private void abandon(int nature) {
		if(Math.random() > utilityPercentage)
			return;
		
		int[] selectMinCost=new int[domain.length];
		int[] abandonValueIndex = new int[neighboursQuantity];
		int[][] abandonNeighbourIndex = new int[neighboursQuantity][domain.length];
		int[] abandonCost = new int[neighboursQuantity];
		for(int h = 0; h < neighbours.length; h++){						//遍历邻居
			for(int i=0; i<domain.length; i++){							//遍历值域
				for(int j=0; j<neighbours.length && j != h; j++)
						selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];
																		//累计邻居以外的cost值
				int tempIndex = 0;
				int tempCost = constraintCosts.get(neighbours[h])[i][tempIndex];
				for(int hValue = 0; hValue < constraintCosts.get(neighbours[h])[i].length; hValue++){
					if(constraintCosts.get(neighbours[h])[i][hValue] < tempCost){
						tempCost = constraintCosts.get(neighbours[h])[i][hValue];
						tempIndex = hValue;								//找到选定的最小值，记录value和cost
					}
				}
				selectMinCost[i] += tempCost;							//做累计
				abandonNeighbourIndex[h][i] = tempIndex;				//记录对应的邻居号和自己value对应的邻居的value建议
			}
			int selectOneMinCost=selectMinCost[0];
			for(int i = 1; i < domain.length; i++){
				if(selectOneMinCost > selectMinCost[i]){
					selectOneMinCost = selectMinCost[i];
					abandonValueIndex[h] = i;
				}
			}
			abandonCost[h] = selectOneMinCost;							//找到每一个邻居对应的自己最小的value
		}
		int abandonIndex = 0;
		for(int i = 1; i < neighboursQuantity; i++){
			if(abandonCost[i] < abandonCost[abandonIndex])
				abandonIndex = i;										//找到差值最小的邻居，选作舍弃
		}
		if(abandonCost[abandonIndex] < nature){
			valueIndex = abandonValueIndex[abandonIndex];
//			abandonGuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
//			abandonGuggestGain[1] = nature - abandonCost[abandonIndex];
//			sendSuggestMessages(abandonIndex);
//			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
	}
	
	private void disposeSuggestMessage(Message msg) {
		if(wait != 1 && suggestTag !=1 && Math.random() < 0.1){
			int senderIndex=0;
			int senderId=msg.getIdSender();
			for(int i=0; i<neighbours.length; i++){
				if(neighbours[i]==senderId){
					senderIndex=i;
					break;
				}
			}
			suggester = senderIndex;
			suggestValue = ((int[])msg.getValue())[0];
			suggestGain = ((int[])msg.getValue())[1];
			suggestTag = 1;
		}
	}
	
	private void abandonChain(){
		int selectMinCost = 0;
		int[] abandonValueIndex = new int[neighboursQuantity];
		int[] abandonNeighbourIndex = new int[neighboursQuantity];
		int[] abandonCost = new int[neighboursQuantity];
		for(int h = 0; h < neighbours.length && h != suggester; h++){		//遍历邻居
			
			for(int j=0; j<neighbours.length && j != h ; j++)
					selectMinCost+=constraintCosts.get(neighbours[j])[suggestValue][neighboursValueIndex[j]];
																			//累计邻居以外的cost值
			int tempIndex = 0;
			int tempCost = constraintCosts.get(neighbours[h])[suggestValue][tempIndex];
			for(int hValue = 0; hValue < constraintCosts.get(neighbours[h])[0].length; hValue++){
				if(constraintCosts.get(neighbours[h])[suggestValue][hValue] < tempCost){
					tempCost = constraintCosts.get(neighbours[h])[suggestValue][hValue];
					tempIndex = hValue;										//找到选定的最小值，记录value和cost
				}
			}
			selectMinCost += tempCost;										//做累计
			abandonNeighbourIndex[h] = tempIndex;							//记录对应的邻居号对应的邻居的value建议
			
			abandonCost[h] = selectMinCost;									//找到每一个邻居对应的自己最小的value
		}
		int abandonIndex = 0;
		for(int i = 1; i < neighboursQuantity; i++){
			if(abandonCost[i] < abandonCost[abandonIndex])
				abandonIndex = i;											//找到差值最小的邻居，选作舍弃
		}
		if(abandonCost[abandonIndex] < localCost){
			System.out.println("yes!!!!!!!!");
			valueIndex = suggestValue;
			abandonGuggestGain[0] = abandonNeighbourIndex[abandonIndex];
			abandonGuggestGain[1] = localCost - abandonCost[abandonIndex];
			sendSuggestMessages(abandonIndex);
			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
		else{
			System.out.println("no!!!!!!!!!!!!!!!!!!!!!");
			DsaWork();
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
			
			accumulativeCost = localCostList.removeFirst();
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

				if(resetLock == false){
					if(accumulativeCost < bestCostTemp){
						bestCostTemp = accumulativeCost;
						stayUnchanged = 0;
						//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
					}
					else{
						stayUnchanged++;
						//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
						if(stayUnchanged >= stayDsaCountInterval){
							bestCostTemp = 2147483647;
							stayUnchanged = 0;
							prepareToReset = totalHeight + 1;
							resetLock = true;
							sendResetMessages();
						}
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
				localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];
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
	
	public Object printResults(List<Map<String, Object>> results) {

		ResultCycleAls ret=new ResultCycleAls();
		int tag = 0;
		int totalCost = 0;
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
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost));

		ret.totalCost=(int)totalCost;
		return ret;
	}

	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		
		return null;
	}

	protected void messageLost(Message msg) {
		
	}
}
