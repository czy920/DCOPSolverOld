package org.cqu.heuristics;

import org.cqu.problem.parser.Problem;
import org.cqu.utility.StatisticUtil;
import org.cqu.varOrdering.dfs.DFSview;


public class MostConstributionHeuristic implements ScoringHeuristic<Short>{
	private Problem problem; //主要是为了获取问题本身结点之间的约束关系
	public MostConstributionHeuristic (Problem problem)
	{
		this.problem = problem;
	}
	@Override
	public int getScores() {
		// TODO Auto-generated method stub
		int maxConstribution=-1;
		int maxConstributionNodeId=-1;
		/*
		 * 寻找邻居结点最多的结点ID
		 */
		
		for(Integer nodeId : this.problem.agentProperty.keySet())
		{
			int temp=this.problem.agentProperty.get(nodeId);
			if(temp >= maxConstribution)
			{
				maxConstribution=temp;
				maxConstributionNodeId=nodeId;
			}
		}
		return maxConstributionNodeId;
	}

	@Override
	public int getScores(Integer nodeId, DFSview dfsview) {
		// TODO Auto-generated method stub
		int[] neighbours=dfsview.neighbourNodes.get(nodeId);
		int[] counts = new int[neighbours.length];
		for (int id = 0; id < neighbours.length; id++)
		{
			counts[id] = this.problem.agentProperty.get(neighbours[id]);
		}
		for(int i=0;i<neighbours.length;i++)
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
