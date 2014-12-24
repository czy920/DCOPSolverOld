package com.cqu.heuristics;

import java.util.Map;

import com.cqu.parser.Problem;
import com.cqu.varOrdering.dfs.DFSview;

public class RandScoringHeuristic implements ScoringHeuristic<Short>{
	private Problem problem;
	public RandScoringHeuristic(Problem problem)
	{
		this.problem = problem;
	}
	@Override
	public int getScores() {
		// TODO Auto-generated method stub
		int[] nodes = new int[this.problem.neighbourAgents.size()];
		int i = 0;
		for(Map.Entry<Integer, int[]> entry : this.problem.neighbourAgents.entrySet()){
			nodes[i] = entry.getKey();
			i++;
		}
		int randNodeId = (int)(Math.random() * nodes.length); //随机产生一个下标
		System.out.println("root: " + nodes[randNodeId]);
		return nodes[randNodeId];
	}
	@Override
	public int getScores(Integer nodeId, DFSview dfsview) {
		// TODO Auto-generated method stub
		int[] neighbours=dfsview.neighbourNodes.get(nodeId); //获取value集合
		Boolean[] r = new Boolean[neighbours.length];
		for (int i = 0; i < r.length; i++){
			r[i] = false;
		}
		int n = 0;
		int temp = 0;
		int num = 1;
		while (true){
			temp = (int) (neighbours.length * Math.random()) ; //遍历完全部，获取的是下标
			
			if (dfsview.nodeIterated.get(neighbours[temp])==true) //是否遍历过
			{
				if (!r[temp]){ //结点遍历过的
					r[temp] = true; // 遍历过的
				} 
				num = num + 1;
			}		
			if (!r[temp]) //没有遍历过
			{ 
				n = n + 1;		
				break;	
			}
			if (num == neighbours.length){ //当所有的结点都遍历完之后
				break;
			}
		}
		
		if (n == 1){
			return neighbours[temp];
		}else {
			return -1;
		}
	}

}
