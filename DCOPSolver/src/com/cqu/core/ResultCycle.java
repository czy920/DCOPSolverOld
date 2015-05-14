package com.cqu.core;

import java.util.HashMap;

public class ResultCycle extends Result{

	public ResultCycle() {
		// TODO Auto-generated constructor stub
		this.totalCost=0;
		this.messageQuantity=0;
		this.lostRatio=0;
		this.totalTime=0;
		this.otherResults=new HashMap<String, Object>();
		this.totalCostInCycle = null;
	}
	
	public ResultCycle(Result rs)
	{
		this.messageQuantity=rs.messageQuantity;
		this.lostRatio=rs.lostRatio;
		this.totalTime=rs.totalTime;
		this.agentValues=rs.agentValues;
		this.totalCost = rs.totalCost;
		this.totalCostInCycle = rs.totalCostInCycle;
	}
	
	public void min(Result rs)
	{
		this.messageQuantity=Math.min(this.messageQuantity, rs.messageQuantity);
		this.lostRatio=Math.min(this.lostRatio, rs.lostRatio);
		this.totalTime=Math.min(this.totalTime, rs.totalTime);
		if(this.totalCost > rs.totalCost){
			this.totalCost = rs.totalCost;
			this.totalCostInCycle = rs.totalCostInCycle;
		}
	}
	
	public void max(Result rs)
	{
		this.messageQuantity=Math.max(this.messageQuantity, rs.messageQuantity);
		this.lostRatio=Math.max(this.lostRatio, rs.lostRatio);
		this.totalTime=Math.max(this.totalTime, rs.totalTime);
		if(this.totalCost < rs.totalCost){
			this.totalCost = rs.totalCost;
			this.totalCostInCycle = rs.totalCostInCycle;
		}
	}
	
	public void add(Result rs, int validCount)
	{
		this.messageQuantity+=(int)(1.0*rs.messageQuantity/validCount);
		this.lostRatio+=(int)(1.0*rs.lostRatio/validCount);
		this.totalTime+=(int)(1.0*rs.totalTime/validCount);
		this.totalCost+=(int)(1.0*rs.totalCost/validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[rs.totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++)
				this.totalCostInCycle[i] = 0;
		}
		for(int i = 0; i < totalCostInCycle.length; i++)
			this.totalCostInCycle[i] += (rs.totalCostInCycle[i]/validCount);
	}
	
	public void minus(Result rs, int validCount)
	{
		this.messageQuantity-=(int)(1.0*rs.messageQuantity/validCount);
		this.lostRatio-=(int)(1.0*rs.lostRatio/validCount);
		this.totalTime-=(int)(1.0*rs.totalTime/validCount);
		this.totalCost-=(int)(1.0*rs.totalCost/validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[rs.totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++)
				this.totalCostInCycle[i] = 0;
		}
		for(int i = 0; i < totalCostInCycle.length; i++)
			this.totalCostInCycle[i] -= (rs.totalCostInCycle[i]/validCount);
	}
}
