package com.cqu.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;

public class BFSTree implements TreeGenerator{
	
    private Map<Integer, int[]> neighbourNodes;//无向图（邻接表存储）
	
	private Map<Integer, List<Integer>> childrenNodes;//生成树子节点
	private Map<Integer, Integer> parentNode;//生成树父节点
	private Map<Integer, Integer> nodeLevel;//节点层次，根节点为0层
	
	private Map<Integer, Boolean> nodeIterated;
	private Map<Integer, int[]> neighbourCounts;
	
	private Integer rootId=-1;
	
	public BFSTree(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		this.neighbourNodes=neighbourNodes;
		
		this.childrenNodes=new HashMap<Integer, List<Integer>>();
		this.parentNode=new HashMap<Integer, Integer>();
		this.nodeLevel=new HashMap<Integer, Integer>();
		
		this.nodeIterated=new HashMap<Integer, Boolean>();
		this.neighbourCounts=new HashMap<Integer, int[]>();
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			this.childrenNodes.put(nodeId, new ArrayList<Integer>());
			
			this.nodeIterated.put(nodeId, false);
			
			int[] neighbours=this.neighbourNodes.get(nodeId);
			int[] nodeNeighbourCounts=new int[neighbours.length];
			for(int i=0;i<nodeNeighbourCounts.length;i++)
			{
				nodeNeighbourCounts[i]=this.neighbourNodes.get(neighbours[i]).length;
			}
			this.neighbourCounts.put(nodeId, nodeNeighbourCounts);
		}
		
		int maxNeighbourCount=-1;
		int maxNeighbourCountNodeId=-1;
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int temp=this.neighbourNodes.get(nodeId).length;
			if(temp>maxNeighbourCount)
			{
				maxNeighbourCount=temp;
				maxNeighbourCountNodeId=nodeId;
			}
		}
		this.rootId=maxNeighbourCountNodeId;
		/*int minNeighbourCount=Integer.MAX_VALUE;
		int minNeighbourCountNodeId=-1;
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int temp=this.neighbourNodes.get(nodeId).length;
			if(temp<minNeighbourCount)
			{
				minNeighbourCount=temp;
				minNeighbourCountNodeId=nodeId;
			}
		}
		this.rootId=minNeighbourCountNodeId;*/
	}

	public void generate() {
		// TODO Auto-generated method stub
		Integer curLevel=0;
		Integer curNodeId=-1;
		List<Integer> nodeQueue=new LinkedList<Integer>();
		nodeQueue.add(this.rootId);
		this.parentNode.put(this.rootId, -1);
		this.nodeLevel.put(this.rootId, 0);
		while(nodeQueue.size()>0)
		{
			curNodeId=nodeQueue.remove(0);
			curLevel=this.nodeLevel.get(curNodeId);
			
			int[] curLevelNodes=this.neighbourNodes.get(curNodeId);
			for(int i=0;i<curLevelNodes.length;i++)
			{
				if(this.nodeIterated.get(curLevelNodes[i])==false)
				{
					nodeQueue.add(curLevelNodes[i]);
					this.nodeLevel.put(curLevelNodes[i], curLevel+1);
					
					this.parentNode.put(curLevelNodes[i], curNodeId);
					this.childrenNodes.get(curNodeId).add(curLevelNodes[i]);
					
					this.nodeIterated.put(curLevelNodes[i], true);
				}
			}
			this.nodeIterated.put(curNodeId, true);
		}
	}
	
	public Map<Integer, Integer> getNodeLevels() {
		// TODO Auto-generated method stub
		return this.nodeLevel;
	}

	public Map<Integer, Integer> getParentNode() {
		// TODO Auto-generated method stub
		return this.parentNode;
	}

	public Map<Integer, int[]> getChildrenNodes() {
		// TODO Auto-generated method stub
		return CollectionUtil.transform(childrenNodes);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map[] getAllChildrenAndParentNodes() {
		// TODO Auto-generated method stub
		Map<Integer, List<Integer>> allChildren=new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> allParents=new HashMap<Integer, List<Integer>>();
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			allChildren.put(nodeId, new ArrayList<Integer>());
			allParents.put(nodeId, new ArrayList<Integer>());
		}
		return new Map[]{CollectionUtil.transform(allParents), CollectionUtil.transform(allChildren)};
	}

	@Override
	public long getPseduHeight() {
		// TODO Auto-generated method stub
		return 0;
	}
}
