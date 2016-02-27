package com.cqu.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphIteratorBlockingDFS extends GraphIterator{
	
	/**
	 * 父节点
	 */
	protected Map<Integer, Integer> parents;
	
	/**
	 * 阻断节点集
	 */
	private Set<Integer> blockingNodesSet;

	public GraphIteratorBlockingDFS(Map<Integer, int[]> neighbors,
			Integer rootNodeId, NodeOperation nodeOp, Set<Integer> blockingNodesSet) {
		super(neighbors, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
		this.blockingNodesSet=blockingNodesSet;
		
		this.parents=new HashMap<Integer, Integer>();
	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		if(this.nodeOp==null)
		{
			return;
		}
		
		Integer curNodeId=this.rootNodeId;
		if(this.blockingNodesSet.contains(this.rootNodeId))
		{
			return;
		}
		this.nodesIterated.add(curNodeId);
		
		this.nodeOp.operate(curNodeId);
		if(this.itStatus==IteratingStatus.ENDEDAHEAD)
		{
			return;
		}
		
		while(true)
		{
			Integer nextNodeId=this.getRandomNodeId(curNodeId);
			if(nextNodeId==-1)
			{
				//遍历结束标志：回溯至根节点且无未遍历邻居节点
				if(curNodeId.equals(this.rootNodeId))
				{
					return;
				}
				
				//回溯
				curNodeId=this.parents.get(curNodeId);
			}else
			{
				this.parents.put(nextNodeId, curNodeId);
				this.nodesIterated.add(nextNodeId);
				
				curNodeId=nextNodeId;
				this.nodeOp.operate(curNodeId);
				if(this.itStatus==IteratingStatus.ENDEDAHEAD)
				{
					return;
				}
			}
		}
	}
	
	private Integer getRandomNodeId(Integer nodeId)
	{
		int[] neighbors=this.neighbors.get(nodeId);
		for(int i=0;i<neighbors.length;i++)
		{
			if(this.nodesIterated.contains(neighbors[i])==false)
			{
				if(this.blockingNodesSet.contains(neighbors[i])==false)
				{
					return neighbors[i];
				}
			}
		}
		return -1;
	}

}
