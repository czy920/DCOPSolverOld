package com.cqu.core;

public class ResultCycleAls extends ResultCycle{
	public double[] bestCostInCycle;
	
	public ResultCycleAls(){
		super();
		bestCostInCycle = null;
	}
	
	public ResultCycleAls(Result rs){
		super(rs);
		bestCostInCycle = ((ResultCycleAls)rs).bestCostInCycle;
	}

	public void min(Result rs)
	{
		super.min(rs);
		if(this.totalCost > rs.totalCost){
			this.bestCostInCycle = ((ResultCycleAls)rs).bestCostInCycle;
		}
	}
	
	public void max(Result rs)
	{
		super.max(rs);
		if(this.totalCost < rs.totalCost){
			this.bestCostInCycle = ((ResultCycleAls)rs).bestCostInCycle;
		}
	}
	
	public void add(Result rs, int validCount)
	{
		super.add(rs, validCount);
		if(bestCostInCycle == null){
			this.bestCostInCycle = new double[((ResultCycleAls)rs).bestCostInCycle.length];
			for(int i = 0; i < bestCostInCycle.length; i++){
				this.bestCostInCycle[i] = 0;
			}
		}
		for(int i = 0; i < Math.min(bestCostInCycle.length, ((ResultCycleAls)rs).bestCostInCycle.length); i++){
			this.bestCostInCycle[i] += (((ResultCycleAls)rs).bestCostInCycle[i]/validCount);
		}
	}
	
	public void minus(Result rs, int validCount)
	{
		super.minus(rs, validCount);
		if(bestCostInCycle == null){
			this.bestCostInCycle = new double[((ResultCycleAls)rs).bestCostInCycle.length];
			for(int i = 0; i < bestCostInCycle.length; i++){
				this.bestCostInCycle[i] = 0;
			}
		}
		for(int i = 0; i < Math.min(bestCostInCycle.length, ((ResultCycleAls)rs).bestCostInCycle.length); i++){
			this.bestCostInCycle[i] -= (((ResultCycleAls)rs).bestCostInCycle[i]/validCount);
		}
	}
}
