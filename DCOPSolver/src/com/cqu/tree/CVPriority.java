package com.cqu.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 查找具有割点属性的节点优先级
 * @author hz
 *
 */
public class CVPriority{
	
	public static final int MIN_PRIORITY=1000000;
	
	/**
	 * 深度优先搜索树
	 */
	private DFSTree dfsTree;
	
	/**
	 * 节点优先级，值小优先级大
	 */
	private Map<Integer, Integer> nodePrioritiesMap;
	
	/**
	 * 节点序号
	 */
	private Map<Integer, Integer> seqNumsMap;
	
	/**
	 * 节点子树通过回边连接的最早祖先节点
	 */
	private Map<Integer, Integer> oldestAncestorsMap;
	
	/**
	 * 子树节点数目
	 */
	private Map<Integer, Integer> subChildrenCountsMap;
	
	/**
	 * 去除割点后各非连通部分大小
	 */
	private Map<Integer, Map<Integer, Integer>> cvPartSizesMap;
	
	public CVPriority(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		nodePrioritiesMap=new HashMap<Integer, Integer>();
		seqNumsMap=new HashMap<Integer, Integer>();
		oldestAncestorsMap=new HashMap<Integer, Integer>();
		subChildrenCountsMap=new HashMap<Integer, Integer>();
		cvPartSizesMap=new HashMap<Integer, Map<Integer, Integer>>();
		
		for(Integer nodeId : neighbourNodes.keySet())
		{
			nodePrioritiesMap.put(nodeId, CVPriority.MIN_PRIORITY);
			seqNumsMap.put(nodeId, -1);
			oldestAncestorsMap.put(nodeId, -1);
			subChildrenCountsMap.put(nodeId, 0);
		}
		
		dfsTree=new DFSTree(neighbourNodes);
	}
	
	/**
	 * 开始
	 */
	public void start()
	{
	}
	
	/**
	 * 从下至上遍历树，收集割点信息
	 */
	private void collectCVInfo()
	{
		
	}
	
	/**
	 * 从上至下设置割点
	 */
	private void setCutVertex()
	{
		
	}

}
