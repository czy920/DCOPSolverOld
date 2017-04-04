package com.cqu.pds;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class Pds_AlsMgm2Agent extends AgentCycleAls {

	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_OFFER_MESSAGE=1;
//	public final static int TYPE_WAIT_MESSAGE=2;
	public final static int TYPE_ACCEPT_MESSAGE=3;
	public final static int TYPE_REJECT_MESSAGE=4;
//	public final static int TYPE_WAITGAIN_MESSAGE=5;
	public final static int TYPE_GAIN_MESSAGE=6;
//	public final static int TYPE_COGAIN_MESSAGE=7;
	public final static int TYPE_DECIDEGO_MESSAGE=8;
//	public final static int TYPE_WAITAGAIN_MESSAGE=9;
	public final static int TYPE_SUGGEST_MESSAGE = 123;
	
	private static double p;
	private static int cycleCountEnd;
	
	public final static String TYPE_OFFER="type_offer";
	public final static String TYPE_RECEIVER="type_receiver";
	public final static String TYPE_UNKNOW="type_unknow";
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	
	public final static int CYCLE_VALUE=561;
	public final static int CYCLE_OFFER=562;
	public final static int CYCLE_ACCEPT=563;
	public final static int CYCLE_GAIN=564;
	public final static int CYCLE_GO=565;
	
	private int cycleTag = CYCLE_VALUE;
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
	private int[] neighboursValueIndexEx;
	
	public final static int go=111;
	public final static int not=222;
	private int isAbleToGo=not;
	private int coordinate;
	private LinkedList<int[]> selectValueGroup;
	private LinkedList<int[]> selectOfferGroup;
	private String ownType;
	
	public final static int higherP = 0;
	public final static int lowerP = 1;
	private double myPercentage=1;
	private double myBestPercentage=1;
	private int selectP = higherP;
	private int myPercentageUnchanged = 0;
//	private double myThreshold = 0.1;
//	private int[] neighbourThreshold;
	private int[] abandonSuggestGain;
	private int suggestValue;
	private int suggestGain;
	private int wait = 0;
	private int suggestTag = 0;
	private int suggester;
	private int[] abandenByOffer;
	private int[] assumeByOffer;
	
	private int myMaxCost = 0;
	private int myMinCost = 2147483647;
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	
	public Pds_AlsMgm2Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
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
		
		neighboursValueIndexEx = new int[neighboursQuantity];
		abandonSuggestGain = new int[3];
		abandenByOffer = new int[neighboursQuantity];
		assumeByOffer = new int[neighboursQuantity];
		for(int i = 0; i < neighboursQuantity; i++)
			abandenByOffer[i] = -1;
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
//		neighbourThreshold = new int[neighboursQuantity];
		buildMyTable();
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
//		int[] valueBox = new int[2];
//		valueBox[0] = valueIndex;
//		valueBox[1] = (int)(myThreshold*100000);
//		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
//			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueBox);
//			this.sendMessage(msg);
//		}
		
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendOfferMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], Pds_Mgm2Agent.TYPE_OFFER_MESSAGE, selectValueGroup);
		this.sendMessage(msg);
	}
	
//	private void sendWaitMessages(int neighbourIndex){
//		Message msg=new Message(this.id, neighbours[neighbourIndex], Pds_Mgm2Agent.TYPE_WAIT_MESSAGE, valueIndex);
//		this.sendMessage(msg);
//	}
	
	private void sendAcceptMessages(int[] temp){
		Message msg=new Message(this.id, neighbours[coordinate], Pds_Mgm2Agent.TYPE_ACCEPT_MESSAGE, temp);
		this.sendMessage(msg);
	}
	
	private void sendRejectMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], Pds_Mgm2Agent.TYPE_REJECT_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
//	private void sendWaitGainMessages(int neighbourIndex){
//		Message msg=new Message(this.id, neighbours[neighbourIndex], Pds_Mgm2Agent.TYPE_WAITGAIN_MESSAGE, valueIndex);
//		this.sendMessage(msg);
//	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity ; neighbourIndex++){
			if(neighbourIndex!=coordinate){
				Message msg=new Message(this.id, neighbours[neighbourIndex], Pds_Mgm2Agent.TYPE_GAIN_MESSAGE, gainValue);
				this.sendMessage(msg);
			}
		}
	}
	
//	private void sendCoGainMessages(){
//		Message msg=new Message(this.id, neighbours[coordinate], Pds_Mgm2Agent.TYPE_COGAIN_MESSAGE, valueIndex);
//		this.sendMessage(msg);
//	}
	
	private void sendDecideGoMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], Pds_Mgm2Agent.TYPE_DECIDEGO_MESSAGE, isAbleToGo);
		this.sendMessage(msg);
	}
	
//	private void sendWaitAgainMessages(){
//		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
//			if(neighbourIndex!=coordinate){
//				Message msg=new Message(this.id, neighbours[neighbourIndex], Pds_Mgm2Agent.TYPE_WAITAGAIN_MESSAGE, valueIndex);
//				this.sendMessage(msg);
//			}
//		}
//	}
	
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
	
//	protected void work(int i){
//		wrong = 0;
//		if(i != neighboursQuantity){
//			wrong = 1;
//			wrongNumber = i;
//		}
//	}
	
	protected void disposeMessage(Message msg) {
		
//		receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
		
//		//纠错部分，找到message未收全的Agent
//		if(wrong == 1){
//			receivedWrongNumber++;
//			System.out.println("Agent "+this.id+"____"+"cycleCount "+cycleCount+"____"+"neighbour数 "+neighboursQuantity+"____"+"邻居"+msg.getIdSender()+"____"+"收到 "+wrongNumber+"____"+
//					"第 "+receivedQuantity+"____"+"类型 "+msg.getType());
//			if(receivedWrongNumber == wrongNumber){
//				int ii = 1;
//				ii = ii/0;
//			}
//		}
		
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" gainValue="+Infinity.infinityEasy(this.gainValue));
		}
		if(msg.getType()==Pds_Mgm2Agent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}
		else if(msg.getType()==Pds_Mgm2Agent.TYPE_OFFER_MESSAGE)
		{
			disposeOfferMessage(msg);
		}
//		else if(msg.getType()==Pds_Mgm2Agent.TYPE_WAIT_MESSAGE)
//		{
//			disposeWaitMessage(msg);
//		}
		else if(msg.getType()==Pds_Mgm2Agent.TYPE_ACCEPT_MESSAGE)
		{
			disposeAcceptMessage(msg);
		}
		else if(msg.getType()==Pds_Mgm2Agent.TYPE_REJECT_MESSAGE)
		{
			disposeRejectMessage(msg);
		}
//		else if(msg.getType()==Pds_Mgm2Agent.TYPE_WAITGAIN_MESSAGE)
//		{
//			disposeWaitGainMessage(msg);
//		}
		else if(msg.getType()==Pds_Mgm2Agent.TYPE_GAIN_MESSAGE)
		{
			disposeGainMessage(msg);
		}
//		else if(msg.getType()==Pds_Mgm2Agent.TYPE_COGAIN_MESSAGE)
//		{
//			disposeCoGainMessage(msg);
//		}
		else if(msg.getType()==Pds_Mgm2Agent.TYPE_DECIDEGO_MESSAGE)
		{
			disposeDecideGoMessage(msg);
		}
//		else if(msg.getType()==Pds_Mgm2Agent.TYPE_WAITAGAIN_MESSAGE)
//		{
//			disposeWaitAgainMessage(msg);
//		}
		else if(msg.getType() == TYPE_SUGGEST_MESSAGE){
			disposeSuggestMessage(msg);
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
	
	private void disposeValueMessage(Message msg) {
		//System.out.println("value");		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighboursQuantity; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndexEx[senderIndex] = neighboursValueIndex[senderIndex];
		neighboursValueIndex[senderIndex] = ((int)(msg.getValue()));
//		neighboursValueIndex[senderIndex] = ((int[])(msg.getValue()))[0];
//		neighbourThreshold[senderIndex] = ((int[])(msg.getValue()))[1];
		
		if(receivedQuantity==0){
			
		}
		//System.out.println("value_end");
	}
	
	private void cycleForValue(){

			cycleCount++;
			localCost=localCost();
			AlsWork();
			
//			int sum = (int)(myThreshold*100000); 
//			for(int i = 0; i < neighboursQuantity; i++){
//				sum+=neighbourThreshold[i];
//			}
//			myThreshold = sum/((neighboursQuantity+1)*100000.0);
			
			int[] selectMinCost=new int[domain.length];
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
			
			isAbleToGo=not;
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
//			else{
//				if(gainValue == 0){
//					if(suggestTag == 0){
//						myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
//						abandon(localCost);
//					}
//				}
//			}
			
//			for(int neighbourIndex = 0; neighbourIndex < neighboursQuantity; neighbourIndex++){
//				if(neighbourIndex!=coordinate){
//					sendWaitMessages(neighbourIndex);
//				}
//			}
		
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
			
//			int[] tempCoGain = new int[tempMap.size()];
//			for(int i = 0; i < tempMap.size(); i++)
//				tempCoGain[i] = tempList[i][2];
//			for(int i=0; i<tempList.length; i++){
//				tempCoGain[i]+=localCost;
//				tempCoGain[i]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
//				
//				for(int j=0; j<neighbours.length; j++){
//					if(j!=senderIndex)
//						tempCoGain[i]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
//				}
//				increaseNccc();
//			}
//			int selectTemp=0;
//			for(int i=1; i<tempList.length; i++){
//				if(tempCoGain[selectTemp] < tempCoGain[i])
//					selectTemp=i;
//			}
//			if(tempCoGain[selectTemp] > 0){
//				tempList[selectTemp][2] = tempCoGain[selectTemp];
//				selectOfferGroup.add(tempList[selectTemp]);
//			}
//			else{
//				
//				myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
////				if(Math.random() < abandonProbability && myPercentage > utilityPercentage){
//				if(Math.random() < getAbandonP()){
//					int[] abandonValueIndex = new int[neighboursQuantity];
//					int[][] abandonNeighbourIndex = new int[neighboursQuantity][domain.length];
//					int[] abandonGain = new int[neighboursQuantity];
//					int h = (int)(neighboursQuantity*Math.random());
////					for(int h = 0; h < neighboursQuantity; h++){							//遍历邻居
//						if(h != senderIndex){
//							//int[] selectMinCost=new int[domain.length];
//							for(int i = 0; i < tempMap.size(); i++)
//								tempCoGain[i] = tempList[i][2];
//							
//							for(int i=0; i<tempList.length; i++){						//遍历可取域，i为可取值得编号，
//								tempCoGain[i]+=localCost;
//								tempCoGain[i]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
//								for(int j=0; j<neighbours.length; j++)
//									if(j != h && j != senderIndex)
//										tempCoGain[i]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
//								
//								int tempIndex = 0;
//								int tempCost = constraintCosts.get(neighbours[h])[tempList[i][0]][tempIndex];
//								for(int hValue = 0; hValue < neighbourDomains.get(neighbours[h]).length; hValue++){
//									if(constraintCosts.get(neighbours[h])[tempList[i][0]][hValue] < tempCost){
//										tempCost = constraintCosts.get(neighbours[h])[tempList[i][0]][hValue];
//										tempIndex = hValue;								//找到选定的最小值，记录value和cost
//									}
//								}
//								tempCoGain[i] -= tempCost;								//做累计
//								abandonNeighbourIndex[h][i] = tempIndex;				//记录对应的邻居号和自己value对应的邻居的value建议
//							}
//							int selectMaxGain=tempCoGain[0];
//							for(int i = 1; i < tempCoGain.length; i++){
//								if(selectMaxGain < tempCoGain[i]){
//									selectMaxGain = tempCoGain[i];
//									abandonValueIndex[h] = i;
//								}
//							}
//							abandonGain[h] = selectMaxGain;								//找到每一个邻居对应的自己最小的value
//						}
////					}
////					int abandonIndex = 0;
////					for(int i = 1; i < neighboursQuantity; i++){
////						if(i != senderIndex)
////							if(abandonGain[i] > abandonGain[abandonIndex])
////								abandonIndex = i;										//找到差值最小的邻居，选作舍弃
////					}
//					int abandonIndex = h;
//					
//					if(abandonGain[abandonIndex] > 0){
//						tempList[abandonValueIndex[abandonIndex]][2] = abandonGain[abandonIndex];
//						selectOfferGroup.add(tempList[abandonValueIndex[abandonIndex]]);
//						abandenByOffer[senderIndex] = abandonIndex;
//						assumeByOffer[senderIndex] = abandonNeighbourIndex[abandonIndex][abandonValueIndex[abandonIndex]];
//					}
//					else{
//						sendRejectMessages(senderIndex);
//					}
//				}
//				else{
//					sendRejectMessages(senderIndex);
//				}
//			}	
			
		}else
			sendRejectMessages(senderIndex);
		//System.out.println("offer_end");
	}
	
//	private void disposeWaitMessage(Message msg) {
//		//System.out.println("wait");
//		int senderIndex=0;
//		int senderId=msg.getIdSender();
//		for(int i=0; i<neighboursQuantity; i++){
//			if(neighbours[i]==senderId){
//				senderIndex=i;
//				break;
//			}
//		}
//		sendWaitGainMessages(senderIndex);
//		//System.out.println("wait_end");
//	}
	
	private void cycleForOffer(){
		if(ownType != TYPE_OFFER){
			if(selectOfferGroup.isEmpty() != true){
				gather();
			}
		}
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
	
//	private void gather() {
//		//System.out.println("gather");
//		int temp[]=selectOfferGroup.get(0);
//		for(int i=1; i<selectOfferGroup.size(); i++){
//			if(temp[2]<selectOfferGroup.get(i)[2]){
//				sendRejectMessages(temp[3]);
//				temp=selectOfferGroup.get(i);
//			}
//			else{
//				sendRejectMessages(selectOfferGroup.get(i)[3]);
//			}
//		}
//		ownType=TYPE_RECEIVER;
//		selectValueIndex=temp[0];
//		gainValue=temp[2];
//		coordinate=temp[3];
//		
//		if(abandenByOffer[coordinate] != -1){
//			abandonSuggestGain[0] = assumeByOffer[coordinate];
//			abandonSuggestGain[1] = temp[2];
//			sendSuggestMessages(abandenByOffer[coordinate]);
////			mustGo = true;
////			valueIndex = selectValueIndex;						/** 这里直接改变自值，未考虑offer  */
//		}
//		for(int i = 0; i < neighboursQuantity; i++)
//			abandenByOffer[i] = -1;
//		sendAcceptMessages(temp);
//		//System.out.println("gather_end");
//	}
	
	private void disposeAcceptMessage(Message msg) {
		//System.out.println("accept");
		selectValueIndex=((int[])(msg.getValue()))[1];
		gainValue=((int[])(msg.getValue()))[2];
		
		//System.out.println("accept_end");
	}
	
	private void disposeRejectMessage(Message msg) {
		//System.out.println("reject");
		ownType=TYPE_UNKNOW;
		coordinate=-1;

		//System.out.println("reject_end");
	}
	
//	private void disposeWaitGainMessage(Message msg) {
//		//System.out.println("waitgain");
//		
//		//System.out.println("waitgain_end");
//	}
	
	private void cycleForAccept(){
		sendGainMessages();
//		if(coordinate!=-1){
//			sendCoGainMessages();
//		}
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
		
		//System.out.println("gain_end");
	}
	
//	private void disposeCoGainMessage(Message msg){
//		//System.out.println("cogain");
//		
//		//System.out.println("cogain_end");
//	}
	
	private void cycleForGain(){
		/** */
		checkMyPercentage();
		for(int i=0; i<neighboursQuantity; i++){
			if(i!=coordinate){
				if(neighboursGain[i]>=gainValue){
					isAbleToGo=not;
					if(coordinate!=-1){	
						sendDecideGoMessages();
					}
					abandon(localCost);
//					sendWaitAgainMessages();
					return;
				}
			}
		}
		isAbleToGo=go;
		if(coordinate!=-1){	
			sendDecideGoMessages();
		}
//		sendWaitAgainMessages();
	}
	
	private void disposeDecideGoMessage(Message msg) {
		//System.out.println("decide");
		if((Integer)(msg.getValue()) == go && isAbleToGo == go){
			valueIndex=selectValueIndex;
			sendValueMessages();
		}

		//System.out.println("decide_end");
	}
	
//	private void disposeWaitAgainMessage(Message msg) {
//		//System.out.println("waitagain");
//
//		//System.out.println("waitagain_end");
//	}
	
	private void cycleForGo(){
		if(isAbleToGo==go && coordinate == -1){
			valueIndex=selectValueIndex;
			sendValueMessages();
		}
		
		if(wait == 1){
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" wait 1 cycle");
			wait = 0;
//			sendValueMessages();
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
//					sendValueMessages();
					return;
				}
			}
		}
		
	}
	
	protected void allMessageDisposed(){
		if(cycleCount>=cycleCountEnd){
			AlsStopRunning();
		}
		else{
			if(cycleTag == CYCLE_VALUE){
				cycleTag = CYCLE_OFFER;
				cycleForValue();
			}
			else if(cycleTag == CYCLE_OFFER){
				AlsWork();
				cycleTag = CYCLE_ACCEPT;
				cycleForOffer();
			}
			else if(cycleTag == CYCLE_ACCEPT){
				AlsWork();
				cycleTag = CYCLE_GAIN;
				cycleForAccept();
			}
			else if(cycleTag == CYCLE_GAIN){
				AlsWork();
				cycleTag = CYCLE_GO;
				cycleForGain();
			}
			else if(cycleTag == CYCLE_GO){
				AlsWork();
				cycleTag = CYCLE_VALUE;
				cycleForGo();
			}
		}
	}
	
	private void checkMyPercentage(){
		myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
		if(selectP == higherP){
			if(myBestPercentage > myPercentage){
				myBestPercentage = myPercentage;
				myPercentageUnchanged = 0;
			}
			else{
				myPercentageUnchanged++;
				if(myPercentageUnchanged > 0.1*cycleCountEnd){
					selectP = lowerP;
//					System.out.println(cycleCount);
				}
			}
		}
	}
	
	private double getAbandonP(){
//		if(myPercentage > myThreshold){
//			myThreshold = myThreshold*(1+(neighboursQuantity/100.0));
//			return Math.exp(-(cycleCount)/(Math.pow(neighboursQuantity, 1.5)));
//		}
//		else{
//			myThreshold = myThreshold*(0.99);
//			return Math.exp(-(cycleCount)/(Math.pow(neighboursQuantity, 1)));
//		}
		
//		return myPercentage*(cycleCountEnd-cycleCount)/cycleCountEnd;
		
		if(selectP == higherP)
			return Math.sqrt(myPercentage)*(cycleCountEnd-cycleCount)/cycleCountEnd;
		else
			return myPercentage*(cycleCountEnd-cycleCount)/cycleCountEnd;
	}
	
	private void abandon(int nature) {
		if(Math.random() > getAbandonP())
			return;
		
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
//			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
//			System.out.println("cycle "+cycleCount+"   Agent "+id+" ignore Agent "+neighbours[abandonIndex]+" values "+valueIndex+" suggest "+abandonSuggestGain[0]);
			return;
		}
//		System.out.println("cycle "+cycleCount+"   Agent "+id+" ignore false");
	}
	
	private void disposeSuggestMessage(Message msg) {
		if(wait != 1 && suggestTag !=1 && neighboursQuantity > 1 ){
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
	
	/**
	 * 打乱MGM周期节奏的消息处理模式
	 */
//	private void disposeSuggestMessage(Message msg) {
//		if(wait != 1 && neighboursQuantity > 1 ){
//			
//			int senderIndex=0;
//			int senderId=msg.getIdSender();
//			for(int i=0; i<neighbours.length; i++){
//				if(neighbours[i]==senderId){
//					senderIndex=i;
//					break;
//				}
//			}
//			suggester = senderIndex;
//			suggestValue = ((int[])msg.getValue())[0];
//			suggestGain = ((int[])msg.getValue())[1];
//			neighboursValueIndex[suggester] = ((int[])msg.getValue())[2];
//			
//			int localCostTemp = 0, compareTemp = 0;
//			for(int i=0; i<neighbours.length; i++){
//				localCostTemp+=constraintCosts.get(neighbours[i])[suggestValue][neighboursValueIndex[i]];
//				if(i != suggester){
//					compareTemp += constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndexEx[i]];
//					compareTemp -= constraintCosts.get(neighbours[i])[suggestValue][neighboursValueIndex[i]];
//				}
//			}
//			compareTemp += suggestGain;
//			if(localCostTemp < localCost || compareTemp > 0){
//				valueIndex = suggestValue;
//				sendValueMessages();
//				return;
//			}
//			else{
//				boolean tag = abandonChain();
//				if(tag == true){
//					return;
//				}
//			}
//		}
//	}
	
	
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
//			wait = 1;
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
		result.put(KEY_BESTCOST, this.bestCost);
		result.put(KEY_BESTCOSTINCYCLE, bestCostInCycle);
		result.put(KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
//		System.out.println("Agent "+this.name+" stopped!");
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
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
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
		ret.nccc=(int)ncccTemp;
		return ret;
	}
	
	private void increaseNccc(){
		nccc++;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+Pds_Mgm2Agent.messageContent(msg);
	}
	
	public static String messageContent(Message msg){
		
		return "unknown";
		
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
