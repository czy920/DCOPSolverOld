package com.cqu.bfsdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Problem;
import com.cqu.util.CollectionUtil;

public class CrossEdgeAllocator {
	
	private Map<Integer, int[]> neighbourAgents;
	
	private Map<Integer, boolean[]> isCrossNeighbours;
	private Map<Integer, boolean[]> considerCrossConstraint;

	private Map<Integer, List<Edge>> crossEdges;
	
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
	
	public void allocate()
	{
		if(crossEdges.size()<=0)
		{
			return;
		}
		Integer[] keys=new ListSizeComparator<Edge>(crossEdges).sort();
		List<Edge> edgeList=crossEdges.get(keys[keys.length-1]);
		while(edgeList.size()>0)
		{
			for(int i=0;i<edgeList.size();i++)
			{
				Edge edge=edgeList.get(i);
				
				int index=CollectionUtil.indexOf(neighbourAgents.get(edge.getNodeB()), edge.getNodeA());
				considerCrossConstraint.get(edge.getNodeB())[index]=true;
				
				this.removeEdge(crossEdges.get(edge.getNodeB()), edge);
			}
			edgeList.clear();
			
			keys=new ListSizeComparator<Edge>(crossEdges).sort();
			edgeList=crossEdges.get(keys[keys.length-1]);
		}
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
	
	public Map<Integer, boolean[]> getConsideredConstraint()
	{
		return this.considerCrossConstraint;
	}
}
