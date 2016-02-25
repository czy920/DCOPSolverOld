package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 从下至上广度逆序遍历树或子树的各节点
 * @author hz
 *
 */
public class TreeIteratorBT extends TreeIterator{

	public TreeIteratorBT(Map<Integer, Integer> parentNodesMap,
			Map<Integer, int[]> childrenNodesMap, Integer rootNodeId,
			NodeOperation nodeOp) {
		super(parentNodesMap, childrenNodesMap, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void iterate() {
		// TODO Auto-generated method stub
		if(this.parentNodesMap==null||this.childrenNodesMap==null||
				this.rootNodeId==null||nodeOp==null)
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
				nodesToVisit.add(nodeId);
			}
		}
		
		for(int i=0;i<nodesVisited.size();i++)
		{
			this.nodeOp.operate(nodesVisited.get(nodesVisited.size()-1-i));
		}
	}

}
