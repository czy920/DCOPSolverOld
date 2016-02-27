package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 在阻断点阻断情况下，由下至上的逆序广度优先遍历；
 * 注意，由于阻断点的限制更全面和灵活，故此遍历方
 * 法不是默认针对下部子树，而是针对阻断点限定下的最大子树；
 * 注意，因为允许根节点也为阻断点，故在此情况下，
 * 会遍历多个非连通子树，一个上部子树和多个下部子树。
 * @author hz
 *
 */
public class TreeIteratorBlockingReverseBFS extends TreeIterator{
	
	/**
	 * 阻断节点集
	 */
	private Set<Integer> blockingNodesSet;
	
	private List<Integer> nodesVisited;

	public TreeIteratorBlockingReverseBFS(Map<Integer, Integer> parentNodesMap,
			Map<Integer, int[]> childrenNodesMap, Integer rootNodeId,
			NodeOperation nodeOp, Set<Integer> blockingNodesSet) {
		super(parentNodesMap, childrenNodesMap, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
		this.blockingNodesSet=blockingNodesSet;
		this.nodesVisited=new ArrayList<Integer>();
	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		if(this.childrenNodesMap==null||this.rootNodeId==null||nodeOp==null)
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
		if(oldestAncestor!=-1)
		{
			nodesToVisit.add(oldestAncestor);
			calNodesToIterate(nodesToVisit);
		}
		
		if(this.blockingNodesSet.contains(rootNodeId)==false)
		{
			if(oldestAncestor==-1)
			{
				nodesToVisit.add(rootNodeId);
				calNodesToIterate(nodesToVisit);
			}
		}else
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
			calNodesToIterate(nodesToVisit);
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
	
	private void calNodesToIterate(List<Integer> nodesToVisit)
	{
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
	}

}
