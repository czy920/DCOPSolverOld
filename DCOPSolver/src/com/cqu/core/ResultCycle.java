package com.cqu.core;

import java.util.HashMap;

public class ResultCycle extends Result{
	
	public double[] totalCostInCycle;			//新增参数，用于记录每一个回合的totalCost，来描述动态变化
	public long[] timeCostInCycle;
	public int[] messageQuantityInCycle;
	public double nccc;
	
	public int[] ant_totalCostInCyle;
	public int[] ant_bestCostInCycle;
		
	public ResultCycle() {
		// TODO Auto-generated constructor stub
		super();
		this.totalCostInCycle = null;
		this.timeCostInCycle = null;
		this.messageQuantityInCycle = null;
		this.ant_totalCostInCyle = null;
		this.ant_bestCostInCycle = null;
	}
	
	public ResultCycle(Result rs)
	{
		super(rs);
		this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
		this.timeCostInCycle = ((ResultCycle)rs).timeCostInCycle;
		this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
		this.nccc=((ResultCycle)rs).nccc;
		this.ant_totalCostInCyle = ((ResultCycle)rs).ant_totalCostInCyle;
		this.ant_bestCostInCycle = ((ResultCycle)rs).ant_bestCostInCycle;
	}
	
	public void min(Result rs)
	{
		if(this.totalCost > rs.totalCost){
			this.totalCost = rs.totalCost;
			this.messageQuantity = rs.messageQuantity;
			this.totalTime = rs.totalTime;
			
			this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
			this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
			this.timeCostInCycle = ((ResultCycle)rs).timeCostInCycle;
			
			//this.ant_totalCostInCyle = ((ResultCycle)rs).ant_totalCostInCyle;
			//this.ant_bestCostInCycle = ((ResultCycle)rs).ant_bestCostInCycle;
		}
		this.lostRatio=Math.min(this.lostRatio, rs.lostRatio);
		this.nccc=Math.min(this.nccc, ((ResultCycle)rs).nccc);
		
	}
	
	public void max(Result rs)
	{
		if(this.totalCost < rs.totalCost){
			this.totalCost = rs.totalCost;
			this.messageQuantity = rs.messageQuantity;
			this.totalTime = rs.totalTime;
			
			this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
			this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
			this.timeCostInCycle = ((ResultCycle)rs).timeCostInCycle;
			
			//this.ant_totalCostInCyle = ((ResultCycle)rs).ant_totalCostInCyle;
			//this.ant_bestCostInCycle = ((ResultCycle)rs).ant_bestCostInCycle;
		}
		this.lostRatio=Math.max(this.lostRatio, rs.lostRatio);
		this.nccc=Math.max(this.nccc, ((ResultCycle)rs).nccc);
	}
	
	public void add(Result rs, int validCount)
	{
		super.add(rs, validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[((ResultCycle)rs).totalCostInCycle.length];
			timeCostInCycle = new long[((ResultCycle)rs).timeCostInCycle.length];
			messageQuantityInCycle = new int[((ResultCycle)rs).messageQuantityInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++){
				this.totalCostInCycle[i] = 0;
				this.timeCostInCycle[i] = 0;
				this.messageQuantityInCycle[i] = 0;
			}
		}
		if(totalCostInCycle.length < ((ResultCycle)rs).totalCostInCycle.length){
			double[] tempCost = new double[((ResultCycle)rs).totalCostInCycle.length];
			long[] tempTime = new long[((ResultCycle)rs).totalCostInCycle.length];
			int[] tempMQ = new int[((ResultCycle)rs).totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++){
				tempCost[i] = totalCostInCycle[i];
				tempTime[i] = timeCostInCycle[i];
				tempMQ[i] = messageQuantityInCycle[i];
			}
			for(int i = totalCostInCycle.length; i < ((ResultCycle)rs).totalCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
				tempTime[i] = tempTime[i-1];
				tempMQ[i] = tempMQ[i-1];
			}
			totalCostInCycle = tempCost;
			timeCostInCycle = tempTime;
			messageQuantityInCycle = tempMQ;
		}
		else if(totalCostInCycle.length > ((ResultCycle)rs).totalCostInCycle.length){
			double[] tempCost = new double[totalCostInCycle.length];
			long[] tempTime = new long[totalCostInCycle.length];
			int[] tempMQ = new int[totalCostInCycle.length];
			for(int i = 0; i < ((ResultCycle)rs).totalCostInCycle.length; i++){
				tempCost[i] = ((ResultCycle)rs).totalCostInCycle[i];
				tempTime[i] = ((ResultCycle)rs).timeCostInCycle[i];
				tempMQ[i] = ((ResultCycle)rs).messageQuantityInCycle[i];
			}
			for(int i = ((ResultCycle)rs).totalCostInCycle.length; i < totalCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
				tempTime[i] = tempTime[i-1];
				tempMQ[i] = tempMQ[i-1];
			}
			((ResultCycle)rs).totalCostInCycle = tempCost;
			((ResultCycle)rs).timeCostInCycle = tempTime;
			((ResultCycle)rs).messageQuantityInCycle = tempMQ;
		}
		for(int i = 0; i < totalCostInCycle.length; i++){
			this.totalCostInCycle[i] += (((ResultCycle)rs).totalCostInCycle[i]/validCount);
			this.timeCostInCycle[i] += (((ResultCycle)rs).timeCostInCycle[i]/validCount);
			this.messageQuantityInCycle[i] += (((ResultCycle)rs).messageQuantityInCycle[i]/validCount);
		}
		
		//蚁群算法引入下面数据
		//目前算法都是以轮数作为终止，批处理的每一次执行的轮数相同，应该没必要做下面处理
		//蚁群算法引入下面数据
		if(this.ant_totalCostInCyle == null){
			this.ant_totalCostInCyle = new int[((ResultCycle)rs).ant_totalCostInCyle.length];
			this.ant_bestCostInCycle = new int[((ResultCycle)rs).ant_bestCostInCycle.length];
			for(int i = 0; i < ant_totalCostInCyle.length; i++){
				this.ant_totalCostInCyle[i]=0;
				this.ant_bestCostInCycle[i] = 0;
			}
		}
		/*if(this.ant_totalCostInCyle.length < ((ResultCycle)rs).ant_totalCostInCyle.length){
			int[] tempAnt_totalCost = new int[((ResultCycle)rs).ant_totalCostInCyle.length];
			int[] tempAnt_tempbestCost = new int[((ResultCycle)rs).ant_bestCostInCycle.length];
			for(int i = 0; i < ant_totalCostInCyle.length; i++){
				tempAnt_totalCost[i] = this.ant_totalCostInCyle[i];
				tempAnt_tempbestCost[i] = this.ant_bestCostInCycle[i];
			}
			for(int i = ant_totalCostInCyle.length; i < tempAnt_totalCost.length;i++){
				tempAnt_totalCost[i] = tempAnt_totalCost[i-1];
				tempAnt_tempbestCost[i] = tempAnt_tempbestCost[i-1];
			}
			ant_totalCostInCyle = tempAnt_totalCost;
			ant_bestCostInCycle = tempAnt_tempbestCost;
		}else if(this.ant_totalCostInCyle.length > ((ResultCycle)rs).ant_totalCostInCyle.length){
			int[] tempAnt_totalCost = new int[this.ant_totalCostInCyle.length];
			int[] tempAnt_tempbestCost = new int[this.ant_bestCostInCycle.length];
			for(int i = 0; i < ((ResultCycle)rs).ant_totalCostInCyle.length; i++){
				tempAnt_totalCost[i] = ((ResultCycle)rs).ant_totalCostInCyle[i];
				tempAnt_tempbestCost[i] =((ResultCycle)rs).ant_bestCostInCycle[i];
			}
			for(int i = ((ResultCycle)rs).ant_totalCostInCyle.length; i < tempAnt_totalCost.length; i++){
				tempAnt_totalCost[i] = tempAnt_totalCost[i-1];
				tempAnt_tempbestCost[i] = tempAnt_tempbestCost[i-1];
			}
			((ResultCycle)rs).ant_totalCostInCyle = tempAnt_totalCost;
			((ResultCycle)rs).ant_bestCostInCycle = tempAnt_tempbestCost;
		}*/
		//只记录最后一次的，不求平均
		for(int i = 0; i < this.ant_totalCostInCyle.length;i++){
			this.ant_totalCostInCyle[i] = ((ResultCycle)rs).ant_totalCostInCyle[i];
			this.ant_bestCostInCycle[i] = ((ResultCycle)rs).ant_bestCostInCycle[i];
		}
		
		this.nccc+=((ResultCycle)rs).nccc/validCount;
	}
	
	public void minus(Result rs, int validCount)
	{
		super.minus(rs, validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[((ResultCycle)rs).totalCostInCycle.length];
			timeCostInCycle = new long[((ResultCycle)rs).timeCostInCycle.length];
			messageQuantityInCycle = new int[((ResultCycle)rs).messageQuantityInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++){
				this.totalCostInCycle[i] = 0;
				this.timeCostInCycle[i] = 0;
				this.messageQuantityInCycle[i] = 0;
			}
		}
		if(totalCostInCycle.length < ((ResultCycle)rs).totalCostInCycle.length){
			double[] tempCost = new double[((ResultCycle)rs).totalCostInCycle.length];
			long[] tempTime = new long[((ResultCycle)rs).totalCostInCycle.length];
			int[] tempMQ = new int[((ResultCycle)rs).totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++){
				tempCost[i] = totalCostInCycle[i];
				tempTime[i] = timeCostInCycle[i];
				tempMQ[i] = messageQuantityInCycle[i];
			}
			for(int i = totalCostInCycle.length; i < ((ResultCycle)rs).totalCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
				tempTime[i] = tempTime[i-1];
				tempMQ[i] = tempMQ[i-1];
			}
			totalCostInCycle = tempCost;
			timeCostInCycle = tempTime;
			messageQuantityInCycle = tempMQ;
		}
		else if(totalCostInCycle.length > ((ResultCycle)rs).totalCostInCycle.length){
			double[] tempCost = new double[totalCostInCycle.length];
			long[] tempTime = new long[totalCostInCycle.length];
			int[] tempMQ = new int[totalCostInCycle.length];
			for(int i = 0; i < ((ResultCycle)rs).totalCostInCycle.length; i++){
				tempCost[i] = ((ResultCycle)rs).totalCostInCycle[i];
				tempTime[i] = ((ResultCycle)rs).timeCostInCycle[i];
				tempMQ[i] = ((ResultCycle)rs).messageQuantityInCycle[i];
			}
			for(int i = ((ResultCycle)rs).totalCostInCycle.length; i < totalCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
				tempTime[i] = tempTime[i-1];
				tempMQ[i] = tempMQ[i-1];
			}
			((ResultCycle)rs).totalCostInCycle = tempCost;
			((ResultCycle)rs).timeCostInCycle = tempTime;
			((ResultCycle)rs).messageQuantityInCycle = tempMQ;
		}
		for(int i = 0; i < totalCostInCycle.length; i++){
			this.totalCostInCycle[i] -= (((ResultCycle)rs).totalCostInCycle[i]/validCount);
			this.timeCostInCycle[i] -= (((ResultCycle)rs).timeCostInCycle[i]/validCount);
			this.messageQuantityInCycle[i] -= (((ResultCycle)rs).messageQuantityInCycle[i]/validCount);
		}
		
		// 蚁群算法引入下面数据
		if(this.ant_totalCostInCyle == null){
			this.ant_totalCostInCyle = new int[((ResultCycle)rs).ant_totalCostInCyle.length];
			this.ant_bestCostInCycle = new int[((ResultCycle)rs).ant_bestCostInCycle.length];
			for(int i = 0; i < ant_totalCostInCyle.length; i++){
				this.ant_totalCostInCyle[i]=0;
				this.ant_bestCostInCycle[i] = 0;
			}
		}
		/*if (this.ant_totalCostInCyle.length < ((ResultCycle) rs).ant_totalCostInCyle.length) {
			int[] tempAnt_totalCost = new int[((ResultCycle) rs).ant_totalCostInCyle.length];
			int[] tempAnt_tempbestCost = new int[((ResultCycle) rs).ant_bestCostInCycle.length];
			for (int i = 0; i < ant_totalCostInCyle.length; i++) {
				tempAnt_totalCost[i] = this.ant_totalCostInCyle[i];
				tempAnt_tempbestCost[i] = this.ant_bestCostInCycle[i];
			}
			for (int i = ant_totalCostInCyle.length; i < tempAnt_totalCost.length; i++) {
				tempAnt_totalCost[i] = tempAnt_totalCost[i - 1];
				tempAnt_tempbestCost[i] = tempAnt_tempbestCost[i - 1];
			}
			ant_totalCostInCyle = tempAnt_totalCost;
			ant_bestCostInCycle = tempAnt_tempbestCost;
		} else if (this.ant_totalCostInCyle.length > ((ResultCycle) rs).ant_totalCostInCyle.length) {
			int[] tempAnt_totalCost = new int[this.ant_totalCostInCyle.length];
			int[] tempAnt_tempbestCost = new int[this.ant_bestCostInCycle.length];
			for (int i = 0; i < ((ResultCycle) rs).ant_totalCostInCyle.length; i++) {
				tempAnt_totalCost[i] = ((ResultCycle) rs).ant_totalCostInCyle[i];
				tempAnt_tempbestCost[i] = ((ResultCycle) rs).ant_bestCostInCycle[i];
			}
			for (int i = ((ResultCycle) rs).ant_totalCostInCyle.length; i < tempAnt_totalCost.length; i++) {
				tempAnt_totalCost[i] = tempAnt_totalCost[i - 1];
				tempAnt_tempbestCost[i] = tempAnt_tempbestCost[i - 1];
			}
			((ResultCycle) rs).ant_totalCostInCyle = tempAnt_totalCost;
			((ResultCycle) rs).ant_bestCostInCycle = tempAnt_tempbestCost;
		}*/
		//只记录最后一次，无去最大、最小操作
		/*for (int i = 0; i < this.ant_totalCostInCyle.length; i++) {
			this.ant_totalCostInCyle[i] -= (((ResultCycle) rs).ant_totalCostInCyle[i] / validCount);
			this.ant_bestCostInCycle[i] -= (((ResultCycle) rs).ant_bestCostInCycle[i] / validCount);
		}*/
		
		this.nccc-=((ResultCycle)rs).nccc/validCount;
	}
}
