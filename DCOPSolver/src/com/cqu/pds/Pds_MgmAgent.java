package com.cqu.pds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class Pds_MgmAgent extends AgentCycle {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_GAIN_MESSAGE=1;
	public final static int TYPE_SUGGEST_MESSAGE = 2;
	
	private static int cycleCountEnd;
	private static double utilityPercentage;
	private static double abandonProbability;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";

	public final static int CYCLE_VALUE=567;
	public final static int CYCLE_GAIN=568;
	private int cycleTag = CYCLE_VALUE;
	private int nccc = 0;
	private int gainValue;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;	
	private int neighboursGain[];
	private int[] neighboursValueIndex;	
	private int[] neighboursValueIndexEx;	

//	private int[] neighbourPercentage;
	private double myPercentage;
	private int[] abandonSuggestGain;
	private int suggestValue;
	private int suggestGain;
	private int wait = 0;
	private int suggestTag = 0;
	private int suggester;
//	private int myMaxCost = 0;
//	private int myMinCost = 2147483647;
//	private int[] mySuggestValue;									//给自己的建议值
//	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	
	
	public Pds_MgmAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		utilityPercentage = Settings.settings.getSelectProbabilityA();
		abandonProbability = Settings.settings.getSelectProbabilityB();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		neighboursQuantity=neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		neighboursValueIndexEx = new int[neighboursQuantity];
		neighboursGain=new int[neighboursQuantity];
		
//		mySuggestValue = new int[domain.length];
//		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		abandonSuggestGain = new int[3];
		buildMyTable();
		myPercentage = 1;
//		neighbourPercentage = new int[neighboursQuantity];
		sendValueMessages();
	}
	
	private void buildMyTable(){
//		int localMaxCost = 0;
//		int[] localMinCost = new int[domain.length];
//		int[][] localMinTable = new int[domain.length][neighboursQuantity];
//		for(int i = 0; i < domain.length; i++)
//			localMinCost[i] = 2147483647;
//		
//		for(int i = 0; i < domain.length; i++){
//			int tempLocalMaxCost = 0;
//			int tempLocalCost = 0;
//			int[] tempLocalMinTable = new int[neighboursQuantity];
//			for(int j = 0; j < neighboursQuantity; j++){
//				
//				int oneMinCost,oneMaxCost;
//				oneMinCost = constraintCosts.get(neighbours[j])[i][0];
//				oneMaxCost = constraintCosts.get(neighbours[j])[i][0];
//				tempLocalMinTable[j] = 0;
//				
//				for(int k = 1; k < neighbourDomains.get(neighbours[j]).length; k++){
//					if(oneMinCost > constraintCosts.get(neighbours[j])[i][k]){
//						oneMinCost = constraintCosts.get(neighbours[j])[i][k];
//						tempLocalMinTable[j] = k;
//					}
//					
//					if(oneMaxCost < constraintCosts.get(neighbours[j])[i][k]){
//						oneMaxCost = constraintCosts.get(neighbours[j])[i][k];
//					}
//				}
//				tempLocalCost += oneMinCost;
//				tempLocalMaxCost += oneMaxCost;
//			}
//			
//			if(localMaxCost < tempLocalMaxCost)
//				localMaxCost = tempLocalMaxCost;
//			
//			localMinCost[i] = tempLocalCost;
//			for(int j = 0; j < neighboursQuantity; j++){
//				localMinTable[i][j] = tempLocalMinTable[j];
//			}
//		}
//		myMaxCost = localMaxCost;
//		
//		for(int i = 0; i < domain.length; i++){
//			int minId = 0, minCost = 2147483647;
//			for(int j = 0; j < domain.length; j++){
//				if(localMinCost[j] <= minCost && localMinCost[j] != 2147483647){
//					minId = j;
//					minCost = localMinCost[j];
//				}
//			}
//			mySuggestValue[i] = minId;
//			if(i == 0)
//				myMinCost = localMinCost[minId];
//			for(int j = 0; j < neighboursQuantity; j++)
//				myNeighboursSuggestTable[i][j] = localMinTable[minId][j];
//			localMinCost[minId] = 2147483647;
//		}
	}
	
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_GAIN_MESSAGE, gainValue);
			this.sendMessage(msg);
		}
	}
	
	private void sendSuggestMessages(int neighbourIndex) {
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_SUGGEST_MESSAGE, 
				abandonSuggestGain);
		this.sendMessage(msg);
	}


	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighboursQuantity; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];		
		}
		return localCostTemp;
	}
	
	@Override
	protected void disposeMessage(Message msg) {
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" gainValue="+Infinity.infinityEasy(this.gainValue));
		}
		if(msg.getType()==TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}
		else if(msg.getType()==TYPE_GAIN_MESSAGE)
		{
			disposeGainMessage(msg);
		}
		else if(msg.getType() == TYPE_SUGGEST_MESSAGE){
			disposeSuggestMessage(msg);
		}
	}
	
	private void disposeValueMessage(Message msg) {
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		
		neighboursValueIndexEx[senderIndex] = neighboursValueIndex[senderIndex];
		neighboursValueIndex[senderIndex] = (Integer)(msg.getValue());
	}
	
	private void cycleValue(){
//		System.out.println("cycle "+cycleCount+"   Agent "+id+" values " + valueIndex);
		if(cycleCount>=cycleCountEnd){
			stopRunning();
		}
		else{
			cycleCount++;
			localCost=localCost();
			
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
			
			if(gainValue == 0 && suggestTag != 1){
//				myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
				if(suggestTag == 0){
//					if(myPercentage > utilityPercentage){
						abandon(localCost);
//					}
				}
			}
			
			increaseNccc();
			//System.out.println("agent"+this.id+"_______"+cycleCount+"_______"+gainValue+"________");
			sendGainMessages();
		}
	}
	
	private void disposeGainMessage(Message msg) {
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursGain[senderIndex]=(Integer)msg.getValue();
	}
	
	private void cycleGain(){

		if(wait == 1){
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" wait 1 cycle");
			wait = 0;
			return;
		}
		else if(suggestTag == 1){
			suggestTag = 0;
			int localCostTemp = 0, compareTemp = 0;
			for(int i=0; i<neighbours.length; i++){
				localCostTemp+=constraintCosts.get(neighbours[i])[suggestValue][neighboursValueIndex[i]];
				if(i != suggester){
					compareTemp += constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndexEx[i]];
					compareTemp -= constraintCosts.get(neighbours[i])[suggestValue][neighboursValueIndex[i]];
				}
			}
			compareTemp += suggestGain;
//			myPercentage = ((double)(localCostTemp-myMinCost))/((double)(myMaxCost-myMinCost));
//			if(localCostTemp < localCost || myPercentage < utilityPercentage || compareTemp > 0){
			if(localCostTemp < localCost || compareTemp > 0){
				valueIndex = suggestValue;
				sendValueMessages();
//				System.out.println("cycle "+cycleCount+"   Agent "+id+" accept Agent "+neighbours[suggester]+ " with gain "+compareTemp);
				return;
				//System.out.println("accept!!!!!!!!");
			}
			else{
				//System.out.println("reject!!!!!!!!");
				boolean tag = abandonChain();
				if(tag == true){
					return;
				}
			}
		}
		
		for(int i=0; i<neighboursQuantity; i++){
			if(neighboursGain[i]>=gainValue){
				return;
			}
		}
		valueIndex=selectValueIndex;
		sendValueMessages();
	}
	
	protected void allMessageDisposed(){
		if(cycleTag == CYCLE_VALUE){
			cycleTag = CYCLE_GAIN;
			cycleValue();
		}
		else{
			cycleTag = CYCLE_VALUE;
			cycleGain();
		}
	}

	private double getAbandonP(){
		return Math.exp(-(cycleCount)/(neighboursQuantity*neighboursQuantity));
	}
	
	private void abandon(int nature) {
//		if(Math.random() > abandonProbability)
//			return;

//		if(Math.random() > 0.8*Math.exp(-(Math.abs(msgMailer.getCycleCount()-100))/(neighboursQuantity*50)))
//			return;
	
		if(Math.random() > getAbandonP())
			return;
		
//		if(Math.random() > Math.exp(-cycleCount/(neighboursQuantity*10)))
//			return;
		
		int[] abandonValueIndex = new int[neighboursQuantity];
		int[][] abandonNeighbourIndex = new int[neighboursQuantity][domain.length];
		int[] abandonCost = new int[neighboursQuantity];
		
		int h = (int)(neighboursQuantity*Math.random());
//		for(int h = 0; h < neighbours.length; h++){						//遍历邻居
			int[] selectMinCost=new int[domain.length];
			for(int i=0; i<domain.length; i++){							//遍历值域
				for(int j=0; j<neighbours.length; j++)
					if(j != h)
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
//		}
//		int abandonIndex = 0;
//		for(int i = 1; i < neighboursQuantity; i++){
//			if(abandonCost[i] < abandonCost[abandonIndex])
//				abandonIndex = i;										//找到差值最小的邻居，选作舍弃
//		}
		int abandonIndex = h;
		
		if(abandonCost[abandonIndex] < nature){
			valueIndex = abandonValueIndex[abandonIndex];
			sendValueMessages();
			
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
			abandonSuggestGain[1] = nature - abandonCost[abandonIndex];
			abandonSuggestGain[2] = valueIndex;
			sendSuggestMessages(abandonIndex);
			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" ignore Agent "+neighbours[abandonIndex]+" values "+valueIndex+" suggest "+abandonSuggestGain[0]);
			return;
		}
//		System.out.println("cycle "+cycleCount+"   Agent "+id+" ignore false");
	}
	
	private void disposeSuggestMessage(Message msg) {
		if(wait != 1 && suggestTag !=1 && neighboursQuantity > 1){
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
			neighboursValueIndex[suggester] = ((int[])msg.getValue())[2];
			suggestTag = 1;
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" received Agent "+msg.getIdSender());
			return;
		}
//		System.out.println("cycle "+cycleCount+"   Agent "+id+" reject Agent "+msg.getIdSender());
	}
	
	private boolean abandonChain(){
//		if(Math.random() > abandonProbability)
//			return false;
		
		int[] abandonNeighbourIndex = new int[neighboursQuantity];
		int[] abandonCost = new int[neighboursQuantity];
		for(int h = 0; h < neighbours.length; h++){		//遍历邻居
			if(h != suggester){
				int selectMinCost = 0;
				for(int j=0; j<neighbours.length; j++)
					if(j != h)
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
		}
		int abandonIndex = 0;
		if(suggester == 0)
			abandonIndex = 1;
		for(int i = 1; i < neighboursQuantity; i++){
			if(abandonCost[i] < abandonCost[abandonIndex] && i != suggester)
				abandonIndex = i;											//找到差值最小的邻居，选作舍弃
		}
		if(abandonCost[abandonIndex] < localCost){
			//System.out.println("yes!!!!!!!!");
			valueIndex = suggestValue;
			sendValueMessages();
			
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex];
			abandonSuggestGain[1] = localCost - abandonCost[abandonIndex];
			abandonSuggestGain[2] = valueIndex;
			sendSuggestMessages(abandonIndex);
			wait = 1;
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" accept Agent "+neighbours[suggester]+" then ignore Agent "+neighbours[abandonIndex]+" values "+valueIndex+" suggest "+abandonSuggestGain[0]);
			return true;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
		else{
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" reject Agent "+neighbours[suggester]);
			return false;
		}
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
//		System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		double totalCost=0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
//			int id_=(Integer)result.get(KEY_ID);
//			String name_=(String)result.get(KEY_NAME);
//			int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			totalCost+=((double)((Integer)result.get(KEY_LOCALCOST)))/2;
			
//			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
//			System.out.println(displayStr);
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
		return " ";
	}
	
	public static String messageContent(Message msg){
		
			return "unknown";
	}
	
	@Override
	protected void messageLost(Message msg) {
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}
	
}
