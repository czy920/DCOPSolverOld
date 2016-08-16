package com.cqu.dsan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

public class DsanAgent extends AgentCycle{
	
	public final static int TYPE_VALUE_MESSAGE=1;
	
	private static int cycleCountEnd;
	private static int CONST;
	
	public final static String KEY_LOCALCOST="KEY_LOCALCOST";
	public final static String KEY_NCCC="KEY_NCCC";
	
	private int nccc = 0;
	private int selectValueIndex;
	private int receivedQuantity;
	private int cycleCount;
	private int neighboursQuantity;
	private int[] neighboursValueIndex;			//<neighbour 鐨� Index, neighbourValue 鐨�  Index>
	
	public DsanAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
	}
	
	protected void initRun() {
		super.initRun();

		cycleCountEnd = Settings.settings.getCycleCountEnd();
		CONST = Settings.settings.getSelectInterval();
		
		localCost=2147483647;
		valueIndex=(int)(Math.random()*(domain.length));
		selectValueIndex=0;
		receivedQuantity=0;
		cycleCount=0;
		neighboursQuantity=neighbours.length;
		neighboursValueIndex=new int[neighboursQuantity];
		sendValueMessages();
	}
	
	private void sendValueMessages(){
		for(int neighbourIndex=0; neighbourIndex<neighboursQuantity; neighbourIndex++){
			Message msg=new Message(this.id, neighbours[neighbourIndex], TYPE_VALUE_MESSAGE, valueIndex);
			this.sendMessage(msg);
		}
	}
	
	protected void disposeMessage(Message msg){
		int senderIndex=0;
		int senderId=msg.getIdSender();
		for (int neighbourindex=0; neighbourindex<neighbours.length;neighbourindex++){
			if(senderId == neighbours[neighbourindex]){
				senderIndex = neighbourindex;
				break;
			}
		}
		neighboursValueIndex[senderIndex] = (Integer)(msg.getValue());
		
        if (receivedQuantity == 0){
        	
        }
	}
	
	protected void allMessageDisposed() {
		if (cycleCount <= cycleCountEnd){
			cycleCount++;
            localCost = localCost();
            
//            int[] selectMinCost=new int[domain.length];
//			for(int i=0; i<domain.length; i++){
//				selectMinCost[i]=0;
//			}
//			for(int i=0; i<domain.length; i++){
//				for(int j=0; j<neighbours.length; j++){
//					selectMinCost[i]+=constraintCosts.get(neighbours[j])[i][neighboursValueIndex[j]];	
//				}
//			}
//			int selectValueIndex=0;
//			int selectOneMinCost=selectMinCost[0];
//			for(int i = 1; i < domain.length; i++){
//				if(selectOneMinCost > selectMinCost[i]){
//					selectOneMinCost = selectMinCost[i];
//					selectValueIndex = i;
//				}
//			}
//			if(selectOneMinCost < localCost ){
//				if(Math.random() < 0.3){
//					valueIndex = selectValueIndex;
//					sendValueMessages();
//					return;
//				}
//			}
			
			int randomValueIndex = (int)(Math.random()*(domain.length));
			int newLocalCost = 0;
			
			for (int neighbourindex=0;neighbourindex<neighboursQuantity;neighbourindex++){
				newLocalCost+=constraintCosts.get(neighbours[neighbourindex])[randomValueIndex][neighboursValueIndex[neighbourindex]];		
			}
			
			if (newLocalCost <= localCost){
//				if(Math.random() < 0.3)
					valueIndex = randomValueIndex;
					sendValueMessages();
			}
			else if (newLocalCost > localCost){
				if(Math.random() < Math.exp(((localCost - newLocalCost)*(cycleCount*cycleCount))/CONST)){
					valueIndex = randomValueIndex;
					sendValueMessages();
				}
			}
    	}
		else{
    		stopRunning();
    	}
	}
	
	private int localCost(){
		int localCostTemp=0;
		for(int i=0; i<neighboursQuantity; i++){
//			if(this.id < neighbours[i])
				localCostTemp+=constraintCosts.get(neighbours[i])[valueIndex][neighboursValueIndex[i]];		
//			else
//				localCostTemp+=constraintCosts.get(neighbours[i])[neighboursValueIndex.get(i)][valueIndex];	
		}
		return localCostTemp;
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
		// TODO Auto-generated method stub
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
	
	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
		return " ";
	}
	
	public static String messageContent(Message msg){
		return "unknown";
	}
	
	@Override
	protected void messageLost(Message msg) {
		// TODO 鑷姩鐢熸垚鐨勬柟娉曞瓨鏍�
		
	}
}
