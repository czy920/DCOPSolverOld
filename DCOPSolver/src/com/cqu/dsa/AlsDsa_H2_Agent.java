package com.cqu.dsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.core.ResultCycleAls;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.cyclequeue.AgentCycleAls;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

//Anytime论文的第二个启发式优化，Anytime框架下的DSA_B算法自适应选择概率的优化
public class AlsDsa_H2_Agent extends AgentCycleAls {

	public final static String KEY_NCCC="KEY_NCCC";
	public final static int TYPE_VALUE_MESSAGE=0;
	
	private static int cycleCountEnd;
	private static int r;
	private static double pA;
	private static double pB;
	private static double pC;
	private static double pD;
	
	private int nccc = 0;
	private int localMinCost=0;
	private int receivedQuantity=0;
	private int cycleCount=0;
	private int neighboursQuantity=0;
	private HashMap<Integer, Integer> neighboursValueIndex;			//<neighbour 的 Index, neighbourValue 的  Index>
	
	
	public AlsDsa_H2_Agent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO 自动生成的构造函数存根
	}
	
	
	protected void initRun() {
		super.initRun();
		
		cycleCountEnd = Settings.settings.getCycleCountEnd();
		pA = Settings.settings.getSelectProbabilityA();
		pB = Settings.settings.getSelectProbabilityB();
		pC = Settings.settings.getSelectProbabilityC();
		pD = Settings.settings.getSelectProbabilityD();
		r = Settings.settings.getSelectRound();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		neighboursValueIndex=new HashMap<Integer, Integer>();
		neighboursQuantity=neighbours.length;
		for(int i=0; i<neighbours.length; i++)
			neighboursValueIndex.put((Integer)i, (Integer)0);
		localMinCost();
		sendValueMessages();
	}
	

	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], AlsDsa_H2_Agent.TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO 自动生成的方法存根
		if(msg.getType() == AlsDsa_H1_Agent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}
		else if(msg.getType() == AlsDsa_H1_Agent.TYPE_ALSCOST_MESSAGE)
		{
			disposeAlsCostMessage(msg);
		}
		else if(msg.getType() == AlsDsa_H1_Agent.TYPE_ALSBEST_MESSAGE)
		{
			disposeAlsBestMessage(msg);
		}else
			System.out.println("wrong!!!!!!!!");
	}
	
	
	protected void disposeValueMessage(Message msg) {
		// TODO 自动生成的方法存根
		// TODO Auto-generated method stub
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
		neighboursValueIndex.put((Integer)senderIndex, (Integer)msg.getValue());
		
		if(receivedQuantity==0){
			localCost=localCost();
			
			if(cycleCount<=cycleCountEnd){
				
				AlsWork();
				
				if(cycleCount % r != 0){
					int[] selectMinCost=new int[domain.length];
					for(int i=0; i<domain.length; i++){
						selectMinCost[i]=0;
					}
					for(int i=0; i<domain.length; i++){
						for(int j=0; j<neighbours.length; j++){
								selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex.get(j)];
						}					
					}
					int selectValueIndex = 0;
					int selectOneMinCost = selectMinCost[0];
					for(int i = 1; i < domain.length; i++){
						if(selectOneMinCost >= selectMinCost[i] && selectMinCost[i] != valueIndex){
							selectOneMinCost = selectMinCost[i];
							selectValueIndex = i;
						}
					}
					
					if(selectOneMinCost < localCost){
						double p = pA + Math.min(pB, pA + (localCost - selectOneMinCost)/(localCost+0.01));
						if(Math.random() < p)
							//System.out.println("p~~~"+p);
							valueIndex = selectValueIndex;
					}
					else{
						double q = 0;
						if((selectOneMinCost - localCost)/(localCost+0.01) <= 1){
							q = Math.max(pC, pD - (selectOneMinCost - localCost)/(localCost+0.01));
							if(Math.random() < q)
								//System.out.println("q~~~"+q);
								valueIndex = selectValueIndex;
						}
					}
					nccc++;
				}
				else
					valueIndex = (int) (Math.random() * domain.length);
				sendValueMessages();
			}
		}
	}
	

	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighbours.length; i++){
			localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex.get(i)];		
		}
		return localCostTemp;
	}
	
	
	private void localMinCost(){
		localMinCost=localCost;
		for(int i=0; i<domain.length; i++){
			int tempLocalCost=0;
			for(int j=0; j<neighboursQuantity; j++){
				
				int oneMinCost;
				oneMinCost=constraintCosts.get(neighbours[j])[i][0];
				
				for(int k=1; k<neighbourDomains.get(neighbours[j]).length; k++){	
					if(oneMinCost>constraintCosts.get(neighbours[j])[i][k])
						oneMinCost=constraintCosts.get(neighbours[j])[i][k];
				}
				tempLocalCost+=oneMinCost;
			}
			if(tempLocalCost<localMinCost)
				localMinCost=tempLocalCost;	
		}
	}
	

	protected void runFinished(){
		super.runFinished();
		
		HashMap<String, Object> result=new HashMap<String, Object>();
		result.put(KEY_ID, this.id);
		result.put(KEY_NAME, this.name);
		result.put(KEY_VALUE, this.domain[valueIndex]);
		result.put(KEY_NCCC, this.nccc);
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
		int totalCost=0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results){
			
			//int id_=(Integer)result.get(KEY_ID);
			//String name_=(String)result.get(KEY_NAME);
			//int value_=(Integer)result.get(KEY_VALUE);
			
			if(ncccTemp < (Integer)result.get(KEY_NCCC))
				ncccTemp = (Integer)result.get(KEY_NCCC);
			if(tag == 0){
				ret.bestCostInCycle=(double[])result.get(KEY_BESTCOSTINCYCLE);
				totalCost = ((Integer)result.get(KEY_BESTCOST));
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
	
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO 自动生成的方法存根
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+AlsDsa_H2_Agent.messageContent(msg);
	}
	

	public static String messageContent(Message msg){
		switch (msg.getType()) {
		case AlsDsa_H2_Agent.TYPE_VALUE_MESSAGE:
		{
			int val=(Integer) msg.getValue();
			int valueIndex=val;
			return "value["+valueIndex+"]";
		}
		default:
			return "unknown";
		}
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
