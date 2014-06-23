package com.cqu.core;

import java.util.HashMap;
import java.util.Map;

public class Problem {
	
	public final static String KEY_PARENT="parent";
	public final static String KEY_PSEUDO_PARENT="pseudo_parent";
	public final static String KEY_CHILDREN="children";
	public final static String KEY_NEIGHBOUR="neighbour";
	
	public Problem() {
		// TODO Auto-generated constructor stub
		domains=new HashMap<String, int[]>();
		costs=new HashMap<String, int[]>();
		agentNames=new HashMap<Integer, String>();
		agentDomains=new HashMap<Integer, String>();
		neighbourAgents=new HashMap<Integer, int[]>();
		parentAgents=new HashMap<Integer, Integer>();
		pseudoParentAgents=new HashMap<Integer, int[]>();
		childAgents=new HashMap<Integer, int[]>();
		pseudoChildAgents=new HashMap<Integer, int[]>();
		agentConstraintCosts=new HashMap<Integer, String[]>();
	}
	
	//all possible domains and costs
	public Map<String, int[]> domains;
	public Map<String, int[]> costs;
	
	//for each agent
	public Map<Integer, String> agentNames;
	public Map<Integer,String> agentDomains;
	public Map<Integer, int[]> neighbourAgents;
	
	public Map<Integer, Integer> parentAgents;
	public Map<Integer, int[]> pseudoParentAgents;
	public Map<Integer, int[]> childAgents;
	public Map<Integer, int[]> pseudoChildAgents;
	
	public Map<Integer, String[]> agentConstraintCosts;
}
