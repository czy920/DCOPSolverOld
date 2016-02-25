package com.cqu.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BFSTree extends TreeGenerator{

	private Map<Integer, Boolean> nodeIterated;
	
	public BFSTree(Map<Integer, int[]> neighbors) {
		super(neighbors);
		// TODO Auto-generated constructor stub
		this.nodeIterated=new HashMap<Integer, Boolean>();
		for(Integer nodeId : this.neighbors.keySet())
		{
			this.nodeIterated.put(nodeId, false);
		}
		
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
			curLevel=this.levels.get(curNodeId);
			
			int[] curLevelNodes=this.neighbors.get(curNodeId);
			for(int i=0;i<curLevelNodes.length;i++)
			{
				if(this.nodeIterated.get(curLevelNodes[i])==false)
				{
					nodeQueue.add(curLevelNodes[i]);
					this.levels.put(curLevelNodes[i], curLevel+1);
					
					this.parents.put(curLevelNodes[i], curNodeId);
					this.children.get(curNodeId).add(curLevelNodes[i]);
					
					this.nodeIterated.put(curLevelNodes[i], true);
				}
			}
			this.nodeIterated.put(curNodeId, true);
		}
	}
}
