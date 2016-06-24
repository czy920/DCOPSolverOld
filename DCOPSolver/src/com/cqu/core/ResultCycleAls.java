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
//			for(int i = 0; i < bestCostInCycle.length; i++){
//				this.bestCostInCycle[i] = 0;
//			}
		}
		if(bestCostInCycle.length < ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[((ResultCycleAls)rs).bestCostInCycle.length];
			for(int i = 0; i < bestCostInCycle.length; i++){
				tempCost[i] = bestCostInCycle[i];
			}
			for(int i = bestCostInCycle.length; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			bestCostInCycle = tempCost;
		}
		else if(bestCostInCycle.length > ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[bestCostInCycle.length];
			for(int i = 0; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = ((ResultCycleAls)rs).bestCostInCycle[i];
			}
			for(int i = ((ResultCycleAls)rs).bestCostInCycle.length; i < bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			((ResultCycleAls)rs).bestCostInCycle = tempCost;;
		}
		for(int i = 0; i < bestCostInCycle.length; i++){
			this.bestCostInCycle[i] += (((ResultCycleAls)rs).bestCostInCycle[i]/validCount);
		}
	}
	
	public void minus(Result rs, int validCount)
	{
		super.minus(rs, validCount);
//		if(bestCostInCycle == null){
//			this.bestCostInCycle = new double[((ResultCycleAls)rs).bestCostInCycle.length];
//			for(int i = 0; i < bestCostInCycle.length; i++){
//				this.bestCostInCycle[i] = 0;
//			}
//		}
		if(bestCostInCycle.length < ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[((ResultCycleAls)rs).bestCostInCycle.length];
			for(int i = 0; i < bestCostInCycle.length; i++){
				tempCost[i] = bestCostInCycle[i];
			}
			for(int i = bestCostInCycle.length; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			bestCostInCycle = tempCost;
		}
		else if(bestCostInCycle.length > ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[bestCostInCycle.length];
			for(int i = 0; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = ((ResultCycleAls)rs).bestCostInCycle[i];
			}
			for(int i = ((ResultCycleAls)rs).bestCostInCycle.length; i < bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			((ResultCycleAls)rs).bestCostInCycle = tempCost;;
		}
		for(int i = 0; i < bestCostInCycle.length; i++){
			this.bestCostInCycle[i] -= (((ResultCycleAls)rs).bestCostInCycle[i]/validCount);
		}
	}
	
	public void addAvg(Result rs)
	{
		super.addAvg(rs);
		if(bestCostInCycle == null){
			this.bestCostInCycle = new double[((ResultCycleAls)rs).bestCostInCycle.length];
		}
		if(bestCostInCycle.length < ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[((ResultCycleAls)rs).bestCostInCycle.length];
			for(int i = 0; i < bestCostInCycle.length; i++){
				tempCost[i] = bestCostInCycle[i];
			}
			for(int i = bestCostInCycle.length; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			bestCostInCycle = tempCost;
		}
		else if(bestCostInCycle.length > ((ResultCycleAls)rs).bestCostInCycle.length){
			double[] tempCost = new double[bestCostInCycle.length];
			for(int i = 0; i < ((ResultCycleAls)rs).bestCostInCycle.length; i++){
				tempCost[i] = ((ResultCycleAls)rs).bestCostInCycle[i];
			}
			for(int i = ((ResultCycleAls)rs).bestCostInCycle.length; i < bestCostInCycle.length; i++){
				tempCost[i] = tempCost[i-1];
			}
			((ResultCycleAls)rs).bestCostInCycle = tempCost;;
		}
		for(int i = 0; i < bestCostInCycle.length; i++){
			this.bestCostInCycle[i] += (((ResultCycleAls)rs).bestCostInCycle[i]);
		}
	}
	
	public void avg(int instanceNumber){
		super.avg(instanceNumber);
		for(int i = 0; i < bestCostInCycle.length; i++){
			this.bestCostInCycle[i] = (bestCostInCycle[i]/instanceNumber);
		}
	}
}
