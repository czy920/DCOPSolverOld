package com.cqu.bfsdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Problem;
import com.cqu.util.CollectionUtil;

public class CrossEdgeAllocator {
	
	private Map<Integer, boolean[]> isCrossNeighbours;
	private Map<Integer, boolean[]> considerCrossConstraint;
	private Problem problem;
	private Map<Integer, Map<Integer, List<Edge>>> branchCrossConstraints;
	
	public CrossEdgeAllocator(Problem problem) {
		// TODO Auto-generated constructor stub
		this.problem=problem;
		isCrossNeighbours=new HashMap<Integer, boolean[]>();
		considerCrossConstraint=new HashMap<Integer, boolean[]>();
		branchCrossConstraints=new HashMap<Integer, Map<Integer, List<Edge>>>();
		
		for(Integer agentId : problem.neighbourAgents.keySet())
		{
			int[] neighbours=problem.neighbourAgents.get(agentId);
			boolean[] crossNeighbours=new boolean[neighbours.length];
			boolean[] considered=new boolean[neighbours.length];
			Integer parent=problem.parentAgents.get(agentId);
			int[] children=problem.childAgents.get(agentId);
			for(int i=0;i<neighbours.length;i++)
			{
				if(neighbours[i]==parent||CollectionUtil.indexOf(children, neighbours[i])!=-1)
				{
					crossNeighbours[i]=false;
					considered[i]=true;
				}else
				{
					crossNeighbours[i]=true;
					considered[i]=false;
				}
			}
			isCrossNeighbours.put(agentId, crossNeighbours);
			considerCrossConstraint.put(agentId, considered);
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
					this.findFirstMutualAncestor(edge);
					if(branchCrossConstraints.containsKey(edge.getMutualAncestor())==false)
					{
						Map<Integer, List<Edge>> crossConstraints=new HashMap<Integer, List<Edge>>();
						branchCrossConstraints.put(edge.getMutualAncestor(), crossConstraints);
					}
					Map<Integer, List<Edge>> crossConstraints=branchCrossConstraints.get(edge.getMutualAncestor());
					if(crossConstraints.containsKey(edge.getBranchA())==false)
					{
						List<Edge> constraints=new ArrayList<Edge>();
						crossConstraints.put(edge.getBranchA(), constraints);
					}
					if(crossConstraints.containsKey(edge.getBranchB())==false)
					{
						List<Edge> constraints=new ArrayList<Edge>();
						crossConstraints.put(edge.getBranchB(), constraints);
					}
					List<Edge> constraints=crossConstraints.get(edge.getBranchA());
					this.addIfNotExist(constraints, edge);
					constraints=crossConstraints.get(edge.getBranchB());
					this.addIfNotExist(constraints, edge);
				}
			}
		}
		for(Integer agentId : this.branchCrossConstraints.keySet())
		{
			Map<Integer, List<Edge>> crossConstraints=branchCrossConstraints.get(agentId);
			Integer maxKey=this.maxEdgesItemKey(crossConstraints);
			while(crossConstraints.get(maxKey).size()>0)
			{
				for(Edge edge : crossConstraints.get(maxKey))
				{
					boolean[] considered=considerCrossConstraint.get(edge.getNodeA());
					int[] neighbours=problem.neighbourAgents.get(edge.getNodeA());
					considered[this.indexInNeighbours(neighbours, edge.getNodeA())]=true;
				}
				for(Edge edge : crossConstraints.get(maxKey))
				{
					this.removeEdge(crossConstraints.get(edge.getBranchB()), edge);
				}
				crossConstraints.get(maxKey).clear();
				maxKey=this.maxEdgesItemKey(crossConstraints);
			}
		}
	}
	
	private boolean addIfNotExist(List<Edge> edgeList, Edge target)
	{
		for(Edge edge : edgeList)
		{
			if(edge.equals(target)==true)
			{
				return false;
			}
		}
		edgeList.add(target);
		return true;
	}
	
	private boolean removeEdge(List<Edge> edgeList, Edge target)
	{
		for(Edge edge : edgeList)
		{
			if(edge.equals(target)==true)
			{
				edgeList.remove(edge);
				return true;
			}
		}
		return false;
	}
	
	private Integer indexInNeighbours(int[] neighbours, Integer nodeA)
	{
		for(int i=0;i<neighbours.length;i++)
		{
			if(nodeA==neighbours[i])
			{
				return i;
			}
		}
		return -1;
	}
	
	private Integer maxEdgesItemKey(Map<Integer, List<Edge>> crossConstraints)
	{
		int maxCount=0;
		int maxKey=-1;
		for(Integer key : crossConstraints.keySet())
		{
			if(crossConstraints.get(key).size()>maxCount)
			{
				maxCount=crossConstraints.get(key).size();
				maxKey=key;
			}
		}
		return maxKey;
	}
	
	private void findFirstMutualAncestor(Edge edge)
	{
		Integer branchNodeA=edge.getNodeA();
		Integer branchNodeB=edge.getNodeB();
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
		edge.setMutualAncestorAndBranches(branchNodeA, branchNodeABuf, branchNodeBBuf);
	}
	
	public Map<Integer, boolean[]> getConsideredConstraint()
	{
		return this.considerCrossConstraint;
	}
}
