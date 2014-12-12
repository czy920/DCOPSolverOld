package com.cqu.bnbadopt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.MessageNCCC;
import com.cqu.core.ResultAdopt;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.main.Debugger;
import com.cqu.settings.Settings;

public class BnBAdoptAgent extends AgentCycle {
	
	public final static int TYPE_VALUE_MESSAGE=0;
	public final static int TYPE_COST_MESSAGE=1;
	public final static int TYPE_TERMINATE_MESSAGE=Message.TYPE_TERMINATE_MESSAGE;
	
	public final static String KEY_CONTEXT="KEY_CONTEXT";
	public final static String KEY_LB="KEY_LB";
	public final static String KEY_UB="KEY_UB";
	public final static String KEY_TH="KEY_TH";
	public final static String KEY_NCCC="KEY_NCCC";
	
	public final static String KEY_VALUE_MESSAGE="KEY_VALUE_MESSAGE";
	
	private Map<Integer, int[]> lbs;
    private Map<Integer, int[]> ubs;
	private int LB;
	private int UB;
	private int TH;
	
	private Map<Integer, Context[]> contexts;
	private Context currentContext;
	private boolean terminateReceivedFromParent=false;
	
	private int valueID;
	private boolean Readytermintate=false;
	
	private int nccc;

	public BnBAdoptAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		this.nccc = 0;
	}

	@Override
	protected void initRun() {
		// TODO Auto-generated method stub
		super.initRun();
		
		valueID=0;
		currentContext=new Context();
		if(!this.isRootAgent())
			currentContext.addOrUpdate(this.parent, 0, 1); //仅仅初始化为第1个取值
		if(this.pseudoParents!=null)
		{
			for(int pseudoP:this.pseudoParents){
			    currentContext.addOrUpdate(pseudoP, 0, 1); //仅仅初始化为第1个取值
			}
		}
		lbs=new HashMap<Integer, int[]>();
		ubs=new HashMap<Integer, int[]>();
		contexts=new HashMap<Integer, Context[]>();
		InitChild();
		InitSelf();		
		backtrack();
	}
	
	private void InitChild(){
		if(this.isLeafAgent()==false)
		{
			int childId=0;
			for(int i=0;i<this.children.length;i++)
			{
				childId=this.children[i];
				int[] childLbs=new int[this.domain.length];
				int[] childUbs=new int[this.domain.length];
				Context[] childContexts=new Context[this.domain.length];
				for(int j=0;j<this.domain.length;j++)
				{
					childLbs[j]=0;
					childUbs[j]=Infinity.INFINITY;
					childContexts[j]=new Context();
				}
				lbs.put(childId, childLbs);
				ubs.put(childId, childUbs);
				contexts.put(childId, childContexts);
			}
		}
	}
	 
	private void InitChild(int child,int d)
	{
		if(this.isLeafAgent()==false)
		{
				lbs.get(child)[d] = 0;
				ubs.get(child)[d] = Infinity.INFINITY;
				contexts.get(child)[d].reset();
		}
	}
	
	
	
	void InitSelf(){
		
		TH=Infinity.INFINITY;
		int oldvalueIndex=this.valueIndex;
		valueIndex=this.computeMinimalLBAndUB()[0];
		if(oldvalueIndex!=this.valueIndex||this.valueID==0)
		valueID = valueID + 1;
		Debugger.valueChanges.get(this.name).add(valueIndex);
		
	}
	
	private void maintainTHInvariant()
	{
		if(this.TH<this.LB)
		{
			this.TH=this.LB;
		}
		if(this.TH>this.UB)
		{
			this.TH=this.UB;
		}
	}
		

	private void backtrack() {

		int[] compute = computeMinimalLBAndUB();
		
		//do nccc local here
		this.increaseNcccLocal();
				
		int oldValueIndex = valueIndex;
		maintainTHInvariant();
		if(this.Readytermintate==true&&this.TH==this.UB){
			valueIndex=compute[2];
			if(valueIndex!=oldValueIndex){
				valueID=valueID+1;
				Debugger.valueChanges.get(this.name).add(valueIndex);
			}
		} else {
			if (compute[1] >= this.TH) {
				valueIndex = compute[0];
				if (valueIndex != oldValueIndex) {
					valueID = valueID + 1;
					Debugger.valueChanges.get(this.name).add(valueIndex);
				}
			}
		}
		//System.out.println("agent"+this.id+": "+this.valueIndex+"\t"+this.valueID+"\t"+this.TH+"\t"+this.LB+"\t"+this.UB);
		if(((isRootAgent()==true)&&(UB<=LB))||this.Readytermintate==true&&this.TH==this.UB)
			{
				sendTerminateMessages();
				this.stopRunning();
			}
		sendValueMessages();
		sendCostMessage();
		
	}
	
	public boolean NoPseudoChild(){
		return this.pseudoChildren==null;
	}
	
	private void sendValueMessages()
	{
		if (this.isLeafAgent() == true && this.NoPseudoChild() == true) {
			return;
		}
		if(this.isLeafAgent() ==false)
		{
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			int[] val = new int[3];  //一个取值，一个ID，一个TH
			val[0]=valueIndex;
			val[1]=valueID;
			childId=this.children[i];
			if(this.Readytermintate==true && this.UB==this.TH)
				val[2]=computeTH2(valueIndex,childId);
			else 
				val[2]=computeTH(valueIndex,childId);
			Message msg=new Message(this.id, childId, BnBAdoptAgent.TYPE_VALUE_MESSAGE, val);
			this.sendMessage(this.constructNcccMessage(msg));
		}
		}
		if(this.NoPseudoChild()==false)
		{
		int pseudoChildId=0;
		for(int i=0;i<this.pseudoChildren.length;i++)
		{
			pseudoChildId=this.pseudoChildren[i];
			int[] val = new int[3];  //一个取值，一个ID，一个TH
			val[0]=valueIndex;
			val[1]=valueID;
			val[2]=Infinity.INFINITY;
			Message msg=new Message(this.id, pseudoChildId, BnBAdoptAgent.TYPE_VALUE_MESSAGE, val);
			this.sendMessage(this.constructNcccMessage(msg));
			}
		}
	}
	 	
	private void sendCostMessage()
	{
		if(this.isRootAgent()==true)
		{
			return;
		}
		
		Map<String, Object> cost=new HashMap<String, Object>();
		Context context=new Context(currentContext);
		context.Remove(this.id);
		cost.put(BnBAdoptAgent.KEY_CONTEXT, context);
		cost.put(BnBAdoptAgent.KEY_LB, LB);
		cost.put(BnBAdoptAgent.KEY_UB, UB);
		
		Message msg=new Message(this.id, this.parent, BnBAdoptAgent.TYPE_COST_MESSAGE, cost);
		this.sendMessage(this.constructNcccMessage(msg));
	}
	
	private void sendTerminateMessages()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
	    this.terminateReceivedFromParent=true;
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];		
			Map<String, Object> mapValue=new HashMap<String, Object>();
			//Context c=new Context(currentContext);
			//mapValue.put(KEY_CONTEXT, c);
			
			Message msg=new Message(this.id, childId, BnBAdoptAgent.TYPE_TERMINATE_MESSAGE, mapValue);
			this.sendMessage(this.constructNcccMessage(msg));
		}			
	}
	
	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message got in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg)+" | VALUE="+this.domain[valueIndex]+" LB="+this.LB+" UB="+Infinity.infinityEasy(this.UB)+" TH="+Infinity.infinityEasy(this.TH));
		}
		
		//do nccc message here
		this.increaseNcccFromMessage((MessageNCCC)msg);
	
		if(msg.getType()==BnBAdoptAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==BnBAdoptAgent.TYPE_COST_MESSAGE)
		{
			disposeCostMessage(msg);
		}else if(msg.getType()==BnBAdoptAgent.TYPE_TERMINATE_MESSAGE)
		{
			disposeTerminateMessage(msg);
		}
	}
	
	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		if(Debugger.debugOn==true)
		{
			System.out.println(Thread.currentThread().getName()+": message lost in agent "+
					this.name+" "+this.msgMailer.easyMessageContent(msg));
		}
	}

	private void disposeValueMessage(Message msg)
	{
		if(this.terminateReceivedFromParent==false)
		{
			Context temp = new Context(currentContext);
			int[] val =(int[]) msg.getValue();
			currentContext.addOrUpdate(msg.getIdSender(), val[0], val[1]);
			
			if(!checkCompatible(currentContext,temp))
			{
				checkCompatible();
				InitSelf();
			}
			if(msg.getIdSender() == this.parent)
			{
				TH=val[2];
			}
			if(this.msgQueue.isEmpty()==true)backtrack();
		}
	}
	
	private boolean checkCompatible(Context c1,Context c2){
	return c1.compatible(c2);	
	}
	
	private void checkCompatible()
	{
		if(this.isLeafAgent()==true)
		{
			return;
		}
		
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			for(int j=0;j<this.domain.length;j++)
			{
				if(currentContext.compatible(contexts.get(childId)[j])==false)
				{
					InitChild(childId,j);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void disposeCostMessage(Message msg) {
		Map<String, Object> cost = (Map<String, Object>) msg.getValue();
		Context c = (Context) cost.get(BnBAdoptAgent.KEY_CONTEXT);
		int myValueIndex = c.get(this.id);

		if (myValueIndex == -1) {
			return;
		}
		Context temp = new Context(currentContext);
		merge(c);
		currentContext.Remove(this.id);   //因为合并时将自己的取值加入，应该移除

		if (!checkCompatible(currentContext, temp)) {  //不兼容表示引入了新的内容
			checkCompatible();

		}
		if (checkCompatible(c, currentContext)) {   //兼容表示这个信息可以利用
			if (lbs.get(msg.getIdSender())[myValueIndex] < (Integer) cost
					.get(BnBAdoptAgent.KEY_LB))
				lbs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(BnBAdoptAgent.KEY_LB);
			if (ubs.get(msg.getIdSender())[myValueIndex] > (Integer) cost
					.get(BnBAdoptAgent.KEY_UB))
				ubs.get(msg.getIdSender())[myValueIndex] = (Integer) cost
						.get(BnBAdoptAgent.KEY_UB);
			contexts.get(msg.getIdSender())[myValueIndex] = c;
		}

		if (!checkCompatible(currentContext, temp)) {
			InitSelf();

		}
		if(this.msgQueue.isEmpty()==true)backtrack();
	}
	
	private void merge(Context c)
	{
		currentContext.union(c);  //这个合并会导致currentContext里面有自己的取值
	}
		
	//仅仅作一个记录，准备去停止，但并不是要停止。
	private void disposeTerminateMessage(Message msg) {
		//Message valueMsg = null;
		this.Readytermintate = true;
		//Map<String, Object> mapValue = (Map<String, Object>) msg.getValue();
		//currentContext = (Context) mapValue.get(KEY_CONTEXT);  //不应该加入里，而要自己去处理判断。
		//valueMsg = (Message) mapValue.get(KEY_VALUE_MESSAGE);
		//disposeMessage(this.constructNcccMessage(valueMsg));
		//this.terminateReceivedFromParent = true;
		if(this.msgQueue.isEmpty())backtrack();

	}
	
	//return int[]{dMinimizesLB, LB(curValue), dMinimizesUB}
	private int[] computeMinimalLBAndUB()
	{
		int[] localCosts_=this.localCosts();
		int minLB=Infinity.INFINITY;
		int minUB=Infinity.INFINITY;
		int dMinimizesLB=0;
		int LB_CurValue=0;
		int dMinimizesUB=0;
		
		if(this.isLeafAgent()==true)
		{
			for(int i=0;i<this.domain.length;i++)
			{
				if(i==valueIndex)
				{
					LB_CurValue=localCosts_[i];
				}
				if(localCosts_[i]<minLB)
				{
					minLB=localCosts_[i];
					dMinimizesLB=i;
				}
				if(localCosts_[i]<minUB)
				{
					minUB=localCosts_[i];
					dMinimizesUB=i;
				}
			}
			this.LB=minLB;
			this.UB=minUB;
			
			return new int[]{dMinimizesLB, LB_CurValue, dMinimizesUB};
		}
		
		int childId=0;
		for(int i=0;i<this.domain.length;i++)
		{
			int sumlb=0;
			int sumub=0;
			for(int j=0;j<this.children.length;j++)
			{
				childId=this.children[j];
				sumlb+=this.lbs.get(childId)[i];
				sumub=Infinity.add(sumub, this.ubs.get(childId)[i]);
			}
			sumlb+=localCosts_[i];
			sumub=Infinity.add(sumub, localCosts_[i]);
			if(i==valueIndex)
			{
				LB_CurValue=sumlb;
			}
			if(sumlb<minLB)
			{
				minLB=sumlb;
				dMinimizesLB=i;
			}
			if(sumub<minUB)
			{
				minUB=sumub;
				dMinimizesUB=i;
			}
		}
		this.LB=minLB;
		this.UB=minUB;
		
		
		return new int[]{dMinimizesLB, LB_CurValue, dMinimizesUB};
	}
	
	private int computeTH(int di,int child)
	{
		int localCost_=localCost(di);
		
		if(this.isLeafAgent()==true)
		{
			return localCost_;
		}
		
		int TH_di=0;
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			if(childId!=child)TH_di=TH_di+this.lbs.get(childId)[di];
		}
		TH_di=Infinity.add(TH_di, localCost_);
		
		return (TH>UB)?(UB-TH_di):(TH-TH_di);
	}
	
	private int computeTH2(int di,int child)
	{
		int localCost_=localCost(di);
		
		if(this.isLeafAgent()==true)
		{
			return localCost_;
		}
		
		int TH_di=0;
		int childId=0;
		for(int i=0;i<this.children.length;i++)
		{
			childId=this.children[i];
			if(childId!=child)TH_di=TH_di+this.ubs.get(childId)[di];
		}
		TH_di=Infinity.add(TH_di, localCost_);
		
		return (TH-TH_di);
	}
	
	
	
	private int[] localCosts()
	{
		int[] ret=new int[this.domain.length];
		
		if(this.isRootAgent()==true)
		{
			for(int i=0;i<this.domain.length;i++)
			{
				ret[i]=0;
			}
			return ret;
		}
		
		int parentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.allParents.length;i++)
		{
			parentId=this.allParents[i];
			for(int j=0;j<this.domain.length;j++)
			{
				oppositeAgentValueIndex=currentContext.get(parentId);
				if(oppositeAgentValueIndex==-1)
				{
					ret[j]+=0;
				}else
				{
					//保证id小的为行，id大的为列
					if(this.id<parentId)
					{
						ret[j]+=this.constraintCosts.get(parentId)[j][oppositeAgentValueIndex];
					}else
					{
						ret[j]+=this.constraintCosts.get(parentId)[oppositeAgentValueIndex][j];
					}
				}
			}
		}
		return ret;
	}
	
	private int localCost(int di)
	{
		int ret=0;
		
		if(this.isRootAgent()==true)
		{
			return ret;
		}
		
		int parentId=0;
		int oppositeAgentValueIndex=0;
		for(int i=0;i<this.allParents.length;i++)
		{
			parentId=this.allParents[i];
			
			oppositeAgentValueIndex=currentContext.get(parentId);
			if(oppositeAgentValueIndex==-1)
			{
				ret+=0;
			}else
			{
				//保证id小的为行，id大的为列
				if(this.id<parentId)
				{
					ret+=this.constraintCosts.get(parentId)[di][oppositeAgentValueIndex];
				}else
				{
					ret+=this.constraintCosts.get(parentId)[oppositeAgentValueIndex][di];
				}
			}
		}
		return ret;
	}
	
	/**
	 * 关于nccc的计算，可参考adopt里面的说明
	 */
	private void increaseNcccLocal()
	{
		this.nccc++;
	}
	
	private void increaseNcccFromMessage(MessageNCCC mn)
	{
		int t=Settings.settings.getCommunicationNCCCInAdopts();
		this.nccc=Math.max(mn.getNccc()+t, this.nccc);
	}
	
	private Message constructNcccMessage(Message msg)
	{
		return new MessageNCCC(msg, this.nccc);
	}
	
	@Override
	protected void runFinished() {
		// TODO Auto-generated method stub
		super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(BnBAdoptAgent.KEY_ID, this.id);
		result.put(BnBAdoptAgent.KEY_NAME, this.name);
		result.put(BnBAdoptAgent.KEY_VALUE, this.domain[valueIndex]);
		result.put(BnBAdoptAgent.KEY_LB, this.LB);
		result.put(BnBAdoptAgent.KEY_UB, this.UB);
		result.put(BnBAdoptAgent.KEY_TH, this.TH);
		result.put(BnBAdoptAgent.KEY_NCCC, this.nccc);
		
		this.msgMailer.setResult(result);
		
		System.out.println("Agent "+this.name+" stopped!");
	}

	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		int totalCost=-1;
		int maxNccc=0;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(BnBAdoptAgent.KEY_ID);
			String name_=(String) result.get(BnBAdoptAgent.KEY_NAME);
			int value_=(Integer) result.get(BnBAdoptAgent.KEY_VALUE);
			int LB_=(Integer) result.get(BnBAdoptAgent.KEY_LB);
			int UB_=(Integer) result.get(BnBAdoptAgent.KEY_UB);
			int TH_=(Integer) result.get(BnBAdoptAgent.KEY_TH);
			int ncccTemp=(Integer) result.get(BnBAdoptAgent.KEY_NCCC);
			if(maxNccc<ncccTemp)
			{
				maxNccc=ncccTemp;
			}
			if(totalCost==-1)
			{
				totalCost=UB_;
			}
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_+" LB="+LB_+" UB=";
			displayStr+=Infinity.infinityEasy(UB_);
			displayStr+=" TH="+Infinity.infinityEasy(TH_);
			System.out.println(displayStr);
		}
		System.out.println("totalCost: "+Infinity.infinityEasy(totalCost)+" NCCC: "+maxNccc);
		
		ResultAdopt ret=new ResultAdopt();
		ret.totalCost=totalCost;
		ret.nccc=maxNccc;
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+BnBAdoptAgent.messageContent(msg);
	}
	
	
	@SuppressWarnings("unchecked")
	public static String messageContent(Message msg)
	{
		switch (msg.getType()) {
		case BnBAdoptAgent.TYPE_VALUE_MESSAGE:
		{
			int[] val=(int[]) msg.getValue();
			int valueIndex=(Integer) val[0];
			return "value["+valueIndex+"]";
		}
		case BnBAdoptAgent.TYPE_COST_MESSAGE:
		{
			Map<String, Object> msgValue=(Map<String, Object>) msg.getValue();
			int LB_=(Integer) msgValue.get(KEY_LB);
			int UB_=(Integer) msgValue.get(KEY_UB);
			Context c=(Context) msgValue.get(KEY_CONTEXT);
			return "cost[LB="+LB_+" UB="+Infinity.infinityEasy(UB_)+" context="+c.toString()+"]";
		}
		case BnBAdoptAgent.TYPE_TERMINATE_MESSAGE:
		{
			return "terminate[]";
		}
		default:
			return "unknown";
		}
	}
	
}

