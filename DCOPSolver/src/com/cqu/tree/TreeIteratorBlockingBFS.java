package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在阻断点阻断情况下，由上至下的广度优先遍历；
 * 注意，由于阻断点的限制更全面和灵活，故此遍
 * 历方法不是默认针对下部子树，而是针对阻断点
 * 限定下的最大子树；
 * 注意，因为允许根节点也为阻断点，故在此情况
 * 下，会遍历多个非连通子树，一个上部子树和多个
 * 下部子树。
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
		if(this.parentNodesMap==null||this.childrenNodesMap==null||
				this.rootNodeId==null||nodeOp==null)
		{
			return;
		}
		
		Integer oldestAncestor=-1;
		Integer ancestorNode=this.parentNodesMap.get(this.rootNodeId);
		while(ancestorNode!=-1)
		{
			if(blockingNodesSet.contains(ancestorNode))
			{
				break;
			}else
			{
				oldestAncestor=ancestorNode;
			}
			ancestorNode=this.parentNodesMap.get(ancestorNode);
		}
		
		List<Integer> nodesToVisit=new ArrayList<Integer>();
		nodesToVisit.add(oldestAncestor);
		doIterate(nodesToVisit);
		if(this.itStatus==IteratingStatus.ENDEDAHEAD)
		{
			return;
		}
		
		if(this.blockingNodesSet.contains(rootNodeId))
		{
			nodesToVisit.clear();
			int[] childrenNodes=this.childrenNodesMap.get(this.rootNodeId);
			for(Integer nodeId : childrenNodes)
			{
				if(this.blockingNodesSet.contains(nodeId)==false)
				{
					nodesToVisit.add(nodeId);
				}
			}
			doIterate(nodesToVisit);
		}
	}
	
	private void doIterate(List<Integer> nodesToVisit)
	{
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
