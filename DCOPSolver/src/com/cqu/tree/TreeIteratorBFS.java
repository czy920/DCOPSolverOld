package com.cqu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 从上至下广度遍历树或子树的各节点
 * @author hz
 *
 */
public class TreeIteratorBFS extends TreeIterator{

	public TreeIteratorBFS(Map<Integer, Integer> parentNodesMap,
			Map<Integer, int[]> childrenNodesMap, Integer rootNodeId,
			NodeOperation nodeOp) {
		super(parentNodesMap, childrenNodesMap, rootNodeId, nodeOp);
		// TODO Auto-generated constructor stub
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
				nodesToVisit.add(nodeId);
			}
		}
	}

}
