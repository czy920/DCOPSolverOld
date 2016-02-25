package com.cqu.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphIteratorBlockingDFS extends GraphIterator{
	
	/**
	 * 记录是否遍历过
	 */
	private Map<Integer, Boolean> nodeIterated;
	
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
		this.nodeIterated=new HashMap<Integer, Boolean>();
		for(Integer nodeId : this.neighbors.keySet())
		{
			this.nodeIterated.put(nodeId, false);
		}
	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		if(this.nodeOp==null)
		{
			return;
		}
		
		int iteratedCount=0;
		Integer curNodeId=this.rootNodeId;
		if(this.blockingNodesSet.contains(curNodeId))
		{
			return;
		}
		this.nodeIterated.put(curNodeId, true);
		iteratedCount++;//根节点已遍历
		
		this.nodeOp.operate(curNodeId);
		if(this.itStatus==IteratingStatus.ENDEDAHEAD)
		{
			return;
		}
		
		int totalCount=neighbors.size();
		while(iteratedCount<totalCount)
		{
			Integer nextNodeId=this.getRandomNodeId(curNodeId);
			if(nextNodeId==-1)
			{
				//回溯
				curNodeId=this.parents.get(curNodeId);
			}else
			{
				this.parents.put(nextNodeId, curNodeId);
				this.nodeIterated.put(nextNodeId, true);
				iteratedCount++;
				
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
			if(this.nodeIterated.get(neighbors[i])==false)
			{
				if(this.blockingNodesSet.contains(neighbors[i]))
				{
					return neighbors[i];
				}
			}
		}
		return -1;
	}

}
