package com.cqu.tree;

import java.util.Map;

/**
 * 图遍历器
 * @author hz
 *
 */
public abstract class GraphIterator {
	
	/**
	 * 遍历状态
	 * @author hz
	 *
	 */
	public static enum IteratingStatus{
		/**
		 * 未开始
		 */
		INIT, 
		
		/**
		 * 遍历中
		 */
		ON, 
		
		/**
		 * 提前终止
		 */
		ENDEDAHEAD, 
		
		/**
		 * 正常结束
		 */
		FINISHED
	}
	
	/**
	 * 深度优先遍历
	 */
	public static final String ITERATING_TYPE_DFS="DFS";
	
	/**
	 * 广度优先遍历
	 */
	public static final String ITERATING_TYPE_BFS="BFS";
	
	/**
	 * 无向图（邻接表存储）
	 */
	protected Map<Integer, int[]> neighbors;
	
	/**
	 * 根节点
	 */
	protected Integer rootNodeId;
	
	/**
	 * 节点操作
	 */
	protected NodeOperation nodeOp;
	
	/**
	 * 遍历状态
	 */
	protected IteratingStatus itStatus; 
	
	public GraphIterator(Map<Integer, int[]> neighbors, Integer rootNodeId, NodeOperation nodeOp) {
		// TODO Auto-generated constructor stub
		this.neighbors=neighbors;
		this.rootNodeId=rootNodeId;
		this.nodeOp=nodeOp;
		
		this.itStatus=IteratingStatus.INIT;
	}
	
	/**
	 * 获取邻接表
	 * @return
	 */
	public Map<Integer, int[]> getNeighbors()
	{
		return this.neighbors;
	}
	
	/**
	 * 获取根节点
	 * @return
	 */
	public Integer getRoot()
	{
		return this.rootNodeId;
	}
	
	/**
	 * 提前终止遍历
	 */
	public void endAhead()
	{
		this.itStatus=IteratingStatus.ENDEDAHEAD;
	}
	
	/**
	 * 获取状态
	 * @return
	 */
	public IteratingStatus getStatus()
	{
		return this.itStatus;
	}
	
	/**
	 * 遍历
	 */
	public abstract void iterate();
}
