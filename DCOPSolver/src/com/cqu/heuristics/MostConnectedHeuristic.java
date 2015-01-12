package com.cqu.heuristics;

import com.cqu.parser.Problem;
import com.cqu.util.StatisticUtil;
import com.cqu.varOrdering.dfs.DFSview;

public class MostConnectedHeuristic implements ScoringHeuristic<Short>{
	private Problem problem; //主要是为了获取问题本身结点之间的约束关系
	
	public MostConnectedHeuristic(Problem problem)
	{
		this.problem = problem;
	}
	@Override
	//针对根结点的选择
	public int getScores() {
		// TODO Auto-generated method stub
		int maxNeighbourCount=-1;
		int maxNeighbourCountNodeId=-1;
		/*
		 * 寻找邻居结点最多的结点ID
		 */
		for(Integer nodeId : this.problem.neighbourAgents.keySet())
		{
			int temp=this.problem.neighbourAgents.get(nodeId).length;
			if(temp >= maxNeighbourCount)
			{
				maxNeighbourCount=temp;
				maxNeighbourCountNodeId=nodeId;
			}
		}
		return maxNeighbourCountNodeId;
	}

	@Override
	public int getScores(Integer nodeId, DFSview dfsview) {
		// TODO Auto-generated method stub
		int[] neighbours=dfsview.neighbourNodes.get(nodeId);
		int[] counts=dfsview.neighbourCounts.get(nodeId);
		for(int i=0;i<counts.length;i++)
		{
			if(dfsview.nodeIterated.get(neighbours[i])==true) //是否遍历过
			{
				counts[i]=-1; //将最大的结点数目变成最小
			}
		}
		int maxIndex=StatisticUtil.max(counts);
		if(counts[maxIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbours[maxIndex];
		}
	}

}
