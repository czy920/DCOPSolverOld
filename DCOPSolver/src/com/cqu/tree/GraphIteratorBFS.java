package com.cqu.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 广度优先遍历
 * @author hz
 *
 */
public class GraphIteratorBFS extends GraphIterator{
	
	/**
	 * 记录是否遍历过
	 */
	private Map<Integer, Boolean> nodeIterated;

	public GraphIteratorBFS(Map<Integer, int[]> neighbors, Integer rootNodeId,
			NodeOperation nodeOp) {
		super(neighbors, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
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
		
		Integer curNodeId=-1;
		List<Integer> nodeQueue=new LinkedList<Integer>();
		nodeQueue.add(this.rootNodeId);
		while(nodeQueue.size()>0)
		{
			curNodeId=nodeQueue.remove(0);
			this.nodeIterated.put(curNodeId, true);
			
			this.nodeOp.operate(curNodeId);
			if(this.itStatus==IteratingStatus.ENDEDAHEAD)
			{
				return;
			}
			
			int[] curLevelNodes=this.neighbors.get(curNodeId);
			for(int i=0;i<curLevelNodes.length;i++)
			{
				if(this.nodeIterated.get(curLevelNodes[i])==false)
				{
					nodeQueue.add(curLevelNodes[i]);
				}
			}
		}
	}

}
