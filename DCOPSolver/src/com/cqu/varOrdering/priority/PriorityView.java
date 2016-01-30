package com.cqu.varOrdering.priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorityView {
	public Map<Integer, int[]> neighbourNodes; //无向图（邻接表存储）
	public Map<Integer, Boolean> nodeIterated; //节点是否遍历过
	
	public Map<Integer, List<Integer>> highNodes; //生成高优先级结点
	public Map<Integer, List<Integer>> lowNodes; //生成低优先级结点
	public Map<Integer, Integer> priority;  //每个结点的优先级
	
	public Integer maxPriority=-1;
	public Integer minPriority=-1;
	public Map<Integer, int[]> neighbourCounts;
	
	public int[] allNodes;  //为ACO算法最低优先级结点去广播以及判断是否完整解
	
	public PriorityView(Map<Integer, int[]> neighbourNodes){
		this.neighbourNodes = neighbourNodes;
		this.highNodes = new HashMap<Integer, List<Integer>>();
		this.lowNodes = new HashMap<Integer, List<Integer>>();
		this.priority = new HashMap<Integer, Integer>(); 
		this.nodeIterated = new HashMap<Integer, Boolean>();
		this.neighbourCounts = new HashMap<Integer, int[]>();
		this.allNodes = new int[this.neighbourNodes.size()];
		
		/*
		 * 计算每个结点相邻结点的个数
		 */
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			this.highNodes.put(nodeId, new ArrayList<Integer>());
			this.lowNodes.put(nodeId, new ArrayList<Integer>());
			
			this.nodeIterated.put(nodeId, false);
			
			int[] neighbours=this.neighbourNodes.get(nodeId);
			int[] nodeNeighbourCounts=new int[neighbours.length];
			
			for(int i=0;i<nodeNeighbourCounts.length;i++)
			{
				nodeNeighbourCounts[i]=this.neighbourNodes.get(neighbours[i]).length;
				
			}
			
			this.neighbourCounts.put(nodeId, nodeNeighbourCounts);
		}
		
		getAllNodes();
	}
	
	public void getAllNodes(){
		int i = 0;
		for(Integer nodeId : this.neighbourNodes.keySet()){
			allNodes[i++] = nodeId;
		}
	}
	
	
	

}
