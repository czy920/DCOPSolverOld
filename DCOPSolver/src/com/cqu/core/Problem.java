package com.cqu.core;

import java.util.List;

public class Problem {
	
	public final static String KEY_PARENT="parent";
	public final static String KEY_PSEUDO_PARENT="pseudo_parent";
	public final static String KEY_CHILDREN="children";
	public final static String KEY_NEIGHBOUR="neighbour";
	
	public int agentCount;
	public int constraintCount;
	
	//all possible domains and costs
	public List<int[]> domains;
	public List<int[][]> costs;
	
	//for each agent
	public int[] agentIds;
	public String[] agentNames;
	public int[] agentDomains;
	
	public List<int[]> neighbourAgents;
	public List<Integer> parentAgents;
	public List<int[]> pseudoParentAgents;
	public List<int[]> childAgents;
	public List<int[]> pseudoChildAgents;
	
	public int[][] neighbourAgentDomains;
	public int[][] agentConstraintCosts;
}
