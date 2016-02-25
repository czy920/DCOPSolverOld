package com.cqu.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 查找具有割点属性的节点优先级
 * @author hz
 *
 */
public class CVPriority extends TreeGenerator{
	
	public static final int MIN_PRIORITY=1000000;
	public static final int MAX_PRIORITY=0;
	public static final int INVALID_ANCESTOR=1000000;
	
	/**
	 * DFS生成树
	 */
	private DFSTree dfsTree;
	
	/**
	 * 节点优先级，值小优先级大
	 */
	private Map<Integer, Integer> nodePrioritiesMap;
	
	/**
	 * 节点层次
	 */
	private Map<Integer, Integer> levels;
	
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
	 * 节点上方大小
	 */
	private int parentPartSize;
	
	public CVPriority(Map<Integer, int[]> neighbourNodes) {
		super(neighbourNodes);
		// TODO Auto-generated constructor stub
		nodePrioritiesMap=new HashMap<Integer, Integer>();
		oldestAncestorLevelMap=new HashMap<Integer, Integer>();
		subTreeSizesMap=new HashMap<Integer, Integer>();
		cvCutPartsSizesMap=new HashMap<Integer, Map<Integer, Integer>>();
		
		for(Integer nodeId : neighbourNodes.keySet())
		{
			nodePrioritiesMap.put(nodeId, CVPriority.MIN_PRIORITY);
			oldestAncestorLevelMap.put(nodeId, CVPriority.INVALID_ANCESTOR);
			subTreeSizesMap.put(nodeId, 0);
		}
	}
	
	@Override
	public void generate() {
		// TODO Auto-generated method stub
		//初始dfs生成树
		dfsTree=new DFSTree(this.neighbors);
		dfsTree.generate();
		levels=new HashMap<Integer, Integer>(dfsTree.getLevels());
		
		collectCVInfo();
		setCutVertex();
	}
	
	/**
	 * 从下至上遍历树，收集割点信息
	 */
	private void collectCVInfo()
	{
		new TreeIteratorBT(dfsTree.getParents(), dfsTree.getChildren(), dfsTree.getRoot(), new NodeOperation() {
			
			@Override
			public void operate(Integer curNodeId) {
				// TODO Auto-generated method stub
				//排除已处理节点
				if(nodePrioritiesMap.containsKey(curNodeId))
				{
					return;
				}
				
				//子树大小
				int subTreeSize=0;
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					if(nodePrioritiesMap.containsKey(e)==false)
					{
						subTreeSize+=subTreeSizesMap.get(e);
					}
				}
				subTreeSizesMap.put(curNodeId, subTreeSize+1);//须加上自己
				
				
				//通过回边连接的最高祖先
				List<Integer> ancestorNodes=new ArrayList<Integer>();
				Integer parent=dfsTree.getParents().get(curNodeId);
				for(Integer e : dfsTree.getAllParents().get(curNodeId))
				{
					if(parent.equals(e)==false)
					{
						if(nodePrioritiesMap.containsKey(e)==false)
						{
							ancestorNodes.add(levels.get(e));
						}
					}
				}
				for(Integer e : dfsTree.getChildren().get(curNodeId))
				{
					if(nodePrioritiesMap.containsKey(e)==false)
					{
						ancestorNodes.add(oldestAncestorLevelMap.get(e));
					}
				}
				oldestAncestorLevelMap.put(curNodeId, Collections.min(ancestorNodes));
				
				//判定割点
				if(oldestAncestorLevelMap.get(curNodeId)<=levels.get(curNodeId))
				{
					Map<Integer, Integer> cutPartsSizes=new HashMap<Integer, Integer>();
					//子分支大小
					for(Integer e : dfsTree.getChildren().get(curNodeId))
					{
						cutPartsSizes.put(e, subTreeSizesMap.get(e));
					}
					
					//节点上方大小
					Integer parentId=dfsTree.getParents().get(curNodeId);
					parentPartSize=0;
					Set<Integer> blockingNodesSet=new HashSet<Integer>(nodePrioritiesMap.keySet());
					blockingNodesSet.add(curNodeId);
					new GraphIteratorBlockingDFS(neighbors, parentId, new NodeOperation() {
						
						@Override
						public void operate(Integer curNodeId) {
							// TODO Auto-generated method stub
							parentPartSize++;
						}
					}, blockingNodesSet).iterate();
					cutPartsSizes.put(parentId, parentPartSize);
					cvCutPartsSizesMap.put(curNodeId, cutPartsSizes);
				}
				
				//选择最佳割点
				if(curNodeId.equals(rootId))
				{
					selectBestCV();
				}
			}
		}).iterate();
	}
	
	/**
	 * 选择最佳割点
	 */
	private void selectBestCV()
	{
		
	}
	
	/**
	 * 从上至下设置割点
	 */
	private void setCutVertex()
	{
		new TreeIteratorTB(dfsTree.getParents(), dfsTree.getChildren(), dfsTree.getRoot(), new NodeOperation() {
			
			@Override
			public void operate(Integer curNodeId) {
				// TODO Auto-generated method stub
				//排除已处理节点
				if(nodePrioritiesMap.containsKey(curNodeId))
				{
					return;
				}
				
				
			}
		}).iterate();
	}

}
