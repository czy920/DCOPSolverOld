package com.cqu.bfsdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.parser.Problem;
import com.cqu.util.CollectionUtil;

public abstract class CrossEdgeAllocator {
	
	protected Map<Integer, int[]> neighbourAgents;
	
	protected Map<Integer, boolean[]> isCrossNeighbours;
	protected Map<Integer, boolean[]> considerCrossConstraint;

	protected Map<Integer, List<Edge>> crossEdges;
	
	public CrossEdgeAllocator(Problem problem) {
		// TODO Auto-generated constructor stub
		this.neighbourAgents=problem.neighbourAgents;
		
		isCrossNeighbours=new HashMap<Integer, boolean[]>();
		considerCrossConstraint=new HashMap<Integer, boolean[]>();
		crossEdges=new HashMap<Integer, List<Edge>>();
		
		for(Integer agentId : neighbourAgents.keySet())
		{
			int[] neighbours=neighbourAgents.get(agentId);
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
					
					if(crossEdges.containsKey(agentId)==false)
					{
						crossEdges.put(agentId, new ArrayList<Edge>());
					}
					crossEdges.get(agentId).add(new Edge(agentId, neighbours[i]));
				}
			}
			isCrossNeighbours.put(agentId, crossNeighbours);
			considerCrossConstraint.put(agentId, considered);
		}
	}
	
	public abstract void allocate();
	
	/**
	 * 从 edgeList中移除target
	 * @param edgeList
	 * @param target
	 * @return
	 */
	protected boolean removeEdge(List<Edge> edgeList, Edge target)
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
	
	/**
	 * 获得交叉边分配结果
	 * @return 每个Agent需要考虑的约束边数组，true为需要考虑，false反之
	 */
	public Map<Integer, boolean[]> getConsideredConstraint()
	{
		return this.considerCrossConstraint;
	}
}
