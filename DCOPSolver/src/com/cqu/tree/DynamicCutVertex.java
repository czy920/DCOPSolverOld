package com.cqu.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cqu.util.CollectionUtil;

/**
 * 动态割点
 * @author hz
 *
 */
public class DynamicCutVertex {
	
	public static final int INVALID_ANCESTOR_MIN=-1;
	public static final int INVALID_ANCESTOR_MAX=1000000;
	
	/**
	 * 无向图（邻接表存储）
	 */
	private Map<Integer, int[]> neighbors;
	
	/**
	 * DFS生成树
	 */
	private DFSTree dfsTree;
	
	/**
	 * 节点子树通过回边连接的最早祖先节点
	 */
	private Map<Integer, Integer> oldestAncestorLevelMap;
	
	/**
	 * 子树节点数目
	 */
	private Map<Integer, Integer> subTreeSizesMap;
	
	/**
	 * 去除割点后各非连通部分大小
	 */
	private Map<Integer, Map<Integer, Integer>> cvCutPartsSizesMap;
	
	/**
	 * 父节点分支大小
	 */
	private int parentPartSize;
	
	public DynamicCutVertex(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		this.neighbors=neighbourNodes;
	}
	
	/**
	 * 初始化
	 */
	public void init()
	{
		dfsTree=new DFSTree(this.neighbors);
		dfsTree.generate();
	}
	
	/**
	 * 割点引导，选出最佳节点
	 * @param blockingNodes 阻断节点
	 * @param candidateNodes 候选节点
	 * @return
	 */
	public Integer selectBest(Integer root, Set<Integer> blockingNodes, Set<Integer> candidateNodes)
	{
		oldestAncestorLevelMap=new HashMap<Integer, Integer>();
		subTreeSizesMap=new HashMap<Integer, Integer>();
		cvCutPartsSizesMap=new HashMap<Integer, Map<Integer, Integer>>();
		
		if(root==null)
		{
			root=dfsTree.getRoot();
		}
		collectCVInfo(root, blockingNodes);
		
		if(cvCutPartsSizesMap.size()<=1)
		{
			//无割点，随机选择第一个节点;或则有唯一割点，直接选择它即可
			return candidateNodes.iterator().next();
		}
		Map<Integer, Double> cutVertexEvaluations=new HashMap<Integer, Double>();
		for(Integer nodeId : candidateNodes)
		{
			if(cvCutPartsSizesMap.containsKey(nodeId)==false)
			{
				continue;
			}
			
			Map<Integer, Integer> partsSizes=cvCutPartsSizesMap.get(nodeId);
			double sum=0.0;
			for(Integer value : partsSizes.values())
			{
				sum+=value;
			}
			
			double minRatio=2;
			for(Integer key : partsSizes.keySet())
			{
				double ratio=Math.abs(partsSizes.get(key)/sum-0.5);
				if(minRatio>ratio)
				{
					minRatio=ratio;
				}
			}
			cutVertexEvaluations.put(nodeId, minRatio);
		}
		return CollectionUtil.min(cutVertexEvaluations);
	}
	
	/**
	 * 从下至上遍历树，收集割点信息
	 */
	private void collectCVInfo(final Integer root, final Set<Integer> blockingNodes)
	{
		new TreeIteratorBlockingReverseBFS(dfsTree.getParents(), dfsTree.getChildren(), root, new NodeOperation() {
			
			@Override
			public void operate(Integer curNodeId) {
				// TODO Auto-generated method stub
				//有效子分支数量
				int validChildPartsCount=0;
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					if(blockingNodes.contains(e)==false)
					{
						validChildPartsCount++;
					}
				}
				//父分支是否有效
				boolean isParentPartValid=false;
				for(Integer e : dfsTree.getAllParents().get(curNodeId))
				{
					if(blockingNodes.contains(e)==false)
					{
						isParentPartValid=true;
						break;
					}
				}
				
				//子树大小
				int subTreeSize=0;
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					if(blockingNodes.contains(e)==false)
					{
						subTreeSize+=subTreeSizesMap.get(e);
					}
				}
				subTreeSizesMap.put(curNodeId, subTreeSize+1);//须加上自己
				
				//通过回边连接的最高祖先-除本节点之外的子节点
				List<Integer> ancestorNodes=new ArrayList<Integer>();
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					if(blockingNodes.contains(e)==false)
					{
						ancestorNodes.add(oldestAncestorLevelMap.get(e));
					}
				}
				if(ancestorNodes.isEmpty())
				{
					ancestorNodes.add(DynamicCutVertex.INVALID_ANCESTOR_MAX);
				}
				oldestAncestorLevelMap.put(curNodeId, Collections.min(ancestorNodes));
				
				//判定割点
				if(oldestAncestorLevelMap.get(curNodeId)>=dfsTree.getLevels().get(curNodeId))
				{
					if(validChildPartsCount>=2||(validChildPartsCount==1&&isParentPartValid==true))
					{
						Map<Integer, Integer> cutPartsSizes=new HashMap<Integer, Integer>();
						//下部子分支大小
						for(Integer e : dfsTree.getChildren().get(curNodeId))
						{
							if(blockingNodes.contains(e)==false)
							{
								cutPartsSizes.put(e, subTreeSizesMap.get(e));
							}
						}
						
						//上部祖先分支大小
						if(isParentPartValid==true)
						{
							parentPartSize=0;
							Set<Integer> blockingNodesSetTemp=new HashSet<Integer>(blockingNodes);
							int[] nodeAllChildren=dfsTree.getAllChildren().get(curNodeId);
							for(int childNodeId : nodeAllChildren)
							{
								blockingNodesSetTemp.add(childNodeId);
							}
							new GraphIteratorBlockingDFS(neighbors, curNodeId, new NodeOperation() {
								
								@Override
								public void operate(Integer curNodeId) {
									// TODO Auto-generated method stub
									parentPartSize++;
								}
							}, blockingNodesSetTemp).iterate();
							cutPartsSizes.put(curNodeId, parentPartSize-1);//减一是排除当前节点
						}
						cvCutPartsSizesMap.put(curNodeId, cutPartsSizes);
					}
				}
				
				//通过回边连接的最高祖先-本节点
				if(dfsTree.getAllParents().get(curNodeId).length>1)
				{
					Integer parent=dfsTree.getParents().get(curNodeId);
					for(Integer e : dfsTree.getAllParents().get(curNodeId))
					{
						if(parent.equals(e)==false)
						{
							if(blockingNodes.contains(e)==false)
							{
								ancestorNodes.add(dfsTree.getLevels().get(e));
							}
						}
					}
					oldestAncestorLevelMap.put(curNodeId, Collections.min(ancestorNodes));
				}
			}
		}, blockingNodes).iterate();
	}

}
