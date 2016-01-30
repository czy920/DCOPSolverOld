package com.cqu.aco;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cqu.bnbadopt.BnBAdoptAgent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultAdopt;
import com.cqu.cyclequeue.AgentCycle;

/**
 * ant solver for dcop 
 * the power of ants in solving distributed constraint satisfaction problems
 * @author hechen
 *
 */

public class AcoAgent extends AgentCycle{
	
	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_VALUE_PHEROMONE = 1;
	
	//保存每只在蚂蚁的上下文以及自己对应的取值
	private HashMap<Integer, Context> currentContext = new HashMap<Integer, Context>(PublicConstants.countAnt);
	private HashMap<Integer, Integer> selfView = new HashMap<Integer, Integer>(PublicConstants.countAnt);
	private HashMap<Integer, Integer> currentCosts = new HashMap<Integer, Integer>(PublicConstants.countAnt);
	//用于标志哪些蚂蚁已经选好值
	private HashMap<Integer, Boolean> mark = new HashMap<Integer, Boolean>(PublicConstants.countAnt);
	int bestAnt;
	double delta;
	
	//信息素
	private HashMap<Integer, Pheromone> taus = null;
	
	public AcoAgent(int id, String name, int level, int[] domain) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		initTaU();
	}
	
	private void initTaU() {
		// TODO Auto-generated method stub
		for(int i = 0; i< this.highPriorities.length; i++){
			Pheromone phero = new Pheromone(this.domain.length, this.neighbourDomains.get(this.highPriorities[i]).length);
			phero.initValue(PublicConstants.Min_tau);
			taus.put(this.highPriorities[i], phero);
		}
		
	}
	
	public double[] computeP(int ant){
		double[] ret = new double[this.domain.length];
		if(this.noHighNode() == true){
			for(int i = 0; i < ret.length; i++){
				ret[i] = PublicConstants.Min_tau;
			}
			return ret;
		}
		for(int i = 0; i<this.domain.length;i++){
			double fenzi = 0.0;
			double fenmu = 0.0;
			fenzi = this.computePPart(ant, i);
			for(int j = 0;j< this.domain.length;j++){
				fenmu += this.computePPart(ant, j);
			}
			ret[i] = fenzi/fenmu;
		}
		return ret;
	}
	
	public double computePPart(int ant, int value){
		double ret = 0.0;
		double sum = 0.0;
		int localCost;
		localCost = this.localCost(ant,value);
		for(int j =0; j< this.highPriorities.length;j++){
			int highId = this.highPriorities[j];
			int highValueIndex = this.currentContext.get(ant).getContext().get(highId);
			sum += taus.get(ant).getTau()[value][highValueIndex];
		}
		ret = Math.pow(sum, PublicConstants.alpha)*Math.pow(1/(1+localCost), PublicConstants.beta);
		return ret;
	}
	
	public int chooseValue(double[] resultP){
		List<Integer> valueIndex = new LinkedList<Integer>();
		double max =0.0;
		for(int i = 0;i< resultP.length; i++){
			if(max < resultP[i]){
				valueIndex.clear();
				valueIndex.add(i);
			}else if( max == resultP[i]){
				valueIndex.add(i);
			}
		}
		int index = (int) (1 + Math.random()*valueIndex.size());
		return valueIndex.get(index);
	}

	@Override
	protected void initRun() {
		super.initRun();
		
		PublicConstants.currentCycle = 0;
		this.startIteration();
	}

	@Override
	protected void disposeMessage(Message msg) {
		// TODO Auto-generated method stub
		if(msg.getType()==AcoAgent.TYPE_VALUE_MESSAGE)
		{
			disposeValueMessage(msg);
		}else if(msg.getType()==AcoAgent.TYPE_VALUE_PHEROMONE)
		{
			disposePheromone(msg);
		}
	}

	private void disposePheromone(Message msg) {
		PheroMsgContext obj = (PheroMsgContext) msg.getValue();
		this.bestAnt = obj.getAnt();
		this.delta = obj.getDelta();
	
		//为每条信息素边更新信息素
		this.updtePhero();
		//判断是否下一轮开始
		if(PublicConstants.currentCycle < PublicConstants.MaxCycle){
			startIteration();
		}
		else{        //停止agent
			this.stopRunning();
		}
	}

	private void disposeValueMessage(Message msg) {
		//取出蚂蚁携带的上下文
		ValueMsgContext obj = (ValueMsgContext) msg.getValue();
		//保存到自己当前的上下文中
		this.union(obj.getAnt(), obj.getCurrentCost(), obj.getContext());
		//执行检查函数
		checkView();	
	}
	
	private void union(int ant, int cost, Context context){
		this.currentCosts.put(ant, this.currentCosts.get(ant)+cost);
		this.currentContext.get(ant).merge(context);
	}
	
	private void sendValueMessages(){
		//对于每只蚂蚁，将自己的上下文传递给优先级低的邻居
		for(int i : this.currentContext.keySet()){
			for(int j =0; j < this.lowPriorities.length;j++){
				int lowId = this.lowPriorities[j];
				ValueMsgContext obj = new ValueMsgContext(i,this.currentCosts.get(i).intValue(),this.currentContext.get(i));
				Message msg=new Message(this.id, lowId, AcoAgent.TYPE_VALUE_MESSAGE, obj);
				this.sendMessage(msg);
			}
			
		}
	}
	
	private void sendPheromone(){
		//向所有agent广播信息素更新值delta
		PheroMsgContext obj = new PheroMsgContext(this.bestAnt, this.delta);
		for(int i=0 ; i < this.allNodes.length; i++){
			int otherId = this.allNodes[i];
			if(otherId != this.id){
				Message msg=new Message(this.id, otherId, AcoAgent.TYPE_VALUE_PHEROMONE, obj);
				this.sendMessage(msg);
			}
		}
	}
	
	private void checkView(){
		//对于每只蚂蚁进行判断是否收到优先级高的邻居的取值
		for(int i = 0; i < PublicConstants.countAnt; i++){
			int antId = PublicConstants.antIds[i];
			if(this.currentContext.get(antId).getContext().size() == this.highPriorities.length){
				selectValue(i);
			}
		}
		/*
		 * 是否为每只蚂蚁选取值，发送value信息；
		 * 以及优先级最低的结点计算信息素并发送信息素；
		 */
		if(this.isAllAntSelected()){
			if(this.noLowerNode() == false){
				this.sendValueMessages();
			}else if(this.isLeastNode() == false){
				this.sendValueMessage();
			}else if(this.isAllSolution()){
				int bestAnt = this.selectBestAnt();
				//更新信息素并广播更新的信息素delta
				this.delta = PublicConstants.computeDelta(this.currentCosts.get(this.bestAnt));
				this.updtePhero();
				PublicConstants.currentCycle++;
				this.sendPheromone();
			}
		}
		
	}
	
	private void updtePhero(){
		if(this.noHighNode()){
			return;
		}
		for(int i = 0; i < this.highPriorities.length; i++){
			int myValue = this.selfView.get(this.bestAnt);
			int oppId = this.highPriorities[i];
			int oppValue = this.currentContext.get(this.bestAnt).getContext().get(oppId);
			double old_tau = this.taus.get(this.bestAnt).getTau()[myValue][oppValue];
			double new_tau = PublicConstants.update_tau(old_tau, this.delta);
			this.taus.get(this.bestAnt).getTau()[myValue][oppValue] = new_tau;
		}
	}
	
	private void sendValueMessage() {
		// TODO Auto-generated method stub
		//发送的信息应该包括了代价，上下文，蚂蚁标识
		for(int i : this.currentContext.keySet()){
			ValueMsgContext obj = new ValueMsgContext(i,this.currentCosts.get(i).intValue(),this.currentContext.get(i));
			Message msg=new Message(this.id, this.minPriority, AcoAgent.TYPE_VALUE_MESSAGE, obj);
			this.sendMessage(msg);
		}
	}

	private int selectBestAnt(){
		int ret =0;
		int best = Infinity.INFINITY;
		for(int i : this.currentCosts.keySet()){
			if(best > this.currentCosts.get(i).intValue()){
				ret = i;
				best = this.currentCosts.get(i).intValue();
			}
		}
		this.bestAnt = ret;
		return ret;
	}
	
	private boolean isAllAntSelected(){
		for(int i : this.mark.keySet()){
			if(this.mark.get(i).booleanValue() == false){
				return false;
			}
		}
		return true;
	}
	
	private void selectValue(int ant){
		/*
		 * 根据概率公式计算，为蚂蚁取值，
		 * 概率相同时，有必要再根据随机概率从这几个值里面选一个
		 * 将选择的值加入selfView currentContext
		 */
		double[] resultP = this.computeP(ant);
		this.valueIndex = this.chooseValue(resultP);
		this.selfView.put(ant, this.valueIndex);
		this.currentContext.get(ant).getContext().put(this.id, this.valueIndex);	
		this.mark.put(ant, true);
		
	}
	
	private void startIteration(){
		//新的一轮，一些参数重新初始化
		for(int i = 0 ; i < PublicConstants.countAnt; i++){
			this.mark.put(i, false);
		}
		this.bestAnt = -1;
		this.delta = -1;
		this.currentContext.clear();
		this.currentCosts.clear();
		this.selfView.clear();
		
		//直接调用检查函数
		checkView();
	}
	
	private int[] localCosts(int ant)
	{
		int[] ret=new int[this.domain.length];
		
		if(this.noHighNode()==true)
		{
			for(int i=0;i<this.domain.length;i++)
			{
				ret[i]=0;
			}
			return ret;
		}
		
		int highId=0;
		int highAgentValueIndex=0;
		for(int i=0;i<this.highPriorities.length;i++)
		{
			highId=this.highPriorities[i];
			for(int j=0;j<this.domain.length;j++)
			{
				highAgentValueIndex=currentContext.get(ant).getContext().get(highId);
				if(highAgentValueIndex==-1)
				{
					ret[j]+=0;
				}else
				{
					//保证id小的为行，id大的为列
					if(this.id<highId)
					{
						//ret[j]+=this.constraintCosts.get(parentId)[this.domain[j]-1][agentDomains.get(parentId)[oppositeAgentValueIndex]-1];
						ret[j]+=this.constraintCosts.get(highId)[j][highAgentValueIndex];
					}else
					{
						//ret[j]+=this.constraintCosts.get(parentId)[agentDomains.get(parentId)[oppositeAgentValueIndex]-1][this.domain[j]-1];
						ret[j]+=this.constraintCosts.get(highId)[highAgentValueIndex][j];
					}
				}
			}
		}
		return ret;
	}
	
	private int localCost(int ant, int di)
	{
		int ret=0;
		
		if(this.noHighNode()==true)
		{
			return ret;
		}
		
		int highId=0;
		int highAgentValueIndex=0;
		for(int i=0;i<this.highPriorities.length;i++)
		{
			highId=this.highPriorities[i];
			
			highAgentValueIndex=currentContext.get(ant).getContext().get(highId);
			if(highAgentValueIndex==-1)
			{
				ret+=0;
			}else
			{
				//保证id小的为行，id大的为列
				if(this.id<highId)
				{
					//ret+=this.constraintCosts.get(parentId)[this.domain[di]-1][agentDomains.get(parentId)[oppositeAgentValueIndex]-1];
					ret+=this.constraintCosts.get(highId)[di][highAgentValueIndex];
				}else
				{
					//ret+=this.constraintCosts.get(parentId)[agentDomains.get(parentId)[oppositeAgentValueIndex]-1][this.domain[di]-1];
					ret+=this.constraintCosts.get(highId)[highAgentValueIndex][di];
				}
			}
		}
		return ret;
	}
	
	private boolean isLeastNode(){
		return this.id == this.minPriority;
	}
	
	private boolean noLowerNode(){
		return this.lowPriorities.length == 0;
	}
	
	private boolean isSolution(int ant){
		return this.currentContext.get(ant).getContext().size() == this.allNodes.length;
	}
	
	private boolean isAllSolution(){
		boolean ret = true;
		for(int i = 0; i < PublicConstants.countAnt; i++){
			ret = isSolution(i);
			if(ret == false) return ret;
		}
		return ret;
	}
	
	private boolean noHighNode(){
		return this.highPriorities.length == 0;
	}
	
	@Override
	protected void runFinished() {
       super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AgentCycle.KEY_ID, this.id);
		result.put(AgentCycle.KEY_NAME, this.name);
		result.put(AgentCycle.KEY_VALUE, this.domain[valueIndex]);
	
		this.msgMailer.setResult(result);
		System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AgentCycle.KEY_ID);
			String name_=(String) result.get(AgentCycle.KEY_NAME);
			int value_=(Integer) result.get(AgentCycle.KEY_VALUE);
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_;
			System.out.println(displayStr);
		}
		
		ResultAdopt ret=new ResultAdopt();
		return ret;
	}

	@Override
	public String easyMessageContent(Message msg, AgentCycle sender,
			AgentCycle receiver) {
		// TODO Auto-generated method stub
		return "from "+sender.getName()+" to "+receiver.getName()+" type "+BnBAdoptAgent.messageContent(msg);
	}

	@Override
	protected void messageLost(Message msg) {
		// TODO Auto-generated method stub
		
	}

}
