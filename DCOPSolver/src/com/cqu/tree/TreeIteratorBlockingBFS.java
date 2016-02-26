package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在阻断点阻断情况下，由上至下的广度优先遍历
 * @author hz
 *
 */
public class TreeIteratorBlockingBFS extends TreeIterator{
	
	/**
	 * 阻断节点集
	 */
	private Set<Integer> blockingNodesSet;

	public TreeIteratorBlockingBFS(Map<Integer, Integer> parentNodesMap,
			Map<Integer, int[]> childrenNodesMap, Integer rootNodeId,
			NodeOperation nodeOp, Set<Integer> blockingNodesSet) {
		super(parentNodesMap, childrenNodesMap, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
		this.blockingNodesSet=blockingNodesSet;
	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		if(this.childrenNodesMap==null||this.rootNodeId==null||nodeOp==null)
		{
			return;
		}
		
		List<Integer> nodesToVisit=new ArrayList<Integer>();
		nodesToVisit.add(rootNodeId);
		if(this.blockingNodesSet.contains(rootNodeId))
		{
			return;
		}
		
		while(nodesToVisit.size()>0)
		{
			Integer curNodeId=nodesToVisit.remove(0);
			this.nodeOp.operate(curNodeId);
			if(this.itStatus==IteratingStatus.ENDEDAHEAD)
			{
				return;
			}
			
			int[] childrenNodes=this.childrenNodesMap.get(curNodeId);
			for(Integer nodeId : childrenNodes)
			{
				if(this.blockingNodesSet.contains(nodeId)==false)
				{
					nodesToVisit.add(nodeId);
				}
			}
		}
	}

}
