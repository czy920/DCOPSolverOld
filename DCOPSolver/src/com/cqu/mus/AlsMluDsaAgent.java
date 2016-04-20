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
import com.cqu.settings.Settings;

//程序中stopRunning()设置在了ALS框架中，当运行cycle被设置成0的时候，会陷入死循环

//新增自适应控制重启机制的MUS算法
//本平台的ALS框架要注意到一个问题，Agent总是在通信消息之前发出ALS消息，所以它在每一轮都首先处理ALS消息，再处理其他消息。
//当需要两者交互时要注意先后顺序
//Suggester遍历选择value~~~~~~~~~~~~

public class AlsMluDsaAgent extends AgentCycleAls{
	
	public final static int TYPE_STEP1_MESSAGE = 1;
	public final static int TYPE_STEP2_MESSAGE = 2;
	public final static int TYPE_STEPDSA_MESSAGE = 4;
	public final static int TYPE_RESET_MESSAGE = 5;
	
	public final static String SUGGESTER = "SUGGESTER";
	public final static String ACCEPTER = "ACCEPTER";
	
	private static int cycleCountEnd;
	private static int stayDsaCountInterval;						//设置DSA操作若干轮无优化效果及重启
	private static double p;
	
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int[] neighboursValueIndex;								//[neighbour 的 Index, neighbourValue 的  Index]
	//private HashMap<Integer, Integer> neighboursValueIndex;		//<neighbour 的 Index, neighbourValue 的  Index>

	private int bestCostTemp = 2147483647;
	private int[] neighboursTag;									//neighbour的tag竞争标记
	private int myTag;												//Suggester竞争标记
	private int myTagStandard=1000;									//Suggester竞争标记标准
	private String myIdentity;										//身份标记
	private double mySuggestValueTag;								//给自己的建议值标记
	private int waitTime = 0;
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	private int mySuggestersNumber;									//周围建议者的数量
	private int mySuggesters[];										//各建议者的标号
	private int mySuggestedValue[];									//建议者发来的建议值
	
	private int STEP = 0;
	private boolean resetLock = false;
	private int stayUnchanged = 0;
	private int prepareToReset = 2147483647;
	protected LinkedList<int[]> localCostList = new LinkedList<int[]>();
	
	
	public AlsMluDsaAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}
	
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		stayDsaCountInterval = Settings.settings.getSelectInterval();
		p = Settings.settings.getSelectProbability();
		
		localCost = 2147483647;
		valueIndex = (int)(Math.random()*(domain.length));
		neighboursQuantity = neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		for(int i = 0; i<neighbours.length; i++)
			neighboursValueIndex[i] = 0;
		
		neighboursTag = new int[neighboursQuantity];
		for(int i = 0; i < neighbours.length; i++){
			neighboursTag[i] = 0;
		}
		myIdentity = SUGGESTER;
		myTag = (int)(100*Math.random()+myTagStandard)*neighboursQuantity;
		
		mySuggestersNumber = 0;
		mySuggesters = new int[neighboursQuantity];
		mySuggestValueTag = 0;
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		mySuggestedValue = new int[neighboursQuantity];
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
				oneMinCost = constraintCosts.get(neighbours[j])[i][0];
				tempLocalMinTable[j] = 0;
				
				for(int k = 1; k < neighbourDomains.get(neighbours[j]).length; k++){
					if(oneMinCost > constraintCosts.get(neighbours[j])[i][k]){
						oneMinCost = constraintCosts.get(neighbours[j])[i][k];
						tempLocalMinTable[j] = k;
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
		int[] box = new int[2];
		box[0] = valueIndex;
		box[1] = myTag;
		for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
			Message msg = new Message(this.id, neighbours[neighbourIndex], TYPE_STEP1_MESSAGE, box);
			this.sendMessage(msg);
		}
	}


	private void sendStep2Messages(){
		if(myIdentity == ACCEPTER){
			int[] box = new int[1];
			box[0] = valueIndex;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				Message msg = new Message(this.id, neighbours[neighbourIndex], TYPE_STEP2_MESSAGE, box);
				this.sendMessage(msg);
			}
		}
		else if(myIdentity == SUGGESTER){
			int[] box = new int[2];
			box[0] = valueIndex;
			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
				box[1] = myNeighboursSuggestTable[((int)mySuggestValueTag)%domain.length][neighbourIndex];
				Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_STEP2_MESSAGE, box);
				this.sendMessage(msg);
			}
		}	
		else{
			System.out.println("wrong in sendStep2Messages!!!!!!!!");
			int a = 1;
			a = a/0;
		}
	}

	
	private void sendStepDsaMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_STEPDSA_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	
	private void sendResetMessages(){
		for(int i = 0; i < children.length; i++){
			Message msg = new Message(this.id, children[i], TYPE_RESET_MESSAGE, prepareToReset - 1);
			this.sendMessage(msg);
		}
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
	
	
	@Override
	protected void disposeMessage(Message msg) {
		//System.out.println("~~~id-"+id+"~~~cycle-"+cycleCount+"~~~step-"+STEP+"~~~"+msg.getType());
		
		if(msg.getType() == TYPE_STEP1_MESSAGE){
			disposeStep1Message(msg);
		}
		else if(msg.getType() == TYPE_STEP2_MESSAGE){
			disposeStep2Message(msg);
		}
		else if(msg.getType() == TYPE_STEPDSA_MESSAGE){
			disposeStepDsaMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsCostMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		//!!!!!!!!!!!!!!!!!!!!要添加disposeAlsBestMessage(msg)的处理模块!!!!!!!!!!!!!!!!!!!
		else if(msg.getType() == TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}
		else if(msg.getType() == TYPE_RESET_MESSAGE){
			disposeAlsResetMessage(msg);
		}
		else{
			System.out.println("wrong in disposeMessage!!!!!!!!");
			int a = 1;
			a=a/0;
		}
	}
	
	
	private void disposeStep1Message(Message msg) {
		//System.out.println("~~~"+id+"~~~"+cycleCount+"~~~");
		STEP = 1;
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
		if(tempBox.length == 2){
			neighboursValueIndex[senderIndex] = tempBox[0];
			neighboursTag[senderIndex] = tempBox[1];
		}
		
		if(receivedQuantity==0){
			localCost=localCost();
			//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!
			//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!
			AlsWork();
			
			if(cycleCount <= cycleCountEnd){
				
					for(int i = 0; i < neighboursQuantity; i++){
						if(myTag <= neighboursTag[i])
						{
							waitTime++;
							myTagStandard = (int)(myTagStandard*(1+1/(waitTime*100)));
							//myTagStandard = (int)(myTagStandard*1.01);
							myIdentity = ACCEPTER;
							DsaWork();
							sendStep2Messages();
							return;
						}
					}
					//System.out.println("Id "+id+"    I am Syggester!!!!!!!!");
					waitTime = 0;
					myTagStandard = (int)(myTagStandard*0.99);
					valueIndex = mySuggestValue[((int)mySuggestValueTag)%domain.length];
					mySuggestValueTag = mySuggestValueTag+0.5;
					sendStep2Messages();
			}
			else
				STOPRUNNING = true;
			bestCostTemp = 2147483647;
		}
	}
	
	
	private void disposeStep2Message(Message msg) {
		STEP = 2;
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
			if(myIdentity == ACCEPTER){
				mySuggesters[mySuggestersNumber] = senderIndex;
				mySuggestedValue[mySuggestersNumber] = tempBox[1];
				mySuggestersNumber++;
			}
			else{
				System.out.println("wrong!!!!!!!!~~~in~~~" + STEP + " ~ Agent "+id+"~~~I am " + myIdentity);
				int a = 1;
				a=a/0;
			}
		}
		
		if(receivedQuantity==0){
			localCost=localCost();
			//!!!!!!!!!!!!!!!!!!!!进行ALS框架操作，调用父类方法!!!!!!!!!!!!!!!!!!!!
			//!!!!!!!要获取localCost的值，该方法必须要位于localCost()方法之后，!!!!!!!!
			AlsWork();
			
			if(myIdentity == ACCEPTER){
				if(mySuggestersNumber == 0){
					DsaWork();
				}
				else{
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
									tempSelectCost += constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];
							}
						}
						if(tempSelectCost < selectCost){
							selectValueIndex = i;
							selectCost = tempSelectCost;
						}
					}
					
					valueIndex = selectValueIndex;
				}
			}
			
			for(int i = 0; i < neighbours.length; i++){
				neighboursTag[i] = 0;
			}
			mySuggestersNumber = 0;
			
			sendStepDsaMessages();
			stayUnchanged = 0;
			resetLock = false;
		}
	}
	
	
	private void disposeStepDsaMessage(Message msg){
		//System.out.println("~~~"+id+"~~~"+cycleCount+"~~~");
		STEP = 4;
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
			
			//if(prepareToReset < 10)
			//	System.out.println("~~~ID "+id+"~~~"+"dsaCount "+dsaCount+"~~~"+prepareToReset+"~~~");
			
			localCost=localCost();
			AlsWork();
			if(myIdentity != SUGGESTER)
				DsaWork();
			if(prepareToReset > 0){
				sendStepDsaMessages();
			}
			else{
				//valueIndex = (int)(Math.random()*(domain.length));
				
				prepareToReset = 2147483647;
				myIdentity = SUGGESTER;
				myTag = (int)(100*Math.random()+myTagStandard)*neighboursQuantity;
				
				sendStep1Messages();
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
				
				if(accumulativeCost < bestCostTemp){
					bestCostTemp = accumulativeCost;
					if(theSTEP == 4)
						stayUnchanged = 0;
				}
				else{
					if(theSTEP == 4 && resetLock == false){
						stayUnchanged++;
						if(stayUnchanged >= stayDsaCountInterval){
							stayUnchanged = -2147483646;
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
		if(valueIndexList.isEmpty() == true && STOPRUNNING == true){
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
			if(selectOneMinCost < localCost){
				valueIndex = selectValueIndex;
			}
		}
	}
	
	protected void localSearchCheck(){
		while(msgQueue.size() == 0){
			try {
				Thread.sleep(1);
				System.out.println("!!! sleep(1) !!!!!");
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
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
	
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {

		ResultCycleAls ret=new ResultCycleAls();
		int tag = 0;
		int totalCost = 0;
		for(Map<String, Object> result : results){
			
			//int id_=(Integer)result.get(KEY_ID);
			//String name_=(String)result.get(KEY_NAME);
			//int value_=(Integer)result.get(KEY_VALUE);
			
			if(tag == 0){
				ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
				totalCost = ((Integer)result.get(KEY_BESTCOST));
				tag = 1;
			}
			//String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			//System.out.println(displayStr);
		}
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost));
		ret.totalCost=(int)totalCost;
		return ret;
	}
	
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		return null;
	}
	
	
	@Override
	protected void messageLost(Message msg) {
		// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍?
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}

}
