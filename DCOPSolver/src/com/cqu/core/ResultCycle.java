package com.cqu.core;

import java.util.HashMap;

public class ResultCycle extends Result{
	
	public double[] totalCostInCycle;			//新增参数，用于记录每一个回合的totalCost，来描述动态变化
	public long[] timeCostInCycle;
	public int[] messageQuantityInCycle;
	public double nccc;
		
	public ResultCycle() {
		// TODO Auto-generated constructor stub
		super();
		this.totalCostInCycle = null;
		this.timeCostInCycle = null;
		this.messageQuantityInCycle = null;
	}
	
	public ResultCycle(Result rs)
	{
		super(rs);
		this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
		this.timeCostInCycle = ((ResultCycle)rs).timeCostInCycle;
		this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
		this.nccc=((ResultCycle)rs).nccc;
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
		this.nccc-=((ResultCycle)rs).nccc/validCount;
	}
}
