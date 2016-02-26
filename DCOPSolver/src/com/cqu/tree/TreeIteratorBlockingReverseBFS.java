package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在阻断点阻断情况下，由下至上的逆序广度优先遍历
 * @author hz
 *
 */
public class TreeIteratorBlockingReverseBFS extends TreeIterator{
	
	/**
	 * 阻断节点集
	 */
	private Set<Integer> blockingNodesSet;

	public TreeIteratorBlockingReverseBFS(Map<Integer, Integer> parentNodesMap,
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
		
		List<Integer> nodesVisited=new ArrayList<Integer>();
		List<Integer> nodesToVisit=new ArrayList<Integer>();
		nodesToVisit.add(rootNodeId);
		
		while(nodesToVisit.size()>0)
		{
			Integer curNodeId=nodesToVisit.remove(0);
			nodesVisited.add(curNodeId);
			
			int[] childrenNodes=this.childrenNodesMap.get(curNodeId);
			for(Integer nodeId : childrenNodes)
			{
				if(this.blockingNodesSet.contains(nodeId)==false)
				{
					nodesToVisit.add(nodeId);
				}
			}
		}
		
		for(int i=0;i<nodesVisited.size();i++)
		{
			this.nodeOp.operate(nodesVisited.get(nodesVisited.size()-1-i));
			if(this.itStatus==IteratingStatus.ENDEDAHEAD)
			{
				return;
			}
		}
	}

}
