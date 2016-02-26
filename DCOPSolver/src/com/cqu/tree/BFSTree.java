package com.cqu.tree;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 广度优先搜索树
 * @author hz
 *
 */
public class BFSTree extends TreeGenerator{

	public BFSTree(Map<Integer, int[]> neighbors) {
		super(neighbors);
		// TODO Auto-generated constructor stub
		this.rootId=this.neighbors.keySet().iterator().next();
	}

	public void generate() {
		// TODO Auto-generated method stub
		Integer curLevel=0;
		Integer curNodeId=-1;
		List<Integer> nodeQueue=new LinkedList<Integer>();
		nodeQueue.add(this.rootId);
		this.parents.put(this.rootId, -1);
		this.levels.put(this.rootId, 0);
		while(nodeQueue.size()>0)
		{
			curNodeId=nodeQueue.remove(0);
			this.nodeIterated.add(curNodeId);
			curLevel=this.levels.get(curNodeId);
			
			int[] curLevelNodes=this.neighbors.get(curNodeId);
			for(int i=0;i<curLevelNodes.length;i++)
			{
				if(this.nodeIterated.contains(curLevelNodes[i])==false)
				{
					nodeQueue.add(curLevelNodes[i]);
					this.levels.put(curLevelNodes[i], curLevel+1);
					
					this.parents.put(curLevelNodes[i], curNodeId);
					this.children.get(curNodeId).add(curLevelNodes[i]);
					
					this.nodeIterated.add(curLevelNodes[i]);
				}
			}
		}
	}
}
