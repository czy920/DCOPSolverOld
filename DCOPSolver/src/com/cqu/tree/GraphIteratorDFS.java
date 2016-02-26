package com.cqu.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * 深度优先遍历
 * @author hz
 *
 */
public class GraphIteratorDFS extends GraphIterator{
	
	/**
	 * 父节点
	 */
	protected Map<Integer, Integer> parents;

	public GraphIteratorDFS(Map<Integer, int[]> neighbors, Integer rootNodeId,
			NodeOperation nodeOp) {
		super(neighbors, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
		this.parents=new HashMap<Integer, Integer>();
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
		this.nodesIterated.add(curNodeId);
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
				this.nodesIterated.add(nextNodeId);
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
			if(this.nodesIterated.contains(neighbors[i])==false)
			{
				return neighbors[i];
			}
		}
		return -1;
	}

}
