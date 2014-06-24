package com.cqu.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;
import com.cqu.util.StatisticsUtil;

public class DFSTree {
	
	private Map<Integer, int[]> neighbourNodes;//无向图（邻接表存储）
	
	private Map<Integer, List<Integer>> childrenNodes;//生成树子节点
	private Map<Integer, Integer> parentNode;//生成树父节点
	private Map<Integer, Integer> nodeFloor;//节点层次，根节点为0层
	
	private Map<Integer, Boolean> nodeIterated;
	private Map<Integer, int[]> neighbourCounts;
	
	private Integer rootId=-1;
	
	public DFSTree(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		this.neighbourNodes=neighbourNodes;
		
		this.childrenNodes=new HashMap<Integer, List<Integer>>();
		this.parentNode=new HashMap<Integer, Integer>();
		this.nodeFloor=new HashMap<Integer, Integer>();
		
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
	}
	
	public void generate()
	{
		int iteratedCount=0;
		Integer curFloor=0;
		
		Integer curNodeId=this.rootId;
		this.nodeIterated.put(curNodeId, true);
		iteratedCount++;//根节点已遍历
		this.parentNode.put(curNodeId, -1);
		this.nodeFloor.put(curNodeId, curFloor);
		
		int totalCount=neighbourNodes.size();
		while(iteratedCount<totalCount)
		{
			Integer nextNodeId=this.getMaxNeighboursNodeId(curNodeId);
			if(nextNodeId==-1)
			{
				curFloor--;
				//回溯
				curNodeId=this.parentNode.get(curNodeId);
			}else
			{
				curFloor++;
				
				this.childrenNodes.get(curNodeId).add(nextNodeId);
				this.parentNode.put(nextNodeId, curNodeId);
				
				this.nodeIterated.put(nextNodeId, true);
				this.nodeFloor.put(nextNodeId, curFloor);
				iteratedCount++;
				
				curNodeId=nextNodeId;
			}
		}
	}
	
	private Integer getMaxNeighboursNodeId(Integer nodeId)
	{
		int[] neighbours=this.neighbourNodes.get(nodeId);
		int[] counts=this.neighbourCounts.get(nodeId);
		for(int i=0;i<counts.length;i++)
		{
			if(this.nodeIterated.get(neighbours[i])==true)
			{
				counts[i]=-1;
			}
		}
		int maxIndex=StatisticsUtil.max(counts);
		if(counts[maxIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbours[maxIndex];
		}
	}
	
	public Map<Integer, int[]> getChildrenNodes()
	{
		return CollectionUtil.transform(childrenNodes);
	}
	
	public Map<Integer, Integer> getParentNode()
	{
		return this.parentNode;
	}
	
	/**
	 * pseudoChildren包含children, pseudoParents包含parent
	 * @return Map<Integer, int[]>[2]{pseudoParents, pseudoChildren}
	 */
	@SuppressWarnings("rawtypes")
	public Map[] getPseudoChildrenAndParentNodes()
	{
		Map<Integer, List<Integer>> pseudoChildren=new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> pseudoParents=new HashMap<Integer, List<Integer>>();
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int[] neighbours=this.neighbourNodes.get(nodeId);
			List<Integer> children=this.childrenNodes.get(nodeId);
			Integer parent=this.parentNode.get(nodeId);
			Integer floor=this.nodeFloor.get(nodeId);
			if(parent==-1)
			{
				//根节点
				List<Integer> pseudoChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					pseudoChildrenList.add(neighbours[i]);
				}
				pseudoParents.put(nodeId, new ArrayList<Integer>());
				pseudoChildren.put(nodeId, pseudoChildrenList);
			}else if(children.size()==0)
			{
				//叶子节点
				List<Integer> pseudoParentList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					pseudoParentList.add(neighbours[i]);
				}
				pseudoParents.put(nodeId, pseudoParentList);
				pseudoChildren.put(nodeId, new ArrayList<Integer>());
			}else
			{
				//中间节点
				List<Integer> pseudoParentList=new ArrayList<Integer>();
				List<Integer> pseudoChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					if(neighbours[i]==parent)
					{
						pseudoParentList.add(neighbours[i]);
					}else if(CollectionUtil.exists(children, neighbours[i])!=-1)
					{
						pseudoChildrenList.add(neighbours[i]);
					}else
					{
						if(floor<this.nodeFloor.get(neighbours[i]))
						{
							//在本节点之下
							pseudoChildrenList.add(neighbours[i]);
						}else
						{
							//在本节点之上
							pseudoParentList.add(neighbours[i]);
						}
					}
				}
				pseudoParents.put(nodeId, pseudoParentList);
				pseudoChildren.put(nodeId, pseudoChildrenList);
			}
		}
		return new Map[]{CollectionUtil.transform(pseudoParents), CollectionUtil.transform(pseudoChildren)};
	}
}
