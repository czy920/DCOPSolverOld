package org.cqu.varOrdering.dfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DFSview {
public Map<Integer, int[]> neighbourNodes; //无向图（邻接表存储）
	
	public Map<Integer, List<Integer>> childrenNodes; //生成树子节点
	public Map<Integer, Integer> parentNode; //生成树父节点
	public Map<Integer, Integer> nodeLevel; //节点层次，根节点为0层
	
	public long pseduHeight=0;
	public Map<Integer, Boolean> nodeIterated; //节点是否遍历过
	public Map<Integer, int[]> neighbourCounts;
	
	public Integer rootId=-1;
	
	public DFSview(Map<Integer, int[]> neighbourNodes) {
		this.neighbourNodes = neighbourNodes;
		this.childrenNodes = new HashMap<Integer, List<Integer>> ();
	    this.parentNode = new HashMap<Integer, Integer>();
	    this.nodeLevel = new HashMap<Integer, Integer>();
	    this.nodeIterated=new HashMap<Integer, Boolean>();
		this.neighbourCounts=new HashMap<Integer, int[]>();
		
		/*
		 * 计算每个结点相邻结点的个数
		 */
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			this.childrenNodes.put(nodeId, new ArrayList<Integer>());
			
			this.nodeIterated.put(nodeId, false);
			
			int[] neighbours=this.neighbourNodes.get(nodeId);
			int[] nodeNeighbourCounts=new int[neighbours.length];
			
			for(int i=0;i<nodeNeighbourCounts.length;i++)
			{
				nodeNeighbourCounts[i]=this.neighbourNodes.get(neighbours[i]).length;
				
			}
			
			this.neighbourCounts.put(nodeId, nodeNeighbourCounts);
		}
		/*
		 * 暂时以此种为选择策略
		 */
		/*int maxNeighbourCount=-1;
		int maxNeighbourCountNodeId=-1;
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int temp=this.neighbourNodes.get(nodeId).length;
			if(temp>maxNeighbourCount)
			{
				maxNeighbourCount=temp;
				maxNeighbourCountNodeId=nodeId;
			}
		}
		this.rootId=maxNeighbourCountNodeId;*/
	}
	
	public Set<String> getPseudoParents(int nodeId)
	{
		Set<String> pseudoParents = new HashSet<String>();
		int[] neightbour = this.neighbourNodes.get(nodeId);  //获取其所有的邻居结点
		
		for(int i = 0; i < neightbour.length; i++)
		{
			int pseudo = -1;
			int parent = this.parentNode.get(nodeId) ;
			if (parent == neightbour[i]) //排除直接父亲结点
				continue;
			if (this.nodeLevel.get(nodeId) > this.nodeLevel.get(neightbour[i])) 
			{
				pseudo = neightbour[i] ;
			}
			if (pseudo != -1)
			{
				String pseudoParent = "X" + pseudo;
				pseudoParents.add(pseudoParent);
			}
		}
		return pseudoParents;
	}
	public int getParent()
	{
		return 0;
	}
}
