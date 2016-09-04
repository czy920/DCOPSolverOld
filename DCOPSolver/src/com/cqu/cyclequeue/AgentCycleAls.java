package com.cqu.cyclequeue;

import java.util.HashMap;
import java.util.LinkedList;

import com.cqu.core.Message;

public abstract class AgentCycleAls extends AgentCycle{
	
	public final static String KEY_BESTCOST="KEY_BESTCOST";
	public final static String KEY_BESTCOSTINCYCLE="KEY_BESTCOSTINCYCLE";
	public final static int TYPE_ALSCOST_MESSAGE=345;
	public final static int TYPE_ALSBEST_MESSAGE=346;
	
	public final static String YES="YES";
	public final static String NO="NO";
	
	protected int AlsCycleCount = 0;
	protected int bestCost = 2147483647;
	protected double[] bestCostInCycle; 
	protected int bestValue = 0;
	protected int accumulativeCost = 0;
	protected String isChanged = NO; 
	protected boolean enoughReceived = false;
	protected LinkedList<Integer> localCostList = new LinkedList<Integer>();
	protected LinkedList<Integer> valueIndexList = new LinkedList<Integer>();
	protected HashMap<Integer, LinkedList<Integer>> childrenMessageList = new HashMap<Integer, LinkedList<Integer>>();
	
	protected int warning = 0;
	
	public AgentCycleAls(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		this.id = id;
		this.level=level;
		this.name = name;
		this.domain=domain;
		bestCostInCycle = new double[9999];
	}

	//!!!!!!!!!!!!!!!!!!!!该类的核心方法，子类必须调用才能使ALS框架起作用!!!!!!!!!!!!!!!!!!!!
	protected void sendAlsCostMessage(){
		Message msg=new Message(this.id, this.parent, AgentCycleAls.TYPE_ALSCOST_MESSAGE, this.accumulativeCost);
		this.sendMessage(msg);
	}
	
	//!!!!!!!!!!!!!!!!!!!!该类的核心方法，子类必须调用才能使ALS框架起作用!!!!!!!!!!!!!!!!!!!!
	protected void sendAlsBestMessage(){
		for(int i = 0; i < children.length; i++){
			Message msg=new Message(this.id, children[i], AgentCycleAls.TYPE_ALSBEST_MESSAGE, isChanged);
			this.sendMessage(msg);
		}
	}
	
	//!!!!!!!!!!!!!!!!!!!!该类的核心方法，子类必须调用才能使ALS框架起作用!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!调用位置应该位于算法获取局部COST之后，改变value之前 !!!!!!!!!!!!!!!!!!
	protected void AlsWork(){
		
		warning = 0;
		
		valueIndexList.add(valueIndex);
		if(this.isLeafAgent() == false)
			localCostList.add(localCost);
		else{
			accumulativeCost  = localCost;
			sendAlsCostMessage();
		}
		//System.out.println("Agent "+this.name+"~~~~~"+cycleCount);
		
		//if(id == 40){
		//	System.out.println("Agent "+this.id+"~~~costMessage~~~"+cycleCount);
		//}
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
	
	
	protected void disposeAlsBestMessage(Message msg){
		if((String)msg.getValue() == YES){
			bestValue = valueIndexList.remove();
			isChanged = YES;
		}
		else{
			valueIndexList.remove();
			isChanged = NO;
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
}
