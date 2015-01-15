package com.cqu.core;

import java.util.HashMap;
import java.util.Map;

public class Result {
	
	public int totalCost;
	
	public int messageQuantity;
	public int lostRatio;
	public long totalTime;
	public Map<Integer, Integer> agentValues;
	public Map<String, Object> otherResults;
	
	public Result() {
		// TODO Auto-generated constructor stub
		this.totalCost=0;
		this.messageQuantity=0;
		this.lostRatio=0;
		this.totalTime=0;
		this.otherResults=new HashMap<String, Object>();
	}
	
	public Result(Result rs)
	{
		this.messageQuantity=rs.messageQuantity;
		this.lostRatio=rs.lostRatio;
		this.totalTime=rs.totalTime;
		this.agentValues=rs.agentValues;
	}
	
	public void min(Result rs)
	{
		this.messageQuantity=Math.min(this.messageQuantity, rs.messageQuantity);
		this.lostRatio=Math.min(this.lostRatio, rs.lostRatio);
		this.totalTime=Math.min(this.totalTime, rs.totalTime);
	}
	
	public void max(Result rs)
	{
		this.messageQuantity=Math.max(this.messageQuantity, rs.messageQuantity);
		this.lostRatio=Math.max(this.lostRatio, rs.lostRatio);
		this.totalTime=Math.max(this.totalTime, rs.totalTime);
	}
	
	public void add(Result rs, int validCount)
	{
		this.messageQuantity+=(int)(1.0*rs.messageQuantity/validCount);
		this.lostRatio+=(int)(1.0*rs.lostRatio/validCount);
		this.totalTime+=(int)(1.0*rs.totalTime/validCount);
	}
	
	public void minus(Result rs, int validCount)
	{
		this.messageQuantity-=(int)(1.0*rs.messageQuantity/validCount);
		this.lostRatio-=(int)(1.0*rs.lostRatio/validCount);
		this.totalTime-=(int)(1.0*rs.totalTime/validCount);
	}
}
