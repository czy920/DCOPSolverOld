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

public class AlsDsaDsaEvoAgent extends AgentCycleAls{
	
	public final static int TYPE_DSA_VALUE_MESSAGE = 0;
	public final static int TYPE_RESET_MESSAGE = 5;
	
	public final static int DSASTEP1 = 0;
	public final static int DSASTEP2 = 1;
	public final static String YESYES="YESYES";				//前一个YES或NO是全局的，后一个是对临时的
	public final static String YESNO="YESNO";				//前一个YES或NO是全局的，后一个是对临时的
	public final static String NOYES="NOYES";				//前一个YES或NO是全局的，后一个是对临时的
	public final static String NONO="NONO";					//前一个YES或NO是全局的，后一个是对临时的
	
	private static int cycleCountEnd;
	private static int stayDsaCountInterval;
	private static double pH;
	private static double pL;
	private static double interActProbability;
	
	private int receivedDsaValueQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity;	
	private int[] neighboursValueIndex;								//[neighbour 的 Index, neighbourValue 的  Index]
	//private HashMap<Integer, Integer> neighboursValueIndex;		//<neighbour 的 Index, neighbourValue 的  Index>

	private int bestCostTemp = 2147483644;
	private int STEP = DSASTEP1;
	private boolean resetLock = false;
	private boolean ResetLock1 = false;
	private boolean ResetLock2 = false;
	private int stayUnchanged = 0;
	private int prepareToReset = 2147483647;
	protected LinkedList<int[]> localCostList = new LinkedList<int[]>();
	private int dsaCycle = 0;
	private int mgmCycle = 0;
	private int candidate = 0;
	private String tempTag = null;
	private String NEWCYCLE = YES;
	private double p;
	
	public AlsDsaDsaEvoAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
	}

	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		pH = Settings.settings.getSelectProbability();
		pL = Settings.settings.getSelectNewProbability();
		stayDsaCountInterval = Settings.settings.getSelectInterval();
		interActProbability = Settings.settings.getSelectProbabilityC();
			
		localCost = 2147483647;
		valueIndex = (int)(Math.random()*(domain.length));
		neighboursQuantity = neighbours.length;
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
		receivedDsaValueQuantity=(receivedDsaValueQuantity+1)%neighboursQuantity;
		
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for(int i=0; i<neighbours.length; i++){
			if(neighbours[i]==senderId){
				senderIndex=i;
				break;
			}
		}
		neighboursValueIndex[senderIndex] = (int)((Integer)msg.getValue());
		
		if(receivedDsaValueQuantity==0){
			dsaCycle++;
			prepareToReset--;
			localCost=localCost();
			AlsWork();
			
			if(NEWCYCLE == YES){
				NEWCYCLE = NO;
				cycleCount++;
				if(cycleCount > cycleCountEnd){
					STOPRUNNING = true;
					return;
				}
				STEP = DSASTEP1;
				p = pH;
			}
			
			if(prepareToReset > 0){
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
				sendDsaValueMessages();
			}
			else{
				prepareToReset = 2147483647;
				resetLock = false;
				
//				NEWCYCLE = YES;
				if(STEP == DSASTEP1){
					STEP = DSASTEP2;
					p = pL;
				}
				else{
					NEWCYCLE = YES;
					if(cycleCount > 1){
						if(cycleCount % 2 == 0){
							if(Math.random() < interActProbability)
								valueIndex = bestValue;
							else
								valueIndex = candidate;
						}
						else{
							valueIndex = (int)(Math.random()*(domain.length));
						}
					}
					else
						valueIndex = (int)(Math.random()*(domain.length));
				}
				sendDsaValueMessages();
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
				if(resetLock == false){
					if(theSTEP == DSASTEP1 && ResetLock1 == false){
						ResetLock2 = false;
						if(accumulativeCost < bestCostTemp){
							bestCostTemp = accumulativeCost;
							tempTag = YES;
							stayUnchanged = 0;
							//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
						}
						else{
							tempTag = NO;
							stayUnchanged++;
							//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
							if(stayUnchanged >= stayDsaCountInterval){
								//System.out.println("dsaCycle   "+dsaCycle+"   !!!!!!!!");
								//dsaCycle = 0;
								bestCostTemp = 2147483644;
								stayUnchanged = 0;
								prepareToReset = totalHeight + 1;
								resetLock = true;
								ResetLock1 = true;
								sendResetMessages();
							}
						}
					}
					else if(theSTEP == DSASTEP2 && ResetLock2 == false){
						ResetLock1 = false;
						if(accumulativeCost < bestCostTemp){
							bestCostTemp = accumulativeCost;
							tempTag = YES;
							stayUnchanged = 0;
							//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
						}
						else{
							tempTag = NO;
							stayUnchanged++;
							//System.out.println("stayUnchanged   "+stayUnchanged+"   !!!!!!!!");
							if(stayUnchanged >= stayDsaCountInterval){
								//System.out.println("dsaCycle   "+dsaCycle+"   !!!!!!!!");
								//dsaCycle = 0;
								bestCostTemp = 2147483644;
								stayUnchanged = 0;
								prepareToReset = totalHeight + 1;
								resetLock = true;
								ResetLock2 = true;
								sendResetMessages();
							}
						}
					}
					
				}
				
				if(accumulativeCost < bestCost){
					bestCost = accumulativeCost;
					bestValue = valueIndexList.removeFirst();
					candidate = bestValue;
					isChanged = YESYES;
				}
				else{
					if(tempTag == YES){
						candidate = valueIndexList.removeFirst();
						isChanged = NOYES;
					}
					else{
						valueIndexList.removeFirst();
						isChanged = NONO;
					}
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
	
	protected void disposeAlsBestMessage(Message msg){
		String Temp = (String)msg.getValue();
		if(Temp == YESYES){
			bestValue = valueIndexList.remove();
			candidate = bestValue;
			isChanged = YESYES;
		}
		else if(Temp == YESNO){
			bestValue = valueIndexList.remove();
			isChanged = YESNO;
		}
		else if(Temp == NOYES){
			candidate = valueIndexList.remove();
			isChanged = NOYES;
		}
		else if(Temp == NONO){
			valueIndexList.remove();
			isChanged = NONO;
		}
		else{
			System.out.println("wrong in AlsBestMessage!!!!!!!!");
			int a = 1;
			a=a/0;
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
		
		if(valueIndexList.isEmpty() == true){
			valueIndex = bestValue;
			stopRunning();
			
			//if(id == 40 && cycleCount != 19){
			//	System.out.println("~~~"+cycleCount+"~~~wrong!!!!!!!!");
			//}
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
	
	private void disposeAlsResetMessage(Message msg){
		
		prepareToReset = (Integer)msg.getValue();
		sendResetMessages();
		
	}
	
	protected void localSearchCheck(){
		if(msgQueue.size() == 0){
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
				System.out.println("dsaCycle   "+dsaCycle+"   !!!!!!!!");
				System.out.println("mgmCycle   "+mgmCycle+"   !!!!!!!!");
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
