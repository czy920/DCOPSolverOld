package com.cqu.pds;

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

public class Pds_DsaSdpAgent extends AgentCycleAls{
	
	public final static int TYPE_VALUE_MESSAGE = 1;
	public final static int TYPE_SUGGEST_MESSAGE = 2;
	public final static int TYPE_DENSITY_MESSAGE = 3;
	
//	public final static int TYPE_RESET_MESSAGE = 5;
	
	private static int cycleCountEnd;
	private static int r;
	private static double pA;
	private static double pB;
	private static double pC;
	private static double pD;
//	private static int stayDsaCountInterval;						//设置DSA操作若干轮无优化效果及重启
	
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int[] neighboursValueIndex;	
	private int[] neighboursValueIndexEx;	
	
	private int bestCostTemp = 2147483647;
	private int[] mySuggestValue;									//给自己的建议值
	private int[][] myNeighboursSuggestTable;						//给邻居的建议值
	private int myMaxCost = 0;
	private int myMinCost = 2147483647;
//	double[] mp = new double[1000];
	
	/**  自适应重置部分  */
//	private boolean resetLock = false;
//	private int stayUnchanged = 0;
//	private int prepareToReset = 2147483647;
//	private boolean newCycle = true;
	
	public final static int higherP = 0;
	public final static int lowerP = 1;
//	private boolean isNeighborsChanged = false;
	private double myPercentage=1;
	private double myBestPercentage=1;
	private int selectP = higherP;
	private int myPercentageUnchanged = 0;
//	private double myThreshold = 0.5;
//	private int[] neighbourThreshold;
	private int[] abandonSuggestGain;
	private int suggestValue;
	private int suggestGain;
	private int wait = 0;
	private int suggestTag = 0;
	private int suggester;
	
	private int neighborsOfNeighbors = 0;
	private int density;
	
	private int context = 0;
	private int lastValueChoice = 0;
	private int[] choiceNeighborQuene;
	private int choiceNeighbor = 0;
	
	public Pds_DsaSdpAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		pA = Settings.settings.getSelectProbabilityA();
		pB = Settings.settings.getSelectProbabilityB();
		pC = Settings.settings.getSelectProbabilityC();
		pD = Settings.settings.getSelectProbabilityD();
		r = Settings.settings.getSelectRound();
//		stayDsaCountInterval = Settings.settings.getSelectInterval();
		
		localCost = 2147483647;
		valueIndex = (int)(Math.random()*(domain.length));
		neighboursQuantity = neighbours.length;
		neighboursValueIndex = new int[neighboursQuantity];
		neighboursValueIndexEx = new int[neighboursQuantity];
		
		choiceNeighborQuene = new int[neighboursQuantity];
		mySuggestValue = new int[domain.length];
		myNeighboursSuggestTable = new int[domain.length][neighboursQuantity];
		abandonSuggestGain = new int[3];
		buildMyTable();
//		neighbourThreshold = new int[neighboursQuantity];
//		valueIndex = mySuggestValue[0];
//		sendDensityMessages(1, id, 0);
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
//		valueBox[1] = (int)(myThreshold*1000);
//		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
//			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueBox);
//			this.sendMessage(msg);
//		}
		
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	private void sendSuggestMessages(int neighbourIndex) {
		Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_SUGGEST_MESSAGE, 
				abandonSuggestGain);
		this.sendMessage(msg);
	}

//	private void sendResetMessages(){
//		for(int i = 0; i < children.length; i++){
//			Message msg = new Message(this.id, children[i], TYPE_RESET_MESSAGE, prepareToReset - 1);
//			this.sendMessage(msg);
//		}
//	}
	
	protected void disposeMessage(Message msg) {
		if(msg.getType() == TYPE_VALUE_MESSAGE){
			disposeValueMessage(msg);
		}
		else if(msg.getType() == TYPE_SUGGEST_MESSAGE){
			disposeSuggestMessage(msg);
		}
//		else if(msg.getType() == TYPE_RESET_MESSAGE){
//			disposeAlsResetMessage(msg);
//		}
		else if(msg.getType() == TYPE_ALSCOST_MESSAGE){
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == TYPE_ALSBEST_MESSAGE){
			disposeAlsBestMessage(msg);
		}
		else if(msg.getType() == TYPE_DENSITY_MESSAGE){
			dispooseDensityMessage(msg);
		}
		else
			System.out.println("wrong!!!!!!!!");
	}
	
	private void disposeValueMessage(Message msg){
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndexEx[senderIndex] = neighboursValueIndex[senderIndex];
		neighboursValueIndex[senderIndex] = ((int)(msg.getValue()));
//		neighboursValueIndex[senderIndex] = ((int[])(msg.getValue()))[0];
//		neighbourThreshold[senderIndex] = ((int[])(msg.getValue()))[1];
		
//		isNeighborsChanged = true;
		if(receivedQuantity==0){
			
		}
	}
	
	protected void allMessageDisposed(){
		if(cycleCount < cycleCountEnd){
//			mp[cycleCount] = ((int)(myPercentage*1000))/1000.0;
//			prepareToReset--;
			localCost=localCost();
			AlsWork();
//			if(newCycle == true){
				cycleCount++;
//				newCycle = false;
//			}
		
//			if(prepareToReset > 0){
				
//				int sum = (int)(myThreshold*1000); 
//				for(int i = 0; i < neighboursQuantity; i++){
//					sum+=neighbourThreshold[i];
//				}
//				myThreshold = sum/((neighboursQuantity+1)*1000.0);
			if(cycleCount % r != 0){	
				if(wait == 0 && suggestTag == 0)
					DsaWork();
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
//					myPercentage = ((double)(localCostTemp-myMinCost))/((double)(myMaxCost-myMinCost));
//					if(localCostTemp < localCost || myPercentage < utilityPercentage || compareTemp > 0){
					if(localCostTemp < localCost || compareTemp > 0){
						valueIndex = suggestValue;
						sendValueMessages();
						//System.out.println("accept!!!!!!!!");
					}
					else{
						//System.out.println("reject!!!!!!!!");
						abandonChain();
					}
				}
				else
					wait = 0;
//				sendValueMessages();
				
//			}
//			else{
//				prepareToReset = 2147483647;
//				resetLock = false;
//				newCycle = true;
//				valueIndex = (int)(Math.random()*(domain.length));
//				sendValueMessages();
//			}
			}
			else{
				valueIndex = (int) (Math.random() * domain.length);
				sendValueMessages();
			}
		}
		else
			AlsStopRunning();
	}
	
	private void DsaWork(){
		int done = 0;
		int[] selectMinCost=new int[domain.length];
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
			done = 1;
			double p = pA + Math.min(pB, pA + (localCost - selectOneMinCost)/(localCost+0.01));
			if(Math.random() < p){
				//System.out.println("p~~~"+p);
				valueIndex = selectValueIndex;
				sendValueMessages();
			}
		}
		else{
			double q = 0;
			if((selectOneMinCost - localCost)/(localCost+0.01) <= 1){
				q = Math.max(pC, pD - (selectOneMinCost - localCost)/(localCost+0.01));
				if(Math.random() < q){
					//System.out.println("q~~~"+q);
					valueIndex = selectValueIndex;
					sendValueMessages();
				}
			}
		}
		
		/** */
		checkMyPercentage();
		if(done == 0 ){							//	&& isNeighborsChanged == false
			abandon(localCost);
		}
//		isNeighborsChanged = false;
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
//			return Math.exp(-(cycleCount)/(Math.pow(neighboursQuantity, 2.5)));
//		}
//		else{
//			myThreshold = myThreshold*(0.99);
//			return Math.exp(-(cycleCount)/(Math.pow(neighboursQuantity, 1.5)));
//		}
		
//		if(cycleCount < 0.25*cycleCountEnd)
//			return Math.sqrt(myPercentage)*(cycleCountEnd-cycleCount)/cycleCountEnd;
//		else if(cycleCount < 0.5*cycleCountEnd)
//			return myPercentage;
//		else
//			return myPercentage*(cycleCountEnd-cycleCount)/cycleCountEnd;
		
//		if(selectP == higherP)
//			return Math.sqrt(myPercentage)*(cycleCountEnd-cycleCount)/cycleCountEnd;
//		else
//			return myPercentage*(cycleCountEnd-cycleCount)/cycleCountEnd;
		
//		if(cycleCount < (0.8)*cycleCountEnd){
//			if(selectP == higherP)
//				return Math.sqrt(myPercentage)*(1-Math.sqrt(cycleCount/(0.8*cycleCountEnd)));
//			else
//				return myPercentage*(1-Math.sqrt(cycleCount/(0.8*cycleCountEnd)));
//		}
//		else
//			return 0;
		
//		if(selectP == higherP)
//			return Math.sqrt(myPercentage)*(1-Math.sqrt(cycleCount/(double)cycleCountEnd));
//		else
//			return myPercentage*(1-Math.sqrt(cycleCount/(double)cycleCountEnd));
		
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
		
		/**
		 * 1。选择最优的进行忽略
		 */
//		int abandonIndex = 0;
//		for(int i = 1; i < neighboursQuantity; i++){
//			if(abandonCost[i] < abandonCost[abandonIndex])
//				abandonIndex = i;										//找到差值最小的邻居，选作舍弃
//		}
//		
//		if(abandonCost[abandonIndex] < nature){
//			valueIndex = abandonValueIndex[abandonIndex];
////			sendValueMessages();
//			
//			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
//			abandonSuggestGain[1] = nature - abandonCost[abandonIndex];
//			abandonSuggestGain[2] = valueIndex;
//			sendSuggestMessages(abandonIndex);
//			wait = 1;
//			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
//		}
		
		/**
		 * 2.次优决策，避免重复
		 */
//		int abandonIndex = 0;
//		for(int i = 1; i < neighboursQuantity; i++){
//			if(abandonCost[i] < abandonCost[abandonIndex])
//				abandonIndex = i;										//找到差值最小的邻居，选作舍弃
//		}
//		
//		int[] choiceNeighborQueneTag = new int[neighboursQuantity];
//		int tag = 0;
//		int[] temp = abandonCost.clone();
//		for(int i = 0; i < neighboursQuantity; i++){
//			int min = 0;
//			for(int j = 0; j < neighboursQuantity; j++){
//				if(temp[j] < temp[min])
//					min = j;
//			}
//			temp[min] = 2147483647;
//			choiceNeighborQueneTag[tag] = min;
//			tag++;
//		}
//		//上次的选择是本次的最优，并且两次的choiceNeighbor都相同，就判定两次决策序列相同
//		if(abandonValueIndex[abandonIndex] == lastValueChoice && choiceNeighborQueneTag[choiceNeighbor] == choiceNeighborQuene[choiceNeighbor]){
//			choiceNeighbor=(choiceNeighbor+1)%neighboursQuantity;
//			abandonIndex = choiceNeighborQuene[choiceNeighbor];
//		}
//		else{
//			lastValueChoice = abandonValueIndex[abandonIndex];
//			choiceNeighborQuene = choiceNeighborQueneTag.clone();
//			choiceNeighbor = 0;
//		}
//			
//		if(abandonCost[abandonIndex] < nature){
//			valueIndex = abandonValueIndex[abandonIndex];
////			sendValueMessages();
//			
//			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
//			abandonSuggestGain[1] = nature - abandonCost[abandonIndex];
//			abandonSuggestGain[2] = valueIndex;
//			sendSuggestMessages(abandonIndex);
//			wait = 1;
//			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
//		}
		
		/**
		 * 3.随机选择一个进行忽略
		 */
		int abandonIndex = h;
		
		if(abandonCost[abandonIndex] < nature){
			valueIndex = abandonValueIndex[abandonIndex];
//			sendValueMessages();
			
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex][valueIndex];
			abandonSuggestGain[1] = nature - abandonCost[abandonIndex];
			abandonSuggestGain[2] = valueIndex;
			sendSuggestMessages(abandonIndex);
			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
		
		/**
		 * 4.多重忽略，选择单个value对应最多忽略对象的进行决策
		 */
//		int[] sumSame = new int [domain.length];
//		for(int i = 0; i < neighboursQuantity; i++){
//			if(abandonCost[i] < nature)
//				sumSame[abandonValueIndex[i]]++;
//		}
//		int max = 0;
//		int sum = sumSame[0];
//		for(int i = 0; i < domain.length; i++){
//			if(sumSame[i] > sumSame[max]){
//				max = i;
//				sum = sumSame[i];
//			}
//		}
//		int abandonIDTag = 0;
//		int[] abandonID = new int[sum];
//		for(int i = 0; i < sum; i++){
//			while(abandonValueIndex[i] != max || abandonCost[i] > nature)
//				i++;
//			abandonID[abandonIDTag] =  i;
//			abandonIDTag++;
//		}
//		if(msgMailer.getCycleCount() > 100){
//			int  i =0;
//		}
//		valueIndex = max;
////		sendValueMessages();
//		if(sum > neighboursQuantity*0.7){
//			sum = (int)(neighboursQuantity*0.2);
//		}
//		for(int i = 0; i < sum; i++){
//			abandonSuggestGain[0] = abandonNeighbourIndex[abandonID[i]][valueIndex];
//			abandonSuggestGain[1] = nature - abandonCost[abandonID[i]];
//			abandonSuggestGain[2] = valueIndex;
//			sendSuggestMessages(abandonID[i]);
//		}
//		wait = 1;
//		//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		
		/**
		 * 5.随机忽略，多重
		 */
		
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
		}
	}
	
	private void abandonChain(){
//		if(Math.random() > abandonProbability)
//			return;
		
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
//			sendValueMessages();
			
			abandonSuggestGain[0] = abandonNeighbourIndex[abandonIndex];
			abandonSuggestGain[1] = localCost - abandonCost[abandonIndex];
			abandonSuggestGain[2] = valueIndex;
			sendSuggestMessages(abandonIndex);
			wait = 1;
			//System.out.println(nature - abandonCost[abandonIndex]+"~~~~~");
		}
		else{
			//System.out.println("no!!!!!!!!!!!!!!!!!!!!!");
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
			
			boolean source = false;
			while(source == false){
				try {
					localCostList.getFirst();
					source = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("~~~ source not found ~~~");
					source = false;
					e.printStackTrace();
					try {
						Thread.sleep(100);
					System.out.println("~~~ sleep 100 ~~~");
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
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

				/**   重置关键部分         */
//				if(resetLock == false){
//					if(accumulativeCost < bestCostTemp){
//						bestCostTemp = accumulativeCost;
//						stayUnchanged = 0;
//						System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
//					}
//					else{
//						stayUnchanged++;
//						//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
//						if(stayUnchanged >= stayDsaCountInterval){
//							bestCostTemp = 2147483647;
//							stayUnchanged = 0;
//							prepareToReset = totalHeight + 1;
//							resetLock = true;
//							sendResetMessages();
//						}
//					}
//				}
				
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
	}
	
//	private void disposeAlsResetMessage(Message msg){
//		prepareToReset = (Integer)msg.getValue();
//		sendResetMessages();
//	}
	
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
	
	public void sendDensityMessages(int Tag, int Id, int Rcv){
		int[] box = new int[3];
		box[0] = Tag;
		box[1] = Id;
		box[2] = Rcv;
		switch(Tag){
		case 1:
			for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
				Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_DENSITY_MESSAGE, box);
				this.sendMessage(msg);
			}
			return;
		case 2:
			for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
				if(Id != neighbours[neighbourIndex]){
					Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_DENSITY_MESSAGE, box);
					this.sendMessage(msg);
				}
			}
			return;
		case 3:
			Message msg=new Message(this.id, Rcv, TYPE_DENSITY_MESSAGE, box);
			this.sendMessage(msg);
			return;
		case 4:
			Message msg4=new Message(this.id, Id, TYPE_DENSITY_MESSAGE, box);
			this.sendMessage(msg4);
			return;
		}
	}
	
	public void dispooseDensityMessage(Message msg){
		int Tag = ((int[])msg.getValue())[0];
		int Id = ((int[])msg.getValue())[1];
		int Rcv = ((int[])msg.getValue())[2];
		switch(Tag){
		case 1:
			sendDensityMessages(Tag+1, Id, Rcv);
			return;
		case 2:
			for(int i = 0; i < neighboursQuantity; i++){
				if(neighbours[i] == Id)
					return;
			}
			sendDensityMessages(Tag+1, Id, msg.getIdSender());
			return;
		case 3:
			sendDensityMessages(Tag+1, Id, 0);
			return;
		case 4:
			neighborsOfNeighbors++;
			density = (int)(neighborsOfNeighbors+neighboursQuantity+1.0)/(neighboursQuantity+1);
			return;
		}
	}

	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		
		return null;
	}

	protected void messageLost(Message msg) {
		
	}
}
