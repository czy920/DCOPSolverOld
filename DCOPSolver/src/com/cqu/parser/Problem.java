package com.cqu.parser;

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
		agentLevels=new HashMap<Integer, Integer>();
		treeDepth=0;
		pseudoHeight=0;
		agentDomains=new HashMap<Integer, String>();
		neighbourAgents=new HashMap<Integer, int[]>();
		parentAgents=new HashMap<Integer, Integer>();
		allParentAgents=new HashMap<Integer, int[]>();
		childAgents=new HashMap<Integer, int[]>();
		allChildrenAgents=new HashMap<Integer, int[]>();
		agentConstraintCosts=new HashMap<Integer, String[]>();
		relationCost = new HashMap<String, Integer>();
		agentProperty = new HashMap<Integer, Integer>();
	}
	
	//all possible domains and costs
	public Map<String, int[]> domains;
	public Map<String, int[]> costs;
	
	//for each agent
	public Map<Integer, String> agentNames;
	public Map<Integer, Integer> agentLevels;
	public long treeDepth;
	public long pseudoHeight;     //用来辅佐BD-ADOPT算法
	public Map<Integer,String> agentDomains;
	public Map<Integer, int[]> neighbourAgents;
	
	public Map<Integer, Integer> parentAgents;
	public Map<Integer, int[]> allParentAgents;
	public Map<Integer, int[]> childAgents;
	public Map<Integer, int[]> allChildrenAgents;
	
	public Map<Integer, String[]> agentConstraintCosts;
	
	public Map<Integer, boolean[]> crossConstraintAllocation;
	
	public Map<String, Integer> relationCost; // 记录每个连接关系矩阵中的最小代价
	public Map<Integer, Integer> agentProperty; // 将agent中的评估代价作为其属性
}
