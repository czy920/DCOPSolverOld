package org.cqu.structure.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.gui.settings.SettingsPersistent;
import org.cqu.utility.CollectionUtil;
import org.cqu.utility.StatisticUtil;


public class DFSTree implements TreeGenerator{
	
	public final static String[] heuristics=new String[]{"random", "max_degree", "min_degree"};
	
	private Map<Integer, int[]> neighbourNodes;//无向图（邻接表存储）
	
	private Map<Integer, List<Integer>> childrenNodes;//生成树子节点
	private Map<Integer, Integer> parentNode;//生成树父节点
	private Map<Integer, Integer> nodeLayers;//节点层次，根节点为0层
	/*private long pseduHeight=0;*/
	

	private Map<Integer, Boolean> nodeIterated;
	private Map<Integer, int[]> neighbourCounts;
	
	private Integer rootId=-1;
	
	public DFSTree(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		this.neighbourNodes=neighbourNodes;
		
		this.childrenNodes=new HashMap<Integer, List<Integer>>();
		this.parentNode=new HashMap<Integer, Integer>();
		this.nodeLayers=new HashMap<Integer, Integer>();
		
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
		int maxIndex=StatisticUtil.max(counts);
		if(counts[maxIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbours[maxIndex];
		}
	}
	
	private Integer getMinNeighboursNodeId(Integer nodeId)
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
		int minIndex=StatisticUtil.min(counts);
		if(counts[minIndex]==-1)
		{
			return -1;
		}else
		{
			return neighbours[minIndex];
		}
	}
	
	private Integer getRandomNodeId(Integer nodeId)
	{
		int[] neighbours=this.neighbourNodes.get(nodeId);
		for(int i=0;i<neighbours.length;i++)
		{
			if(this.nodeIterated.get(neighbours[i])==false)
			{
				return neighbours[i];
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
		this.parentNode.put(curNodeId, -1);
		this.nodeLayers.put(curNodeId, curLevel);
		
		int totalCount=neighbourNodes.size();
		while(iteratedCount<totalCount)
		{
			Integer nextNodeId=-1;
			if(SettingsPersistent.settings.getDfsHeuristics().equals("random"))
			{
				nextNodeId=this.getRandomNodeId(curNodeId);
			}else if(SettingsPersistent.settings.getDfsHeuristics().equals("max_degree"))
			{
				nextNodeId=this.getMaxNeighboursNodeId(curNodeId);
			}else if(SettingsPersistent.settings.getDfsHeuristics().equals("min_degree"))
			{
				nextNodeId=this.getMinNeighboursNodeId(curNodeId);
			}
			if(nextNodeId==-1)
			{
				curLevel--;
				//回溯
				curNodeId=this.parentNode.get(curNodeId);
			}else
			{
				curLevel++;
				
				this.childrenNodes.get(curNodeId).add(nextNodeId);
				this.parentNode.put(nextNodeId, curNodeId);
				
				this.nodeIterated.put(nextNodeId, true);
				this.nodeLayers.put(nextNodeId, curLevel);
				iteratedCount++;
				
				curNodeId=nextNodeId;
			}
		}
	}
	
	@Override
	public Map<Integer, Integer> getNodeLayers() {
		// TODO Auto-generated method stub
		return this.nodeLayers;
	}
	
	/*public long getPseduHeight() {
		Integer curNodeId=this.rootId;		
		boolean link=true;
		while(link==true&&curNodeId!=(-1))
		{
			if(this.childrenNodes.get(curNodeId).size()>1){
				link=false;
			}else {
				this.pseduHeight++;
				if(this.childrenNodes.get(curNodeId).size()==1){
					curNodeId=this.childrenNodes.get(curNodeId).iterator().next();
				}
				if(this.childrenNodes.get(curNodeId).size()==0){
					break;
				}
			}
		}
		return pseduHeight;
	}*/

	@Override
	public Map<Integer, Integer> getParentNode() {
		// TODO Auto-generated method stub
		return this.parentNode;
	}

	@Override
	public Map<Integer, int[]> getChildrenNodes() {
		// TODO Auto-generated method stub
		return CollectionUtil.transform(childrenNodes);
	}

	@Override
	public Map<Integer, int[]> getAllChildren() {
		// TODO Auto-generated method stub
		Map<Integer, List<Integer>> allChildren=new HashMap<Integer, List<Integer>>();
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int[] neighbours=this.neighbourNodes.get(nodeId);
			List<Integer> children=this.childrenNodes.get(nodeId);
			Integer parent=this.parentNode.get(nodeId);
			Integer level=this.nodeLayers.get(nodeId);
			if(parent==-1)
			{
				//根节点
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					allChildrenList.add(neighbours[i]);
				}
				allChildren.put(nodeId, allChildrenList);
			}else if(children.size()==0)
			{
				//叶子节点
				List<Integer> allParentList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					allParentList.add(neighbours[i]);
				}
				allChildren.put(nodeId, new ArrayList<Integer>());
			}else
			{
				//中间节点
				List<Integer> allParentList=new ArrayList<Integer>();
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					if(neighbours[i]==parent)
					{
						allParentList.add(neighbours[i]);
					}else if(CollectionUtil.indexOf(children, neighbours[i])!=-1)
					{
						allChildrenList.add(neighbours[i]);
					}else
					{
						if(level<this.nodeLayers.get(neighbours[i]))
						{
							//在本节点之下
							allChildrenList.add(neighbours[i]);
						}else
						{
							//在本节点之上
							allParentList.add(neighbours[i]);
						}
					}
				}
				allChildren.put(nodeId, allChildrenList);
			}
		}
		return CollectionUtil.transform(allChildren);
	}

	@Override
	public Map<Integer, int[]> getAllParents() {
		// TODO Auto-generated method stub
		Map<Integer, List<Integer>> allParents=new HashMap<Integer, List<Integer>>();
		for(Integer nodeId : this.neighbourNodes.keySet())
		{
			int[] neighbours=this.neighbourNodes.get(nodeId);
			List<Integer> children=this.childrenNodes.get(nodeId);
			Integer parent=this.parentNode.get(nodeId);
			Integer level=this.nodeLayers.get(nodeId);
			if(parent==-1)
			{
				//根节点
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					allChildrenList.add(neighbours[i]);
				}
				allParents.put(nodeId, new ArrayList<Integer>());
			}else if(children.size()==0)
			{
				//叶子节点
				List<Integer> allParentList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					allParentList.add(neighbours[i]);
				}
				allParents.put(nodeId, allParentList);
			}else
			{
				//中间节点
				List<Integer> allParentList=new ArrayList<Integer>();
				List<Integer> allChildrenList=new ArrayList<Integer>();
				for(int i=0;i<neighbours.length;i++)
				{
					if(neighbours[i]==parent)
					{
						allParentList.add(neighbours[i]);
					}else if(CollectionUtil.indexOf(children, neighbours[i])!=-1)
					{
						allChildrenList.add(neighbours[i]);
					}else
					{
						if(level<this.nodeLayers.get(neighbours[i]))
						{
							//在本节点之下
							allChildrenList.add(neighbours[i]);
						}else
						{
							//在本节点之上
							allParentList.add(neighbours[i]);
						}
					}
				}
				allParents.put(nodeId, allParentList);
			}
		}
		return CollectionUtil.transform(allParents);
	}
	
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
