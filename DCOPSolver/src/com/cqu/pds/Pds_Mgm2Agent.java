package com.cqu.pds;

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

public class Pds_Mgm2Agent extends AgentCycle {

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
	public final static int TYPE_SUGGEST_MESSAGE = 10;
	public final static int TYPE_PD_MESSAGE = 11;
	
	public final static String TYPE_OFFER="type_offer";
	public final static String TYPE_RECEIVER="type_receiver";
	public final static String TYPE_UNKNOW="type_unknow";
	
	private static double p;
	private static int cycleCountEnd;
	private static double utilityPercentage;
	private static double abandonProbability;
	
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
	private int[] neighboursValueIndexEx;	
	
	public final static int go = 1;
	public final static int not = 2;
	
	private int isAbleToGo = not;
	private int coordinate;
	private LinkedList<int[]> selectValueGroup;
	private LinkedList<int[]> selectOfferGroup;
	private String ownType;
	
//	private int[] neighbourPercentage;
	private double myPercentage;
	private int[] abandonSuggestGain;
	private int suggestValue;
	private int suggestGain;
	private int wait = 0;
	private int suggestTag = 0;
	private int suggester;
	private int myMaxCost = 0;
	private int myMinCost = 2147483647;
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	private String STEP;
	private int[] abandenByOffer;
	private int[] assumeByOffer;
	private boolean mustGo = false;
	private int PDaccept = 1;
	private int PDreject = 0;
	private int orgValue = 0;
	
	public Pds_Mgm2Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}
	
	protected void initRun() {
		super.initRun();

		cycleCountEnd = Settings.settings.getCycleCountEnd();
		p = Settings.settings.getSelectProbability();
		utilityPercentage = Settings.settings.getSelectProbabilityA();
		abandonProbability = Settings.settings.getSelectProbabilityB();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		coordinate=-1;
		ownType=TYPE_UNKNOW;
		neighboursQuantity=neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		neighboursValueIndexEx = new int[neighboursQuantity];
		selectValueGroup=new LinkedList<int[]>();
		selectOfferGroup=new LinkedList<int[]>();
		neighboursGain=new int[neighboursQuantity];
		
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		abandonSuggestGain = new int[2];
		buildMyTable();
		myPercentage = 1;
		abandenByOffer = new int[neighboursQuantity];
		assumeByOffer = new int[neighboursQuantity];
		for(int i = 0; i < neighboursQuantity; i++)
			abandenByOffer[i] = -1;
//		neighbourPercentage = new int[neighboursQuantity];
		
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
	
	private void sendOfferMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], TYPE_OFFER_MESSAGE, selectValueGroup);
		this.sendMessage(msg);
	}
	
	private void sendWaitMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_WAIT_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendAcceptMessages(int[] temp){
		Message msg=new Message(this.id, neighbours[coordinate], TYPE_ACCEPT_MESSAGE, temp);
		this.sendMessage(msg);
	}
	
	private void sendRejectMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_REJECT_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendWaitGainMessages(int neighbourIndex){
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_WAITGAIN_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	
	private void sendGainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity ; neighbourIndex++){
			if(neighbourIndex!=coordinate){
				Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_GAIN_MESSAGE, gainValue);
				this.sendMessage(msg);
			}
		}
	}
	
	private void sendCoGainMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], TYPE_COGAIN_MESSAGE, valueIndex);
		this.sendMessage(msg);
	}
	private void sendDecideGoMessages(){
		Message msg=new Message(this.id, neighbours[coordinate], TYPE_DECIDEGO_MESSAGE, isAbleToGo);
		this.sendMessage(msg);
	}
	
	private void sendWaitAgainMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			if(neighbourIndex!=coordinate){
				Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_WAITAGAIN_MESSAGE, valueIndex);
				this.sendMessage(msg);
			}
		}
	}
	
	private void sendSuggestMessages(int neighbourIndex) {
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_SUGGEST_MESSAGE, 
				abandonSuggestGain);
		this.sendMessage(msg);
	}
	
	private void sendPDMessages(int neighbourIndex, int acceptOrReject) {
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_PD_MESSAGE, 
				acceptOrReject);
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
	
	@Override
	protected void disposeMessage(Message msg) {
		//System.out.println(cycleCount+"____"+this.id);
		
		//纠错部分，找到message未收全的Agent
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
		if(msg.getType()==TYPE_VALUE_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeValueMessage(msg);
		}else if(msg.getType()==TYPE_OFFER_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeOfferMessage(msg);
		}else if(msg.getType()==TYPE_WAIT_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeWaitMessage(msg);
		}else if(msg.getType()==TYPE_ACCEPT_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeAcceptMessage(msg);
		}else if(msg.getType()==TYPE_REJECT_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeRejectMessage(msg);
		}else if(msg.getType()==TYPE_WAITGAIN_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeWaitGainMessage(msg);
		}else if(msg.getType()==TYPE_GAIN_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeGainMessage(msg);
		}else if(msg.getType()==TYPE_COGAIN_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeCoGainMessage(msg);
		}else if(msg.getType()==TYPE_DECIDEGO_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeDecideGoMessage(msg);
		}else if(msg.getType()==TYPE_WAITAGAIN_MESSAGE)
		{
			receivedQuantity=(receivedQuantity+1)%neighboursQuantity;
			disposeWaitAgainMessage(msg);
		}
		else if(msg.getType() == TYPE_SUGGEST_MESSAGE){
			disposeSuggestMessage(msg);
		}
		else if(msg.getType() == TYPE_PD_MESSAGE){
//			disposePDMessage(msg);
		}
	}
	
	private void disposeValueMessage(Message msg) {
		STEP="value";
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
		neighboursValueIndexEx[senderIndex] = neighboursValueIndex[senderIndex];
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
				else{
					if(gainValue == 0){
						myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
						if(suggestTag == 0){
							if(myPercentage > utilityPercentage){
								abandon(localCost);
							}
						}
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
	
	private void abandon(int nature) {
		if(Math.random() > abandonProbability)
			return;
		
		int[] abandonValueIndex = new int[neighboursQuantity];
		int[][] abandonNeighbourIndex = new int[neighboursQuantity][domain.length];
		int[] abandonCost = new int[neighboursQuantity];
		for(int h = 0; h < neighbours.length; h++){						//遍历邻居
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
		}
		int abandonIndex = 0;
		for(int i = 1; i < neighboursQuantity; i++){
			if(abandonCost[i] < abandonCost[abandonIndex])
				abandonIndex = i;										//找到差值最小的邻居，选作舍弃
		}
		if(abandonCost[abandonIndex] < nature){
			orgValue = valueIndex;
			valueIndex = abandonValueIndex[abandonIndex];
			
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
			abandonSuggestGain[1] = nature - abandonCost[abandonIndex];
			sendSuggestMessages(abandonIndex);
			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
	}
	
	private void disposeOfferMessage(Message msg) {
		STEP="offer";
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
			
			@SuppressWarnings("unchecked")
			LinkedList<int[]> tempMap = ((LinkedList<int[]>)(msg.getValue()));
			int tempList[][] = new int[tempMap.size()][4];
			for(int i=0; i<tempMap.size(); i++){
				tempList[i][0]=tempMap.get(i)[0];
				tempList[i][1]=tempMap.get(i)[1];
				tempList[i][2]=tempMap.get(i)[2];
				tempList[i][3]=senderIndex;
			}
			
//			for(int i=0; i<tempList.length; i++){
//				tempList[i][2]+=localCost;
//				tempList[i][2]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
//				
//				for(int j=0; j<neighbours.length; j++){
//					if(j!=senderIndex)
//						tempList[i][2]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
//				}
//				increaseNccc();
//			}
//			int selectTemp=0;
//			for(int i=1; i<tempList.length; i++){
//				if(tempList[selectTemp][2] < tempList[i][2])
//					selectTemp=i;
//			}
//			if(tempList[selectTemp][2] > 0){
//				selectOfferGroup.add(tempList[selectTemp]);
//			}
//			else{
//				sendRejectMessages(senderIndex);
////				System.out.println("NO " + " id " + id  + " cycle " + cycleCount);
//			}
			
			int[] tempCoGain = new int[tempMap.size()];
			for(int i = 0; i < tempMap.size(); i++)
				tempCoGain[i] = tempList[i][2];
			for(int i=0; i<tempList.length; i++){
				tempCoGain[i]+=localCost;
				tempCoGain[i]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
				
				for(int j=0; j<neighbours.length; j++){
					if(j!=senderIndex)
						tempCoGain[i]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
				}
				increaseNccc();
			}
			int selectTemp=0;
			for(int i=1; i<tempList.length; i++){
				if(tempCoGain[selectTemp] < tempCoGain[i])
					selectTemp=i;
			}
			if(tempCoGain[selectTemp] > 0){
				tempList[selectTemp][2] = tempCoGain[selectTemp];
				selectOfferGroup.add(tempList[selectTemp]);
			}
			else{
				
				myPercentage = ((double)(localCost-myMinCost))/((double)(myMaxCost-myMinCost));
				if(Math.random() > abandonProbability && myPercentage > utilityPercentage){
					int[] abandonValueIndex = new int[neighboursQuantity];
					int[][] abandonNeighbourIndex = new int[neighboursQuantity][domain.length];
					int[] abandonGain = new int[neighboursQuantity];
					for(int h = 0; h < neighboursQuantity; h++){							//遍历邻居
						if(h != senderIndex){
							//int[] selectMinCost=new int[domain.length];
							for(int i = 0; i < tempMap.size(); i++)
								tempCoGain[i] = tempList[i][2];
							for(int i=0; i<tempList.length; i++){						//遍历可取域，i为可取值得编号，
								tempCoGain[i]+=localCost;
								tempCoGain[i]-=constraintCosts.get(neighbours[senderIndex])[valueIndex][neighboursValueIndex[senderIndex]];
								for(int j=0; j<neighbours.length; j++)
									if(j != h && j != senderIndex)
										tempCoGain[i]-=constraintCosts.get(neighbours[j])[tempList[i][0]][neighboursValueIndex[j]];
								
								int tempIndex = 0;
								int tempCost = constraintCosts.get(neighbours[h])[tempList[i][0]][tempIndex];
								for(int hValue = 0; hValue < neighbourDomains.get(neighbours[h]).length; hValue++){
									if(constraintCosts.get(neighbours[h])[tempList[i][0]][hValue] < tempCost){
										tempCost = constraintCosts.get(neighbours[h])[tempList[i][0]][hValue];
										tempIndex = hValue;								//找到选定的最小值，记录value和cost
									}
								}
								tempCoGain[i] -= tempCost;								//做累计
								abandonNeighbourIndex[h][i] = tempIndex;				//记录对应的邻居号和自己value对应的邻居的value建议
							}
							int selectMaxGain=tempCoGain[0];
							for(int i = 1; i < tempCoGain.length; i++){
								if(selectMaxGain < tempCoGain[i]){
									selectMaxGain = tempCoGain[i];
									abandonValueIndex[h] = i;
								}
							}
							abandonGain[h] = selectMaxGain;							//找到每一个邻居对应的自己最小的value
						}
					}
					int abandonIndex = 0;
					for(int i = 1; i < neighboursQuantity; i++){
						if(i != senderIndex)
							if(abandonGain[i] > abandonGain[abandonIndex])
								abandonIndex = i;										//找到差值最小的邻居，选作舍弃
					}
					
					if(abandonGain[abandonIndex] > 0){
						tempList[abandonValueIndex[abandonIndex]][2] = abandonGain[abandonIndex];
						selectOfferGroup.add(tempList[abandonValueIndex[abandonIndex]]);
						abandenByOffer[senderIndex] = abandonIndex;
						assumeByOffer[senderIndex] = abandonNeighbourIndex[abandonIndex][abandonValueIndex[abandonIndex]];
					}
					else{
						sendRejectMessages(senderIndex);
					}
				}
				else{
					sendRejectMessages(senderIndex);
				}
			}
			
			if(receivedQuantity==0 && selectOfferGroup.isEmpty() != true){
				gather();
			}
		}
		else{
			sendRejectMessages(senderIndex);
		}
		//System.out.println("offer_end");
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
			suggestTag = 1;
		}
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
			orgValue = valueIndex;
			valueIndex = suggestValue;
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex];
			abandonSuggestGain[1] = localCost - abandonCost[abandonIndex];
			sendSuggestMessages(abandonIndex);
			sendPDMessages(suggester, PDaccept);
			wait = 1;
			return true;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
		else{
			sendPDMessages(suggester, PDreject);
			return false;
		}
	}
	
	private void disposePDMessage(Message msg){
		if((Integer)(msg.getValue()) == PDreject){
			valueIndex = orgValue;
		}
	}
	
	private void disposeWaitMessage(Message msg) {
		STEP="wait";
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
		STEP="gather";
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
		
		if(abandenByOffer[coordinate] != -1){
			abandonSuggestGain[0] = assumeByOffer[coordinate];
			abandonSuggestGain[1] = temp[2];
			sendSuggestMessages(abandenByOffer[coordinate]);
			mustGo = true;
		}
		for(int i = 0; i < neighboursQuantity; i++)
			abandenByOffer[i] = -1;
		sendAcceptMessages(temp);
		//System.out.println("gather_end");
	}
	
	private void disposeAcceptMessage(Message msg) {
		STEP="accept";
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
		STEP="reject";
		//System.out.println("reject");
		ownType=TYPE_UNKNOW;
		coordinate=-1;
		if(receivedQuantity==0){
			sendGainMessages();
		}
		//System.out.println("reject_end");
	}
	
	
	private void disposeWaitGainMessage(Message msg) {
		STEP="waitgain";
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
		STEP="gain";
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
						isAbleToGo=not;
						if(coordinate!=-1){
							if(mustGo == true){
								isAbleToGo = go;
								mustGo = false;
							}
							sendDecideGoMessages();
						}
						sendWaitAgainMessages();
						return;
					}
				}
			}
			isAbleToGo=go;
			if(coordinate!=-1){
				if(mustGo == true){
					isAbleToGo = go;
					mustGo = false;
				}
				sendDecideGoMessages();
			}
			sendWaitAgainMessages();
		}

		//System.out.println("gain_end");
	}
	
	
	private void disposeCoGainMessage(Message msg){
		STEP="cogain";
		//System.out.println("cogain");
		if(receivedQuantity==0){
			for(int i=0; i<neighboursQuantity; i++){
				if(i!=coordinate){
					if(neighboursGain[i]>=gainValue){
						isAbleToGo=not;
						if(coordinate!=-1){	
							if(mustGo == true){
								isAbleToGo = go;
								mustGo = false;
							}
							sendDecideGoMessages();
						}
						sendWaitAgainMessages();
						return;
					}
				}
			}
			isAbleToGo=go;
			if(coordinate!=-1){	
				if(mustGo == true){
					isAbleToGo = go;
					mustGo = false;
				}
				sendDecideGoMessages();
			}
			sendWaitAgainMessages();
		}
		//System.out.println("cogain_end");
	}
	
	
	private void disposeDecideGoMessage(Message msg) {
		STEP="decide";
		//System.out.println("decide");
		if(receivedQuantity==0){
			if(wait == 1){
				wait = 0;
				sendValueMessages();
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
				myPercentage = ((double)(localCostTemp-myMinCost))/((double)(myMaxCost-myMinCost));
				if(localCostTemp < localCost || myPercentage < utilityPercentage || compareTemp > 0){
					valueIndex = suggestValue;
					sendPDMessages(suggester, PDaccept);
					sendValueMessages();
					return;
					//System.out.println("accept!!!!!!!!");
				}
				else{
					//System.out.println("reject!!!!!!!!");
					boolean tag = abandonChain();
					if(tag == true){
						sendValueMessages();
						return;
					}
				}
			}
			
			if((Integer)(msg.getValue()) == go && isAbleToGo == go)
				valueIndex=selectValueIndex;
			sendValueMessages();
		}
		//System.out.println("decide_end");
	}
	
	
	private void disposeWaitAgainMessage(Message msg) {
		STEP="waitagain";
		//System.out.println("waitagain");
		if(receivedQuantity==0){
			if(wait == 1){
				wait = 0;
				sendValueMessages();
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
				myPercentage = ((double)(localCostTemp-myMinCost))/((double)(myMaxCost-myMinCost));
				if(localCostTemp < localCost || myPercentage < utilityPercentage || compareTemp > 0){
					valueIndex = suggestValue;
					sendPDMessages(suggester, PDaccept);
					sendValueMessages();
					return;
					//System.out.println("accept!!!!!!!!");
				}
				else{
					//System.out.println("reject!!!!!!!!");
					boolean tag = abandonChain();
					if(tag == true){
						sendValueMessages();
						return;
					}
				}
			}
			
			if(isAbleToGo==go && coordinate == -1)
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
		return "";
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
