package com.cqu.tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 割点引导的DFS树构造
 * @author hz
 *
 */
public class CVDFSTree extends DFSTree{
	
	/**
	 * 动态割点查找器
	 */
	private DynamicCutVertex dcv;
	
	public CVDFSTree(Map<Integer, int[]> neighborNodes) {
		super(neighborNodes);
		// TODO Auto-generated constructor stub
		dcv=new DynamicCutVertex(neighborNodes);
	}
	
	@Override
	public void generate() {
		// TODO Auto-generated method stub
		//初始化动态割点查找器
		dcv.init();
		
		//确定根节点
		this.rootId=this.getNextNodeByCutVertex(null);
		
		//由割点引导的DFS生成树构造
		Integer curLevel=0;
		Integer curNodeId=this.rootId;
		this.nodeIterated.add(curNodeId);
		this.parents.put(curNodeId, -1);
		this.levels.put(curNodeId, curLevel);
		
		while(true)
		{
			Integer nextNodeId=this.getNextNodeByCutVertex(curNodeId);
			if(nextNodeId==-1)
			{
				//遍历结束标志：回溯至根节点且无未遍历邻居节点
				if(curNodeId.equals(this.rootId))
				{
					break;
				}
				
				curLevel--;
				//回溯
				curNodeId=this.parents.get(curNodeId);
			}else
			{
				curLevel++;
				
				this.children.get(curNodeId).add(nextNodeId);
				this.parents.put(nextNodeId, curNodeId);
				
				this.nodeIterated.add(nextNodeId);
				this.levels.put(nextNodeId, curLevel);
				
				curNodeId=nextNodeId;
			}
		}
		
		calAllParentsAndAllChildren();
		calHeight();
	}
	
	private Integer getNextNodeByCutVertex(Integer nodeId)
	{
		Set<Integer> candidateNodes=new HashSet<Integer>();
		
		if(nodeId==null)
		{
			candidateNodes=this.neighbors.keySet();
		}else
		{
			int[] neighbors=this.neighbors.get(nodeId);
			for(int i=0;i<neighbors.length;i++)
			{
				if(this.nodeIterated.contains(neighbors[i])==false)
				{
					candidateNodes.add(neighbors[i]);
				}
			}
		}
		
		if(candidateNodes.isEmpty()==false)
		{
			if(candidateNodes.size()==1)
			{
				return candidateNodes.iterator().next();
			}else
			{
				return dcv.selectBest(nodeId, nodeIterated, candidateNodes);
			}
		}else
		{
			return -1;
		}
	}
}
