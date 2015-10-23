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
			this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
		}
		if(this.messageQuantity > rs.messageQuantity){
			this.messageQuantity = rs.messageQuantity;
			this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
		}
		if(this.totalTime > rs.totalTime){
			this.totalTime = rs.totalTime;
			this.timeCostInCycle = ((ResultCycle)rs).timeCostInCycle;
		}
		this.lostRatio=Math.min(this.lostRatio, rs.lostRatio);
		this.nccc=Math.min(this.nccc, ((ResultCycle)rs).nccc);
	}
	
	public void max(Result rs)
	{
		if(this.totalCost < rs.totalCost){
			this.totalCost = rs.totalCost;
			this.totalCostInCycle = ((ResultCycle)rs).totalCostInCycle;
		}
		if(this.messageQuantity < rs.messageQuantity){
			this.messageQuantity = rs.messageQuantity;
			this.messageQuantityInCycle = ((ResultCycle)rs).messageQuantityInCycle;
		}
		if(this.totalTime < rs.totalTime){
			this.totalTime = rs.totalTime;
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
		for(int i = 0; i < Math.min(totalCostInCycle.length, ((ResultCycle)rs).totalCostInCycle.length); i++){
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
		for(int i = 0; i < Math.min(totalCostInCycle.length, ((ResultCycle)rs).totalCostInCycle.length); i++){
			this.totalCostInCycle[i] -= (((ResultCycle)rs).totalCostInCycle[i]/validCount);
			this.timeCostInCycle[i] -= (((ResultCycle)rs).timeCostInCycle[i]/validCount);
			this.messageQuantityInCycle[i] -= (((ResultCycle)rs).messageQuantityInCycle[i]/validCount);
		}
		this.nccc-=((ResultCycle)rs).nccc/validCount;
	}
}
