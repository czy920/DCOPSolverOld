package com.cqu.bfsdpop;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.parser.Problem;

/**
 * cluster removing by removed cluster count and cross edge count
 * @author CQU
 *
 */
public class CEAllocatorB extends CrossEdgeAllocator{
	
	/**
	 * 簇的得分，从高到低移簇
	 */
	private Map<Integer, Integer> clusterScores;

	public CEAllocatorB(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
		clusterScores=new HashMap<Integer, Integer>();
	}

	@Override
	public void allocate() {
		// TODO Auto-generated method stub
		if(crossEdges.size()<=0)
		{
			return;
		}
		computeScore();
		Integer maxAgent=maxOne();
		while(clusterScores.get(maxAgent)>0)
		{
			removeCluster(maxAgent);
			
			computeScore();
			maxAgent=maxOne();
		}
	}
	
	private Integer maxOne()
	{
		Integer maxValue=-1;
		Integer maxId=-1;
		for(Integer agentId : clusterScores.keySet())
		{
			if(clusterScores.get(agentId)>maxValue)
			{
				maxValue=clusterScores.get(agentId);
				maxId=agentId;
			}
		}
		return maxId;
	}
	
	private void computeScore()
	{
		for(Integer agentId : crossEdges.keySet())
		{
			int score=0;
			
			List<Edge> edges=crossEdges.get(agentId);
			score+=edges.size();//边数
			
			for(Edge edge : edges)
			{
				if(crossEdges.get(edge.getNodeB()).size()==1)
				{
					score+=100;
				}
			}
			
			clusterScores.put(agentId, score);
		}
	}

}
