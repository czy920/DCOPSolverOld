package com.cqu.bfsdpop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Problem;
import com.cqu.util.CollectionUtil;

public class CrossEdgeAllocator {
	
	private Map<Integer, boolean[]> isCrossNeighbours;
	private Map<Integer, boolean[]> considerCrossConstraint;
	private Problem problem;
	private Map<Integer, Map<Integer, Integer>> branchCrossConstraints;
	private Map<Integer, List<Edge>> subtreeCrossConstraints;
	
	public CrossEdgeAllocator(Problem problem) {
		// TODO Auto-generated constructor stub
		this.problem=problem;
		isCrossNeighbours=new HashMap<Integer, boolean[]>();
		considerCrossConstraint=new HashMap<Integer, boolean[]>();
		branchCrossConstraints=new HashMap<Integer, Map<Integer, Integer>>();
		subtreeCrossConstraints=new HashMap<Integer, List<Edge>>();
		
		for(Integer agentId : problem.neighbourAgents.keySet())
		{
			int[] neighbours=problem.neighbourAgents.get(agentId);
			boolean[] crossNeighbours=new boolean[neighbours.length];
			Integer parent=problem.parentAgents.get(agentId);
			int[] children=problem.childAgents.get(agentId);
			for(int i=0;i<neighbours.length;i++)
			{
				if(neighbours[i]==parent||CollectionUtil.indexOf(children, neighbours[i])!=-1)
				{
					crossNeighbours[i]=false;
				}else
				{
					crossNeighbours[i]=true;
				}
			}
			isCrossNeighbours.put(agentId, crossNeighbours);
		}
	}
	
	public void allocate()
	{
		for(Integer agentId : this.isCrossNeighbours.keySet())
		{
			int[] neighbours=problem.neighbourAgents.get(agentId);
			boolean[] crossNeighbours=this.isCrossNeighbours.get(agentId);
			for(int i=0;i<neighbours.length;i++)
			{
				if(crossNeighbours[i]==true)
				{
					Edge edge=new Edge(agentId, neighbours[i]);
					Integer[] mutualAncestorAndBranches=this.findFirstMutualAncestorAndBranches(edge);
					
				}
			}
		}
	}
	
	/**
	 * 
	 * @param edge
	 * @return {mutualAncestorNode, branchA(the direct child node of mutualAncestorNode), branchB}
	 */
	private Integer[] findFirstMutualAncestorAndBranches(Edge edge)
	{
		Integer branchNodeA=edge.getIdNodeA();
		Integer branchNodeB=edge.getIdNodeB();
		int branchLevelA=problem.agentLevels.get(branchNodeA);
		int branchLevelB=problem.agentLevels.get(branchNodeB);
		if(branchLevelA<branchLevelB)
		{
			branchNodeA=problem.parentAgents.get(branchNodeA);
			branchLevelA=problem.agentLevels.get(branchNodeA);
		}else
		{
			branchNodeB=problem.parentAgents.get(branchNodeB);
			branchLevelB=problem.agentLevels.get(branchNodeB);
		}
		Integer branchNodeABuf=branchNodeA;
		Integer branchNodeBBuf=branchNodeB;
		while(branchNodeA!=branchNodeB)
		{
			branchNodeABuf=branchNodeA;
			branchNodeA=problem.parentAgents.get(branchNodeA);
			branchLevelA=problem.agentLevels.get(branchNodeA);
			
			branchNodeBBuf=branchNodeB;
			branchNodeB=problem.parentAgents.get(branchNodeB);
			branchLevelB=problem.agentLevels.get(branchNodeB);
		}
		return new Integer[]{branchNodeA, branchNodeABuf, branchNodeBBuf};
	}
	
	public Map<Integer, boolean[]> getConsideredConstraint()
	{
		return this.considerCrossConstraint;
	}
}
