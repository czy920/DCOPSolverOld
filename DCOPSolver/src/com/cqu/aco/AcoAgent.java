package com.cqu.aco;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cqu.bnbadopt.BnBAdoptAgent;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

/**
 * ant solver for dcop 
 * the power of ants in solving distributed constraint satisfaction problems
 * @author hechen
 *
 */

public class AcoAgent extends AgentCycle{
	
	public final static int TYPE_VALUE_MESSAGE = 0;
	public final static int TYPE_VALUE_PHEROMONE = 1;
	public final static int TYPE_SELF_MESSAGE = 2;
	private static final String KEY_LOCALCOST = "KEY_LOCALCOST";
	
	//非流水线方式只有infos[0], 流水线方式则长度不只1
	private EachCycleInfo[] infos;
	private int cycle;   //根结点用于标记触发的轮数
	private int currentCycle; //用于每个结点当前的轮数
	
	private int bestCost = Infinity.INFINITY;   //保留最优解代价
	private Context bestSolution = null;   //仅最后一个结点知道全局解
	private int bestValueIndex = -1;       //保留最好的解对应的取值
	private String endBestAnt = null;      //保留最好解是哪一轮哪只蚂蚁
	private Context bestContext = null;    //保留最好解对应的上下文
	
	
	//信息素
	private HashMap<Integer, Pheromone> taus = new HashMap<Integer, Pheromone>();
	
	public AcoAgent(int id, String name, int level, int[] domain, long treeDepth) {
		super(id, name, level, domain);
		// TODO Auto-generated constructor stub
		PublicConstants.MaxCycle = Settings.settings.getCycleCountEnd();;
		PublicConstants.countAnt = Settings.settings.getCountAnt();
		PublicConstants.alpha = Settings.settings.getAlpha();
		PublicConstants.beta = Settings.settings.getBeta();
		PublicConstants.rho = Settings.settings.getRho();
		PublicConstants.Max_tau = Settings.settings.getMax_tau();
		PublicConstants.Min_tau = Settings.settings.getMin_tau();
		PublicConstants.aco_bestCostInCycle = new double[PublicConstants.MaxCycle];
		PublicConstants.aco_totalCostInCycle = new double[PublicConstants.MaxCycle];
		PublicConstants.betterAntCount = PublicConstants.countAnt/3;
		PublicConstants.dataLength = (int) (treeDepth + 2);
	}
	
	private void initTaU() {
		// TODO Auto-generated method stub
		if(this.noHighNode()){
			return;
		}
		for(int i = 0; i< this.highPriorities.length; i++){
			Pheromone phero = new Pheromone(this.domain.length, this.neighbourDomains.get(this.highPriorities[i]).length);
			phero.initValue(PublicConstants.Min_tau);
			taus.put(this.highPriorities[i], phero);
		}
		
	}
	
	public double[] computeP(int cycle, int ant){
		double[] ret = new double[this.domain.length];
		
		double fenzi = 0.0;
		double fenmu = 0.0;
		for(int j = 0;j< this.domain.length;j++){
			fenmu += this.computePPart(cycle, ant, j);
		}
		for(int i = 0; i<this.domain.length;i++){
			fenzi = this.computePPart(cycle, ant, i);
			ret[i] = fenzi/fenmu;
		}
		return ret;
	}
	
	public double computePPart(int cycle, int ant, int value){
		double ret = 0.0;
		double sum = 0.0;
		
		if(this.noHighNode() == true){
			sum = PublicConstants.Min_tau;
			ret = Math.pow(sum, PublicConstants.alpha);
			return ret;
		}
		
		int localCost;
		EachCycleInfo tmp = this.getInfo(cycle);
		localCost = this.localCost(cycle,ant,value);
		for(int j =0; j< this.highPriorities.length;j++){
			int highId = this.highPriorities[j];
			int highValueIndex = tmp.context.get(ant).getContext().get(highId);
			Pheromone pheromone = taus.get(highId);
			double[][] tau = pheromone.getTau();
			
			sum += tau[value][highValueIndex];
		}
		ret = Math.pow(sum, PublicConstants.alpha)*Math.pow(logCompute(localCost), PublicConstants.beta);
		//ret = Math.pow(sum, PublicConstants.alpha);
		return ret;
	}
	
	public double logCompute(int localCost){
		return 1.0/(Math.log(2+localCost)/Math.log(3.0/2.0));
		//return 1.0/(Math.log(2+localCost)/Math.log(9.0/5.0));
		//return 1.0/(Math.log(2+localCost)/Math.log(2));
		//return 1.0/Math.log(2+localCost);
		//return 1.0/Math.sqrt(localCost);
	}
	
	public double daosuCompute(int localCost){
		return 1.0/(1+localCost);
	}
	
	public double[] transferP(double[] resultP){
		double ret[] = new double[resultP.length];
		double sum = 0.0;
		for(int i = 0; i < ret.length; i++){
			sum += resultP[i];
			ret[i] = sum;
		}
		return ret;
	}
	
	public int chooseValue(double[] resultP){
		double[] tmp = this.transferP(resultP);
		int index = 0;
		double random = Math.random();
		for(int i = 0; i<tmp.length; i++){
			if(random < tmp[i]){
				index = i;
				break;
			}
		}
		return index;
	}

	@Override
	protected void initRun() {
		super.initRun();
		PublicConstants.clearFile();
		initTaU();
		this.infos = EachCycleInfo.getInstances();
		this.cycle = 0;
		this.currentCycle = 0;
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
		}else if(msg.getType()==AcoAgent.TYPE_SELF_MESSAGE)
		{
			disposeSelfMessage(msg);
		}
	}

	private void disposePheromone(Message msg) {
		PheroMsgContext obj = (PheroMsgContext) msg.getValue();
		int cycle = obj.getCycle();
		EachCycleInfo tmp = getInfo(cycle);
		tmp.bestAnt = obj.getAnt();
			
		if(this.endBestAnt == null || !this.endBestAnt.equals(obj.getEndBestAnt())){
			this.bestCost = obj.getBestCost();
			this.endBestAnt = obj.getEndBestAnt();
			this.bestValueIndex = tmp.selfView.get(tmp.bestAnt);
			this.bestContext = new Context(tmp.context.get(tmp.bestAnt));
		}
	
		//为每条信息素边更新信息素
		if((PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[3]) ||
				PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5]))	&& this.currentCycle  < PublicConstants.MaxCycle / 3){
			tmp.betterDelta = obj.betterDelta;
			this.updatePheros(cycle);
		}else{
			tmp.delta = obj.getDelta();
			this.updatePhero(cycle);
		}
		
		PublicConstants.writeTau("cycle " + cycle + "\t" +  this.name + "\n" + this.taus.toString() + "\n");
		
		//更新信息素后，数据初始化，用于保存下一轮的消息
		int tempValueIndex = tmp.selfView.get(tmp.bestAnt);
		this.infos[cycle%PublicConstants.dataLength].initVariable();
		
		this.currentCycle ++;
		if(PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[4]) ||
				PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5])){
			if(this.currentCycle >= PublicConstants.MaxCycle){
				this.endDecision(tempValueIndex);
				//this.endBestDecison();
				this.stopRunning();
			}
		}else{
			//判断是否下一轮开始
			if(this.currentCycle < PublicConstants.MaxCycle){
				startIteration();
			}
			else{        //停止agent
				this.endDecision(tempValueIndex);
				//this.endBestDecison();
				this.stopRunning();
			}
		}	
	}

	private void disposeValueMessage(Message msg) {
		//取出蚂蚁携带的上下文
		OneValueMsgContext MsgObj = (OneValueMsgContext) msg.getValue();
		LinkedList<ValueMsgContext> list = MsgObj.getMsgContext();
		int cycle = list.get(0).getCycle();
		//保存到自己当前的上下文中
		for(ValueMsgContext obj: list){
			
			this.union(obj.getCycle(), obj.getAnt(), obj.getCurrentCost(), obj.getContext());
		}
		//执行检查函数
		checkView(cycle);	
	}
	
	private void disposeSelfMessage(Message msg){
		this.cycle++;
		checkView(this.cycle);
	}
	
	private void union(int cycle, int ant, int cost, Context context){
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		tmpInfo.currentCosts.put(ant, tmpInfo.currentCosts.get(ant)+cost);
		tmpInfo.context.get(ant).merge(context);
	}
	
	private void sendValueMessages(int cycle){
		//对于每只蚂蚁，将自己的上下文传递给优先级低的邻居
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		boolean mark = true;
		for(int j =0; j < this.lowPriorities.length;j++){
			int lowId = this.lowPriorities[j];
			OneValueMsgContext MsgObj = new OneValueMsgContext();
			for(int antId : tmpInfo.context.keySet()){	
				Context tempCotext = new Context(tmpInfo.context.get(antId));
				tempCotext.getContext().put(this.id, tmpInfo.selfView.get(antId));
				int localCost = this.localCost(cycle, antId, tmpInfo.selfView.get(antId)) + tmpInfo.currentCosts.get(antId).intValue();
				ValueMsgContext obj = null;
				if(mark == true)
					obj = new ValueMsgContext(cycle,antId,localCost,tempCotext);
				else
					obj = new ValueMsgContext(cycle,antId,0,tempCotext);
				
				MsgObj.getMsgContext().add(obj);
				
			}
			mark = false;
			Message msg=new Message(this.id, lowId, AcoAgent.TYPE_VALUE_MESSAGE, MsgObj);
			this.sendMessage(msg);	
		}
	}
	
	private void sendSelfMessage(){
		Object obj = null;
		Message msg=new Message(this.id, this.id, AcoAgent.TYPE_SELF_MESSAGE, obj);
		this.sendMessage(msg);
	}
	
	private void sendPheromone(int cycle){
		//向所有agent广播信息素更新值delta
		EachCycleInfo tmp = getInfo(cycle);
		
		for(int i=0 ; i < this.allNodes.length; i++){
			int otherId = this.allNodes[i];
			if(otherId != this.id){
				PheroMsgContext obj = new PheroMsgContext(cycle, tmp.bestAnt, tmp.delta, this.bestCost, this.endBestAnt);
				Message msg=new Message(this.id, otherId, AcoAgent.TYPE_VALUE_PHEROMONE, obj);
				this.sendMessage(msg);
			}
		}
	}
	
	private void sendPheromones(int cycle){
		//向所有agent广播信息素更新值delta
		EachCycleInfo tmp = getInfo(cycle);
		
		for(int i=0 ; i < this.allNodes.length; i++){
			int otherId = this.allNodes[i];
			if(otherId != this.id){
				PheroMsgContext obj = new PheroMsgContext(cycle, tmp.bestAnt, tmp.betterDelta, this.bestCost, this.endBestAnt);
				Message msg=new Message(this.id, otherId, AcoAgent.TYPE_VALUE_PHEROMONE, obj);
				this.sendMessage(msg);
			}
		}
	}
	
	private void checkView(int cycle) {
		// 对于每只蚂蚁进行判断是否收到优先级高的邻居的取值
		EachCycleInfo tmpInfo = getInfo(cycle);
		for (int i = 0; i < PublicConstants.countAnt; i++) {
			int antId = PublicConstants.antIds[i];
			if (this.noHighNode()) {
				selectValue(cycle, antId);
			} else if (this.enableValue(cycle, antId)
					&& tmpInfo.mark.get(antId) == false) {
				selectValue(cycle, antId);
			}
		}
		/*
		 * 是否为每只蚂蚁选取值，发送value信息； 以及优先级最低的结点计算信息素并发送信息素；
		 */
		if (this.isAllAntSelected(cycle)) {
			if (this.noLowerNode() == false) {
				this.sendValueMessages(cycle);
				if ((PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[4]) ||
						PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5]))
						&& this.id == this.maxPriority
						&& this.cycle < (PublicConstants.MaxCycle - 1)) {
					this.sendSelfMessage();
				}
			} else if (this.isLeastNode() == false) {
				this.sendValueMessage(cycle);
			} else if (this.isAllSolution(cycle)) {
				int bestAnt = this.selectBestAnt(cycle);
				// 更新信息素并广播更新的信息素delta
				tmpInfo.delta = PublicConstants.computeLogDelta(this
						.solutionCost(cycle,tmpInfo.bestAnt));

				this.localCost = this.solutionCost(cycle, bestAnt);
				if (this.localCost < this.bestCost) {
					this.bestCost = this.localCost;
					Context temp = new Context(tmpInfo.context.get(bestAnt));
					temp.getContext().put(this.id,
							tmpInfo.selfView.get(bestAnt));
					this.bestSolution = temp;
					this.bestValueIndex = tmpInfo.selfView.get(tmpInfo.bestAnt);
					this.endBestAnt = "" + (this.currentCycle+1)
							+ tmpInfo.bestAnt;
					this.bestContext = new Context(tmpInfo.context.get(bestAnt));
					temp = null;
				}

				PublicConstants.dataInCycleIncrease(PublicConstants.realCycle, this.localCost, this.bestCost);
				
				if ((PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[3]) ||
						PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5]))
						&& this.currentCycle < PublicConstants.MaxCycle / 3) {
					tmpInfo.betterDelta = this.selectBetterSolution(cycle);
					this.updatePheros(cycle);
					this.sendPheromones(cycle);
				} else {
					
					this.updatePhero(cycle);
					this.sendPheromone(cycle);
				}
				
				PublicConstants.writeSolution("cycle " + cycle + "\n");
				for(int andId: tmpInfo.context.keySet()){
					Context temp = new Context(tmpInfo.context.get(andId));
					temp.getContext().put(this.id,
							tmpInfo.selfView.get(andId));
					PublicConstants.writeSolution("Ant " + andId + ": ");
					PublicConstants.writeSolution(temp.toString() + "\n");
				}
				PublicConstants.writeSolution("bestAnt: " + bestAnt + "\t" + this.bestCost + "\t" + this.localCost + "\t" + tmpInfo.delta + "\n");
				PublicConstants.writeTau("cycle " + cycle + "\t" +  this.name + "\n" + this.taus.toString() + "\n");
				PublicConstants.writeBestCost(this.bestCost + "\n");
				PublicConstants.writeTotalCost(this.localCost + "\n");
				
				//更新信息素后，数据初始化，用于保存后面的消息
				int tempValueIndex = tmpInfo.selfView.get(tmpInfo.bestAnt);
				this.infos[cycle%PublicConstants.dataLength].initVariable();
				
				this.currentCycle ++ ;
				// 判断是否下一轮开始
				if ((PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[4]) ||
						PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5]))){
					if(this.currentCycle >= PublicConstants.MaxCycle){
						this.endDecision(tempValueIndex);
						//this.endBestDecison();
						PublicConstants.dataInCycleCorrection();
						this.stopRunning();
					}
				}else{
					if (this.currentCycle < PublicConstants.MaxCycle) {
						startIteration();
					} else { // 停止agent
						this.endDecision(tempValueIndex);
						//this.endBestDecison();
						PublicConstants.dataInCycleCorrection();
						this.stopRunning();
					}
				}
			}
		}
	}
	
	private void endDecision(int tempBestValueIndex) {
		// TODO Auto-generated method stub
		this.valueIndex = tempBestValueIndex;
	}
	
	private void endBestDecison(){
		if(this.id == this.minPriority)
			this.localCost = this.bestCost;
		this.valueIndex = this.bestValueIndex;
	}

	private int solutionCost(int cycle, int bestAnt) {
		// TODO Auto-generated method stub
		EachCycleInfo tmp = getInfo(cycle);
		return this.localCost(cycle, bestAnt, tmp.selfView.get(bestAnt)) + tmp.currentCosts.get(bestAnt);
	}

	private boolean enableValue(int cycle, int antId) {
		// TODO Auto-generated method stub
		EachCycleInfo tmp = getInfo(cycle);
		for(int id : this.highPriorities){
			if(!tmp.context.get(antId).getContext().containsKey(id))
				return false;
		}
		return true;
	}

	private void updatePhero(int cycle){
		if(this.noHighNode()){
			return;
		}
		//执行蒸发
		for(int i = 0; i < this.highPriorities.length; i++){
			int oppId = this.highPriorities[i];
			double[][] old_tau = this.taus.get(oppId).getTau();
			for(int myValue=0;myValue<this.domain.length;myValue++){
				for(int oppValue=0;oppValue<this.neighbourDomains.get(oppId).length;oppValue++){
					PublicConstants.evaporate(old_tau, myValue, oppValue);
				}
			}
		}
		
		//再对相应路径增加信息素
		EachCycleInfo tmp = getInfo(cycle);
		for(int i = 0; i < this.highPriorities.length; i++){
			int myValue = tmp.selfView.get(tmp.bestAnt);
			int oppId = this.highPriorities[i];
			int oppValue = tmp.context.get(tmp.bestAnt).getContext().get(oppId);
			double old_tau = this.taus.get(oppId).getTau()[myValue][oppValue];
			double new_tau = PublicConstants.update_tau(old_tau, tmp.delta);
			this.taus.get(oppId).getTau()[myValue][oppValue] = new_tau;
		}
		
		//再对最好解路径信息素更新
		/*if(PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[2])){
			for(int i = 0; i < this.highPriorities.length; i++){
				int myValue = this.bestValueIndex;
				int oppId = this.highPriorities[i];
				int oppValue = this.bestContext.getContext().get(oppId);
				double old_tau = this.taus.get(oppId).getTau()[myValue][oppValue];
				double new_tau = PublicConstants.update_tau(old_tau,PublicConstants.computeLogDelta(this.bestCost));
				this.taus.get(oppId).getTau()[myValue][oppValue] = new_tau;
			}
		}*/
	}
	
	private void updatePheros(int cycle){
		if(this.noHighNode()){
			return;
		}
		//执行蒸发
		for(int i = 0; i < this.highPriorities.length; i++){
			int oppId = this.highPriorities[i];
			double[][] old_tau = this.taus.get(oppId).getTau();
			for(int myValue=0;myValue<this.domain.length;myValue++){
				for(int oppValue=0;oppValue<this.neighbourDomains.get(oppId).length;oppValue++){
					PublicConstants.evaporate(old_tau, myValue, oppValue);
				}
			}
		}
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		//再对相应路径增加信息素
		for(int andId : tmpInfo.betterDelta.keySet()){
			for(int i = 0; i < this.highPriorities.length; i++){
				int myValue = tmpInfo.selfView.get(andId);
				int oppId = this.highPriorities[i];
				int oppValue = tmpInfo.context.get(andId).getContext().get(oppId);
				double old_tau = this.taus.get(oppId).getTau()[myValue][oppValue];
				double new_tau = PublicConstants.update_tau(old_tau, tmpInfo.betterDelta.get(andId));
				this.taus.get(oppId).getTau()[myValue][oppValue] = new_tau;
			}
		}
		
		//再对最好解路径信息素更新
		/*if(PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[2])){
			for(int i = 0; i < this.highPriorities.length; i++){
				int myValue = this.bestValueIndex;
				int oppId = this.highPriorities[i];
				int oppValue = this.bestContext.getContext().get(oppId);
				double old_tau = this.taus.get(oppId).getTau()[myValue][oppValue];
				double new_tau = PublicConstants.update_tau(old_tau,PublicConstants.computeLogDelta(this.bestCost));
				this.taus.get(oppId).getTau()[myValue][oppValue] = new_tau;
			}
		}*/
	}
	
	private void sendValueMessage(int cycle) {
		// TODO Auto-generated method stub
		//发送的信息应该包括了代价，上下文，蚂蚁标识
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		OneValueMsgContext MsgObj = new OneValueMsgContext();
		for(int i : tmpInfo.context.keySet()){
			Context tempCotext = new Context(tmpInfo.context.get(i));
			tempCotext.getContext().put(this.id, tmpInfo.selfView.get(i));
			int localCost = this.localCost(cycle,i, tmpInfo.selfView.get(i));
			ValueMsgContext obj = new ValueMsgContext(cycle,i,tmpInfo.currentCosts.get(i).intValue() + localCost,tempCotext);
			MsgObj.getMsgContext().add(obj);
		}
		Message msg=new Message(this.id, this.minPriority, AcoAgent.TYPE_VALUE_MESSAGE, MsgObj);
		this.sendMessage(msg);
	}

	private int selectBestAnt(int cycle){
		int ret =0;
		EachCycleInfo tmp = this.getInfo(cycle);
		int best = Infinity.INFINITY;
		for(int i : tmp.currentCosts.keySet()){
			int cost = this.solutionCost(cycle,i);
			if(best > cost){
				ret = i;
				best = cost;
			}
		}
		tmp.bestAnt = ret;
		return ret;
	}
	
	private HashMap<Integer, Double> selectBetterSolution(int cycle){
		HashMap<Integer, Double> ret = new HashMap<Integer,Double>();
		HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		int maxAnt = -1;
		for(int andId: tmpInfo.currentCosts.keySet()){
			int cost = this.solutionCost(cycle, andId);
			if(tmp == null || tmp.size() < PublicConstants.betterAntCount){
				tmp.put(andId, cost);
				if(maxAnt == -1 || cost > tmp.get(maxAnt))
					maxAnt = andId;
			}else if(cost < tmp.get(maxAnt)){
				tmp.remove(maxAnt);
				tmp.put(andId, cost);
				maxAnt = -1;
				for(int iter : tmp.keySet()){
					cost = tmp.get(iter);
					if(maxAnt == -1 || cost > tmp.get(maxAnt))
						maxAnt = andId;
				}
			}
		}
		for(int andId : tmp.keySet()){
			double delta = PublicConstants.computeLogDelta(tmp.get(andId));
			ret.put(andId, delta);
		}
		tmp = null;
		return ret;
	}
	
	private boolean isAllAntSelected(int cycle){
		EachCycleInfo tmp = this.getInfo(cycle);
		for(int i : tmp.mark.keySet()){
			if(tmp.mark.get(i).booleanValue() == false){
				return false;
			}
		}
		return true;
	}
	
	private void selectValue(int cycle,int ant){
		/*
		 * 根据概率公式计算，为蚂蚁取值，
		 * 概率相同时，有必要再根据随机概率从这几个值里面选一个
		 * 将选择的值加入selfView currentContext
		 */
		EachCycleInfo tmpInfo = this.getInfo(cycle);
		double[] resultP = this.computeP(cycle, ant);
		this.valueIndex = this.chooseValue(resultP);
		tmpInfo.selfView.put(ant, this.valueIndex);	
		tmpInfo.mark.put(ant, true);
		
		PublicConstants.writeProbility("cycle " + cycle + "\t" +  this.name + "\t" +
		" Ant " + ant + "\t " + tmpInfo.selfView.get(ant) + "\n" + tmpInfo.context.get(ant).getContext().toString() + "\n"  + Arrays.toString(resultP) + "\n");
		
	}
	
	private void startIteration(){
		//新的一轮，一些参数重新初始化
		if((PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[4]) ||
				PublicConstants.ACO_type.equals(PublicConstants.ACO_TYPE[5]))){
			for(int i = 0; i < PublicConstants.dataLength;i++){
				this.infos[i].initVariable();
			}
		}else{
			this.infos[0].initVariable();
		}
		//直接调用检查函数
		checkView(0);
	}
	
	private int[] localCosts(int cycle, int ant)
	{
		int[] ret=new int[this.domain.length];
		EachCycleInfo tmp = this.getInfo(cycle);
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
				highAgentValueIndex=tmp.context.get(ant).getContext().get(highId);
				if(highAgentValueIndex==-1)
				{
					ret[j]+=0;
				}else
				{
					ret[j]+=this.constraintCosts.get(highId)[j][highAgentValueIndex];
				}
			}
		}
		return ret;
	}
	
	private int localCost(int cycle, int ant, int di)
	{
		int ret=0;
		EachCycleInfo tmp = this.getInfo(cycle);
		if(this.noHighNode()==true)
		{
			return ret;
		}
		
		int highId=0;
		int highAgentValueIndex=0;
		for(int i=0;i<this.highPriorities.length;i++)
		{
			highId=this.highPriorities[i];
			
			highAgentValueIndex=tmp.context.get(ant).getContext().get(highId);
			if(highAgentValueIndex==-1)
			{
				ret+=0;
			}else
			{
				ret+=this.constraintCosts.get(highId)[di][highAgentValueIndex];
			}
		}
		return ret;
	}
		
	private boolean isSolution(EachCycleInfo info, int ant){
		return info.context.get(ant).getContext().size() == this.allNodes.length - 1; //最后一个结点的取值未加入
	}
	
	private boolean isAllSolution(int cycle){
		boolean ret = true;
		EachCycleInfo tmp = getInfo(cycle);
		for(int i = 0; i < PublicConstants.countAnt; i++){
			int antId = PublicConstants.antIds[i];
			ret = isSolution(tmp, antId);
			if(ret == false) return ret;
		}
		return ret;
	}
	
	public EachCycleInfo getInfo(int cycle){
		return this.infos[cycle%PublicConstants.dataLength];
	}
	
	private boolean isLeastNode(){
		return this.id == this.minPriority;
	}
	
	private boolean noLowerNode(){
		return this.lowPriorities == null || this.lowPriorities.length == 0;
	}
	
	private boolean noHighNode(){
		return this.highPriorities == null || this.highPriorities.length == 0;
	}
	
	@Override
	protected void runFinished() {
       super.runFinished();
		
		Map<String, Object> result=new HashMap<String, Object>();
		result.put(AgentCycle.KEY_ID, this.id);
		result.put(AgentCycle.KEY_NAME, this.name);
		result.put(AgentCycle.KEY_VALUE, this.domain[valueIndex]);
		result.put("BEST_VALUE", this.domain[this.bestValueIndex]);
		result.put(KEY_LOCALCOST, this.localCost);
		if(this.id == this.minPriority){
			result.put("BEST_LOCALCOST", this.bestCost);
		}else{
			result.put("BEST_LOCALCOST", 0);
		}
	
		this.msgMailer.setResult(result);
		System.out.println("Agent "+this.name+" stopped!");
	}
	
	@Override
	public Object printResults(List<Map<String, Object>> results) {
		// TODO Auto-generated method stub
		double totalCost=0;
		double bestCost = 0;
		int ncccTemp = 0;
		for(Map<String, Object> result : results)
		{
			int id_=(Integer) result.get(AgentCycle.KEY_ID);
			String name_=(String) result.get(AgentCycle.KEY_NAME);
			int value_=(Integer) result.get(AgentCycle.KEY_VALUE);
			totalCost+= (Integer)result.get(KEY_LOCALCOST);
			int bestValue_ = (Integer) result.get("BEST_VALUE");
			bestCost += (Integer)result.get("BEST_LOCALCOST");
			
			String displayStr="Agent "+name_+": id="+id_+" value="+value_ + " bestvalue=" + bestValue_;
			System.out.println(displayStr);
		}
		
		System.out.println("totalCost: "+Infinity.infinityEasy((int)totalCost));
		System.out.println("bestCost: "+Infinity.infinityEasy((int)bestCost));
		
		ResultCycle ret=new ResultCycle();
		ret.nccc=(int)ncccTemp;   //在ACO算法中暂时不用
		ret.totalCost=(int)totalCost;
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

class EachCycleInfo{
	//保存每只在蚂蚁的上下文以及自己对应的取值
	protected HashMap<Integer, Context> context;
	protected HashMap<Integer, Integer> selfView;
	protected HashMap<Integer, Integer> currentCosts;
	//用于标志哪些蚂蚁已经选好值
	protected HashMap<Integer, Boolean> mark;
	protected int bestAnt;
	protected double delta;
	
	protected HashMap<Integer,Double> betterDelta;
	
	private EachCycleInfo(){
		context = new HashMap<Integer, Context>();
		selfView = new HashMap<Integer, Integer>();
		currentCosts = new HashMap<Integer, Integer>();
		mark = new HashMap<Integer, Boolean>();
		betterDelta = new HashMap<Integer, Double>(PublicConstants.betterAntCount);
	}
	
	public void initVariable(){
		for(int i = 0 ; i < PublicConstants.countAnt; i++){
			int antId = PublicConstants.antIds[i];
			this.mark.put(antId, false);
			Context context = new Context();
			this.context.put(antId, context);
			this.currentCosts.put(antId, 0);
			this.selfView.put(antId, -1);
			this.betterDelta.clear();
		}
		bestAnt = -1;
	    delta = -1;
	}
	
	public static EachCycleInfo[] getInstances(){
		EachCycleInfo[] ret = new EachCycleInfo[PublicConstants.dataLength];
		for(int i = 0; i< PublicConstants.dataLength; i++){
			EachCycleInfo info = new EachCycleInfo();
			ret[i] = info;
		}
		return ret;
	}
	
}
