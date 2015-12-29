package com.cqu.mus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

//程序中stopRunning()设置在了ALS框架中，当运行cycle被设置成0的时候，会陷入死循环

public class AlsMusDsa2Agent extends AgentCycleAls{
	
	public final static int TYPE_STEP1_MESSAGE = 1;
	public final static int TYPE_STEP2_MESSAGE = 2;
	public final static int TYPE_STEP3_MESSAGE = 3;
	public final static int TYPE_STEPDSA_MESSAGE = 4;
	
	public final static String NONE = "NONE";
	public final static String SUGGESTER = "SUGGESTER";
	public final static String ACCEPTER1 = "ACCEPTER1";
	public final static String ACCEPTER2 = "ACCEPTER2";
	
	private static int cycleCountEnd;
	private static int stayDsaCountInterval;
	private static double p;
	private static double selectSuggesterP;
	private static double selectAccepterP;
	
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int[] neighboursValueIndex;								//[neighbour 的 Index, neighbourValue 的  Index]
	//private HashMap<Integer, Integer> neighboursValueIndex;		//<neighbour 的 Index, neighbourValue 的  Index>
	
	private int[][] neighboursDegreeAndTag;							//[neighbour 的度数, neighbour的tag]
	private int myTag;												//Suggester竞争标记
	private String myIdentity;										//身份标记
	private double mySuggestValueTag;								//给自己的建议值标记
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	private int[] myNeighboursSuggestAgainTable;					//第二次接受者发送的建议值
	private int mySuggestersNumber;									//周围建议者的数量
	private int mySuggesters[];										//各建议者的标号
	private int mySuggestedValue[];									//建议者发来的建议值
	
	private int stayDsaCount = 0;
	
	//Suggester遍历选择value~~~~~~~~~~~~
	
	public AlsMusDsa2Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO 自动生成的构造函数存根
	}
	
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		stayDsaCountInterval = Settings.settings.getSelectInterval();
		p = Settings.settings.getSelectProbability();
		selectSuggesterP = Settings.settings.getSelectProbabilityA();
		selectAccepterP = Settings.settings.getSelectProbabilityB();
		
		localCost = 2147483647;
		valueIndex = (int)(Math.random()*(domain.length));
		neighboursQuantity = neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		for(int i = 0; i<neighbours.length; i++)
			neighboursValueIndex[i] = 0;
		
		neighboursDegreeAndTag = new int[neighboursQuantity][2];
		for(int i = 0; i < neighbours.length; i++){
			neighboursDegreeAndTag[i][0] = 0;
			neighboursDegreeAndTag[i][1] = 0;
		}
		if(Math.random() < selectSuggesterP){
			myIdentity = SUGGESTER;
			myTag = (int)(2147483647*Math.random());
		}
		else{
			myIdentity = NONE;
		}
		mySuggestersNumber = 0;
		mySuggesters = new int[neighboursQuantity];
		mySuggestValueTag = 0;
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		mySuggestedValue = new int[neighboursQuantity];
		myNeighboursSuggestAgainTable = new int[neighboursQuantity];
		buildMyTable();
		sendStep1Messages();
	}
	
	
	private void buildMyTable(){
		int[] localMinCost = new int[domain.length];
		//int[] localMinValueIndex = new int[domain.length];
		int[][] localMinTable = new int[domain.length][neighboursQuantity];
		for(int i = 0; i < domain.length; i++)
			localMinCost[i] = 2147483647;
		
		for(int i = 0; i < domain.length; i++){
			int tempLocalCost = 0;
			int[] tempLocalMinTable = new int[neighboursQuantity];
			for(int j = 0; j < neighboursQuantity; j++){
				
				int oneMinCost;
				if(this.id < neighbours[j])
					oneMinCost = constraintCosts.get(neighbours[j])[i][0];
				else
					oneMinCost = constraintCosts.get(neighbours[j])[0][i];
				tempLocalMinTable[j] = 0;
				
				for(int k = 1; k < neighbourDomains.get(neighbours[j]).length; k++){	
					if(this.id < neighbours[j]){
						if(oneMinCost > constraintCosts.get(neighbours[j])[i][k]){
							oneMinCost = constraintCosts.get(neighbours[j])[i][k];
							tempLocalMinTable[j] = k;
						}
					}
					else{
						if(oneMinCost > constraintCosts.get(neighbours[j])[k][i]){
							oneMinCost = constraintCosts.get(neighbours[j])[k][i];
							tempLocalMinTable[j] = k;
						}
					}
				}
				tempLocalCost += oneMinCost;
			}
			
			localMinCost[i] = tempLocalCost;
			for(int j = 0; j < neighboursQuantity; j++){
				localMinTable[i][j] = tempLocalMinTable[j];
			}
			
		}
		
		for(int i = 0; i < domain.length; i++){
			int minId = 0, minCost = 2147483647;
			for(int j = 0; j < domain.length; j++){
				if(localMinCost[j] <= minCost && localMinCost[j] != 2147483647){
					minId = j;
					minCost = localMinCost[j];
				}
			}
			mySuggestValue[i] = minId;
			for(int j = 0; j < neighboursQuantity; j++)
				myNeighboursSuggestTable[i][j] = localMinTable[minId][j];
			localMinCost[minId] = 2147483647;
		}
	}
	
	
	private void sendStep1Messages(){
		if(myIdentity == NONE){
			int[] box = new int[1];
			box[0] = valueIndex;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				Message msg = new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP1_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else if(myIdentity == SUGGESTER){
			int[] box = new int[3];
			box[0] = valueIndex;
			box[1] = neighboursQuantity;
			box[2] = myTag;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				Message msg = new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP1_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else{
			System.out.println("wrong!!!!!!!!");
			int a = 1;
			a = a/0;
		}
	}
	
	
	private void sendStep2Messages(){
		if(myIdentity == NONE){
			int[] box = new int[1];
			box[0] = valueIndex;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				Message msg = new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP2_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else if(myIdentity == SUGGESTER){
			int[] box = new int[2];
			box[0] = valueIndex;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				box[1] = myNeighboursSuggestTable[((int)mySuggestValueTag)%domain.length][neighbourIndex];
				Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP2_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else{
			System.out.println("wrong!!!!!!!!");
			int a = 1;
			a = a/0;
		}
	}
	
	
	private void sendStep3Messages(){
		if(myIdentity == NONE || myIdentity == SUGGESTER || myIdentity == ACCEPTER2){
			int[] box = new int[1];
			box[0] = valueIndex;
			for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
				Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP3_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else if(myIdentity == ACCEPTER1){
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				if(neighbourIndex != mySuggesters[0]){
					int[] box = new int[2];
					box[0] = valueIndex;
					box[1] = myNeighboursSuggestAgainTable[neighbourIndex];
					Message msg = new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP3_MESSAGE, box);
					this.sendMessage(msg);
				}
				else{
					int[] box = new int[1];
					box[0] = valueIndex;
					Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEP3_MESSAGE, box);
					this.sendMessage(msg);
				}
			}
		}
		else{
			System.out.println("wrong!!!!!!!!");
			int a = 1;
			a = a/0;
		}
	}
	
	
	private void sendStepDsaMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], AlsMusDsa2Agent.TYPE_STEPDSA_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO 自动生成的方法存根
		//System.out.println("!!!!!!!!"+cycleCount+"~~~~~"+stayDsaCount);
		if(msg.getType() == AlsMusDsa2Agent.TYPE_STEP1_MESSAGE){
			disposeStep1Message(msg);
		}
		else if(msg.getType() == AlsMusDsa2Agent.TYPE_STEP2_MESSAGE){
			disposeStep2Message(msg);
		}
		else if(msg.getType() == AlsMusDsa2Agent.TYPE_STEP3_MESSAGE){
			disposeStep3Message(msg);
		}
		else if(msg.getType() == AlsMusDsa2Agent.TYPE_STEPDSA_MESSAGE){
			disposeStepDsaMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsCostMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == AlsMusDsa2Agent.TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsBestMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == AlsMusDsa2Agent.TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}
		else{
			System.out.println("wrong!!!!!!!!");
			int a = 1;
			a=a/0;
		}
	}
	
	
	private void disposeStep1Message(Message msg) {
		//System.out.println("~~~"+id+"~~~"+cycleCount+"~~~");
		
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
		
		int[] tempBox = (int[])msg.getValue();
		if(myIdentity == NONE){
			neighboursValueIndex[senderIndex] = tempBox[0];
		}
		else if(myIdentity == SUGGESTER){
			if(tempBox.length == 3){
				neighboursValueIndex[senderIndex] = tempBox[0];
				neighboursDegreeAndTag[senderIndex][0] = tempBox[1];
				neighboursDegreeAndTag[senderIndex][1] = tempBox[2];
			}
		}
		else{
			System.out.println("wrong!!!!!!!!");
			int a = 1;
			a=a/0;
		}
		
		if(receivedQuantity==0){
			localCost=localCost();
			
			if(cycleCount <= cycleCountEnd){
				//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!!
				//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!!
				AlsWork();
				
				if(myIdentity == NONE){
					DsaWork();
					sendStep2Messages();
				}
				else if(myIdentity == SUGGESTER){
					for(int i = 0; i < neighboursQuantity; i++){
						if(neighboursQuantity < neighboursDegreeAndTag[i][0] || 
								neighboursQuantity == neighboursDegreeAndTag[i][0] && myTag <= neighboursDegreeAndTag[i][1])
						{
							myIdentity = NONE;
							DsaWork();
							sendStep2Messages();
							return;
						}
					}
					valueIndex = mySuggestValue[((int)mySuggestValueTag)%domain.length];
					mySuggestValueTag = mySuggestValueTag+0.1;
					sendStep2Messages();
				}
				else{
					System.out.println("wrong!!!!!!!!");
					int a = 1;
					a=a/0;
				}
			}
		}
	}
	
	
	private void disposeStep2Message(Message msg) {
		
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		int[] tempBox = (int[])msg.getValue();
		neighboursValueIndex[senderIndex] = tempBox[0];
		if(tempBox.length == 2){
			if(myIdentity == NONE){
				mySuggesters[mySuggestersNumber] = senderIndex;
				mySuggestedValue[mySuggestersNumber] = tempBox[1];
				mySuggestersNumber++;
			}
			else{
				System.out.println("wrong!!!!!!!!");
				int a = 1;
				a=a/0;
			}
		}
		
		if(receivedQuantity==0){
			localCost=localCost();
			
			//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!!
			//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!!
			AlsWork();
			
			if(myIdentity == NONE){
				if(mySuggestersNumber == 0){
					DsaWork();
				}
				else if(mySuggestersNumber == 1){
					if(Math.random() < selectAccepterP){
						myIdentity = ACCEPTER1;
						valueIndex = mySuggestedValue[0];
						
						for(int i = 0; i < neighbours.length; i++){
							if(i != mySuggesters[0]){
								
								myNeighboursSuggestAgainTable[i] = 0;
								int temp;
								if(this.id < neighbours[i])
									temp = constraintCosts.get(neighbours[i])[valueIndex][0];
								else
									temp = constraintCosts.get(neighbours[i])[0][valueIndex];
								
								int tempCost;
								for(int j = 1; j < neighbourDomains.size(); j++){
									if(this.id < neighbours[i])
										tempCost = constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[j]];
									else
										tempCost = constraintCosts.get(neighbours[i])[neighboursValueIndex[j]][valueIndex];
									
									if(tempCost < temp){
										myNeighboursSuggestAgainTable[i] = j;
										temp = tempCost;
									}
								}
							}
						}
					}
					else{
						myIdentity = ACCEPTER2;
						DsaWork();
					}
				}
				else if(mySuggestersNumber > 1){
					myIdentity = ACCEPTER2;
					DsaWork();
				}
			}
			sendStep3Messages();
		}
	}
	
	
	private void disposeStep3Message(Message msg) {
		
		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		int[] tempBox = (int[])msg.getValue();
		neighboursValueIndex[senderIndex] = tempBox[0];
		if(tempBox.length == 2){
			if(myIdentity == ACCEPTER2){
				mySuggesters[mySuggestersNumber] = senderIndex;
				mySuggestedValue[mySuggestersNumber] = tempBox[1];
				mySuggestersNumber++;
			}
			else if(myIdentity == SUGGESTER){
				System.out.println("wrong!!!!!!!!");
				int a = 1;
				a=a/0;
			}
		}
		
		if(receivedQuantity==0){
			localCost=localCost();
			
			//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!!
			//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!!
			AlsWork();
			
			if(myIdentity == NONE){
				DsaWork();
			}
			else if(myIdentity == ACCEPTER2){
				
				int mySuggestersList[] = new int[neighboursQuantity];
				for(int i = 0; i < mySuggestersNumber; i++){
					mySuggestersList[i] = 0;
				}
				for(int i = 0; i < mySuggestersNumber; i++){
					mySuggestersList[mySuggesters[i]] = 1;
				}
				
				int selectValueIndex = 0;
				int selectCost = 2147483647;
				for(int i = 0; i < domain.length; i++){
					
					int tempSelectCost = 0;
					for(int j = 0; j < neighbours.length; j++){
						if(mySuggestersList[j] == 1){
							if(this.id < neighbours[j])
								tempSelectCost += constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];
							else
								tempSelectCost += constraintCosts.get(neighbours[j])[neighboursValueIndex[j]][i];
						}
					}
					if(tempSelectCost < selectCost){
						selectValueIndex = i;
						selectCost = tempSelectCost;
					}
				}
				
				valueIndex = selectValueIndex;
			}
			/*
			 * 一轮结束初始化数据
			 */
			for(int i = 0; i < neighbours.length; i++){
				neighboursDegreeAndTag[i][0] = 0;
				neighboursDegreeAndTag[i][1] = 0;
			}
			myIdentity = NONE;
			mySuggestersNumber = 0;
			if(stayDsaCountInterval == 0){
				if(Math.random() < selectSuggesterP){
					myIdentity = SUGGESTER;
					myTag = (int)(2147483647*Math.random());
				}
				sendStep1Messages();
			}
			else{
				sendStepDsaMessages();
			}
		}
	}
	
	
	private void disposeStepDsaMessage(Message msg){

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
			
			stayDsaCount++;
			localCost=localCost();
			AlsWork();
			DsaWork();
			
			if(stayDsaCount < stayDsaCountInterval)
				sendStepDsaMessages();
			else{
				stayDsaCount = 0;
				if(Math.random() < selectSuggesterP){
					myIdentity = SUGGESTER;
					myTag = (int)(2147483647*Math.random());
				}
				sendStep1Messages();
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
		// TODO 自动生成的方法存根

		ResultCycleAls ret=new ResultCycleAls();
		int tag = 0;
		int totalCost = 0;
		for(Map<String, Object> result : results){
			
			int id_=(Integer)result.get(KEY_ID);
			String name_=(String)result.get(KEY_NAME);
			int value_=(Integer)result.get(KEY_VALUE);
			
			if(tag == 0){
				ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
				totalCost = ((Integer)result.get(KEY_BESTCOST));
				tag = 1;
			}
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			//System.out.println(displayStr);
		}
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost));
		ret.totalCost=(int)totalCost;
		return ret;
	}
	
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return null;
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
