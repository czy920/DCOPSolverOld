package com.cqu.core;

import java.util.List;

public class Problem {
	
	public int agentCount;
	public int constraintCount;
	
	public List<int[]> domains;
	public List<int[][]> costs;
	
	//对每个agent
	public int[] agentIds;
	public String[] agentNames;
	public int[] neighbourAgents;
	public int[] agentDomains;//存储domains的index
	public int[] agentConstraints;//存储
}
