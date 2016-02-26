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
	
	public static final int INVALID_ANCESTOR=1000000;
	
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
		oldestAncestorLevelMap=new HashMap<Integer, Integer>();
		subTreeSizesMap=new HashMap<Integer, Integer>();
		
		for(Integer nodeId : neighbors.keySet())
		{
			oldestAncestorLevelMap.put(nodeId, DynamicCutVertex.INVALID_ANCESTOR);
			subTreeSizesMap.put(nodeId, 0);
		}
		
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
		cvCutPartsSizesMap=new HashMap<Integer, Map<Integer, Integer>>();
		collectCVInfo(root, blockingNodes);
		
		Map<Integer, Double> cutVertexEvaluations=new HashMap<Integer, Double>();
		for(Integer nodeId : candidateNodes)
		{
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
				//子树大小
				int subTreeSize=0;
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					subTreeSize+=subTreeSizesMap.get(e);
				}
				subTreeSizesMap.put(curNodeId, subTreeSize+1);//须加上自己
				
				
				//通过回边连接的最高祖先
				List<Integer> ancestorNodes=new ArrayList<Integer>();
				Integer parent=dfsTree.getParents().get(curNodeId);
				for(Integer e : dfsTree.getAllParents().get(curNodeId))
				{
					if(parent.equals(e)==false)
					{
						ancestorNodes.add(dfsTree.getLevels().get(e));
					}
				}
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					ancestorNodes.add(oldestAncestorLevelMap.get(e));
				}
				oldestAncestorLevelMap.put(curNodeId, Collections.min(ancestorNodes));
				
				//判定割点
				if(oldestAncestorLevelMap.get(curNodeId)<=dfsTree.getLevels().get(curNodeId))
				{
					Map<Integer, Integer> cutPartsSizes=new HashMap<Integer, Integer>();
					//子分支大小
					for(Integer e : dfsTree.getChildren().get(curNodeId))
					{
						cutPartsSizes.put(e, subTreeSizesMap.get(e));
					}
					//父分支大小
					Integer parentId=dfsTree.getParents().get(curNodeId);
					parentPartSize=0;
					Set<Integer> blockingNodesSetTemp=new HashSet<Integer>(blockingNodes);
					blockingNodesSetTemp.add(curNodeId);
					new GraphIteratorBlockingDFS(neighbors, parentId, new NodeOperation() {
						
						@Override
						public void operate(Integer curNodeId) {
							// TODO Auto-generated method stub
							parentPartSize++;
						}
					}, blockingNodesSetTemp).iterate();
					cutPartsSizes.put(parentId, parentPartSize);
					cvCutPartsSizesMap.put(curNodeId, cutPartsSizes);
				}
			}
		}, blockingNodes).iterate();
	}

}
