package com.cqu.core;

import java.util.HashMap;

public class ResultCycle extends Result{
	
	public ResultCycle() {
		// TODO Auto-generated constructor stub
		super();
		this.totalCostInCycle = null;
	}
	
	public ResultCycle(Result rs)
	{
		super(rs);
		this.totalCostInCycle = rs.totalCostInCycle;
		this.nccc=((ResultCycle)rs).nccc;
	}
	
	public void min(Result rs)
	{
		super.min(rs);
		if(this.totalCost > rs.totalCost){
			this.totalCost = rs.totalCost;
			this.totalCostInCycle = rs.totalCostInCycle;
		}
		this.nccc=Math.min(this.nccc, ((ResultCycle)rs).nccc);
	}
	
	public void max(Result rs)
	{
		super.max(rs);
		if(this.totalCost < rs.totalCost){
			this.totalCost = rs.totalCost;
			this.totalCostInCycle = rs.totalCostInCycle;
		}
		this.nccc=Math.max(this.nccc, ((ResultCycle)rs).nccc);
	}
	
	public void add(Result rs, int validCount)
	{
		super.add(rs, validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[rs.totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++)
				this.totalCostInCycle[i] = 0;
		}
		for(int i = 0; i < totalCostInCycle.length; i++)
			this.totalCostInCycle[i] += (rs.totalCostInCycle[i]/validCount);
		this.nccc+=(int)(1.0*((ResultCycle)rs).nccc/validCount);
	}
	
	public void minus(Result rs, int validCount)
	{
		super.minus(rs, validCount);
		if(totalCostInCycle == null){
			totalCostInCycle = new double[rs.totalCostInCycle.length];
			for(int i = 0; i < totalCostInCycle.length; i++)
				this.totalCostInCycle[i] = 0;
		}
		for(int i = 0; i < totalCostInCycle.length; i++)
			this.totalCostInCycle[i] -= (rs.totalCostInCycle[i]/validCount);
		this.nccc-=(int)(1.0*((ResultCycle)rs).nccc/validCount);
	}
}
