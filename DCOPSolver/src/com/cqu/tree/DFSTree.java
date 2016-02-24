package com.cqu.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;
import com.cqu.util.StatisticUtil;

/**
 * 构造DFS生成树
 * @author hz
 *
 */
public class DFSTree extends TreeGenerator{
	
	/**
	 * DFS构造生成树，随机选择下一节点
	 */
	public final static String HEURISTIC_RANDOM="RANDOM";
	
	/**
	 * DFS构造生成树，优先选择度数大的作为下一节点
	 */
	public final static String HEURISTIC_MAXDEGREE="MAXDEGREE";
	
	/**
	 * DFS构造生成树，优先选择度数小的作为下一节点
	 */
	public final static String HEURISTIC_MINDEGREE="MINDEGREE";
	
	private String heuristic;

	private Map<Integer, Boolean> nodeIterated;
	private Map<Integer, int[]> neighborCounts;
	
	public DFSTree(Map<Integer, int[]> neighbors) {
		super(neighbors);
		// TODO Auto-generated constructor stub
		
		this.nodeIterated=new HashMap<Integer, Boolean>();
		this.neighborCounts=new HashMap<Integer, int[]>();
		for(Integer nodeId : this.neighbors.keySet())
		{
			this.nodeIterated.put(nodeId, false);
			
			int[] nodeNeighbors=this.neighbors.get(nodeId);
			int[] nodeNeighborCounts=new int[nodeNeighbors.length];
			for(int i=0;i<nodeNeighborCounts.length;i++)
			{
				nodeNeighborCounts[i]=this.neighbors.get(nodeNeighbors[i]).length;
			}
			this.neighborCounts.put(nodeId, nodeNeighborCounts);
		}
		
		this.rootId=this.neighbors.keySet().iterator().next();
		this.heuristic=DFSTree.HEURISTIC_RANDOM;
	}
	
	/**
	 * 设置选择下一节点的指导策略
	 * @param heuristic
	 */
	public void setHeuristic(String heuristic)
	{
		this.heuristic=heuristic;
	}
	
	private Integer getMaxNeighborsNodeId(Integer nodeId)
	{
		int[] neighbors=this.neighbors.get(nodeId);
		int[] counts=this.neighborCounts.get(nodeId);
		for(int i=0;i<counts.length;i++)
		{
			if(this.nodeIterated.get(neighbors[i])==true)
			{
				counts[i]=-1;
			}
		}
		int maxIndex=StatisticUtil.max(counts);
		if(counts[maxIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbors[maxIndex];
		}
	}
	
	private Integer getMinNeighborsNodeId(Integer nodeId)
	{
		int[] neighbors=this.neighbors.get(nodeId);
		int[] counts=this.neighborCounts.get(nodeId);
		for(int i=0;i<counts.length;i++)
		{
			if(this.nodeIterated.get(neighbors[i])==true)
			{
				counts[i]=-1;
			}
		}
		int minIndex=StatisticUtil.min(counts);
		if(counts[minIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbors[minIndex];
		}
	}
	
	private Integer getRandomNodeId(Integer nodeId)
	{
		int[] neighbors=this.neighbors.get(nodeId);
		for(int i=0;i<neighbors.length;i++)
		{
			if(this.nodeIterated.get(neighbors[i])==false)
			{
				return neighbors[i];
			}
		}
		return -1;
	}

	@Override
	public void generate() {
		// TODO Auto-generated method stub
		int iteratedCount=0;
		Integer curLevel=0;
		
		Integer curNodeId=this.rootId;
		this.nodeIterated.put(curNodeId, true);
		iteratedCount++;//根节点已遍历
		this.parents.put(curNodeId, -1);
		this.levels.put(curNodeId, curLevel);
		
		int totalCount=neighbors.size();
		while(iteratedCount<totalCount)
		{
			Integer nextNodeId=-1;
			if(this.heuristic.equals(DFSTree.HEURISTIC_RANDOM))
			{
				nextNodeId=this.getRandomNodeId(curNodeId);
			}else if(this.heuristic.equals(DFSTree.HEURISTIC_MAXDEGREE))
			{
				nextNodeId=this.getMaxNeighborsNodeId(curNodeId);
			}else if(this.heuristic.equals(DFSTree.HEURISTIC_MINDEGREE))
			{
				nextNodeId=this.getMinNeighborsNodeId(curNodeId);
			}
			if(nextNodeId==-1)
			{
				curLevel--;
				//回溯
				curNodeId=this.parents.get(curNodeId);
			}else
			{
				curLevel++;
				
				this.children.get(curNodeId).add(nextNodeId);
				this.parents.put(nextNodeId, curNodeId);
				
				this.nodeIterated.put(nextNodeId, true);
				this.levels.put(nextNodeId, curLevel);
				iteratedCount++;
				
				curNodeId=nextNodeId;
			}
		}
		
		calAllParentsAndAllChildren();
		calHeight();
	}
	
	private void calHeight() {
		Integer curNodeId=this.rootId;		
		boolean link=true;
		while(link==true&&curNodeId!=(-1))
		{
			if(this.children.get(curNodeId).size()>1){
				link=false;
			}else {
				this.height++;
				if(this.children.get(curNodeId).size()==1){
					curNodeId=this.children.get(curNodeId).iterator().next();
				}
				if(this.children.get(curNodeId).size()==0){
					break;
				}
			}
		}
	}

	private void calAllParentsAndAllChildren() {
		// TODO Auto-generated method stub
		for(Integer nodeId : this.neighbors.keySet())
		{
			int[] neighbors=this.neighbors.get(nodeId);
			List<Integer> children=this.children.get(nodeId);
			Integer parent=this.parents.get(nodeId);
			Integer level=this.levels.get(nodeId);
			if(parent==-1)
			{
				//根节点
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbors.length;i++)
				{
					allChildrenList.add(neighbors[i]);
				}
				allParents.put(nodeId, new ArrayList<Integer>());
				allChildren.put(nodeId, allChildrenList);
			}else if(children.size()==0)
			{
				//叶子节点
				List<Integer> allParentList=new ArrayList<Integer>();
				for(int i=0;i<neighbors.length;i++)
				{
					allParentList.add(neighbors[i]);
				}
				allParents.put(nodeId, allParentList);
				allChildren.put(nodeId, new ArrayList<Integer>());
			}else
			{
				//中间节点
				List<Integer> allParentList=new ArrayList<Integer>();
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbors.length;i++)
				{
					if(neighbors[i]==parent)
					{
						allParentList.add(neighbors[i]);
					}else if(CollectionUtil.indexOf(children, neighbors[i])!=-1)
					{
						allChildrenList.add(neighbors[i]);
					}else
					{
						if(level<this.levels.get(neighbors[i]))
						{
							//在本节点之下
							allChildrenList.add(neighbors[i]);
						}else
						{
							//在本节点之上
							allParentList.add(neighbors[i]);
						}
					}
				}
				allParents.put(nodeId, allParentList);
				allChildren.put(nodeId, allChildrenList);
			}
		}
	}
	
	/**
	 * 返回DFS伪树的字符串序列表示
	 * @param agentNames
	 * @param parentAgents
	 * @param childAgents
	 * @return
	 */
	public static String toTreeString(Map<Integer, String> agentNames, Map<Integer, Integer> parentAgents, Map<Integer, int[]> childAgents)
	{
		Integer rootId=-1;
		for(Integer nodeId : parentAgents.keySet())
		{
			if(parentAgents.get(nodeId)==-1)
			{
				rootId=nodeId;
				break;
			}
		}
		String treeString=getNodeString(rootId, agentNames, childAgents);
		return "["+treeString+"]";
	}
	
	private static String getNodeString(Integer nodeId, Map<Integer, String> agentNames, Map<Integer, int[]> childAgents)
	{
		int[] children=childAgents.get(nodeId);
		if(children!=null&&children.length>0)
		{
			String str="{";
			str+=agentNames.get(nodeId);
			str+="; ";
			for(int i=0;i<children.length-1;i++)
			{
				str+=getNodeString(children[i], agentNames, childAgents);
				str+="; ";
			}
			str+=getNodeString(children[children.length-1], agentNames, childAgents);
			str+="}";
			return str;
		}else
		{
			return "{"+agentNames.get(nodeId)+"}";
		}
	}
}
