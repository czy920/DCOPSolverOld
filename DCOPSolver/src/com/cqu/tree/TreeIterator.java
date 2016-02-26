package com.cqu.tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 树或子树遍历器
 * @author hz
 *
 */
public abstract class TreeIterator {
	
	/**
	 * 根节点
	 */
	protected Integer rootNodeId;
	
	/**
	 * 树子节点
	 */
	protected Map<Integer, int[]> childrenNodesMap;
	
	/**
	 * 树父节点
	 */
	protected Map<Integer, Integer> parentNodesMap;
	
	/**
	 * 节点操作
	 */
	protected NodeOperation nodeOp;
	
	/**
	 * 遍历状态
	 */
	protected IteratingStatus itStatus; 
	
	/**
	 * 记录是否遍历过
	 */
	protected Set<Integer> nodesIterated;
	
	public TreeIterator(Map<Integer, Integer> parentNodesMap, Map<Integer, int[]> childrenNodesMap, Integer rootNodeId, NodeOperation nodeOp) {
		// TODO Auto-generated constructor stub
		this.parentNodesMap=parentNodesMap;
		this.childrenNodesMap=childrenNodesMap;
		this.rootNodeId=rootNodeId;
		this.nodeOp=nodeOp;
		
		this.nodesIterated=new HashSet<Integer>();
		this.itStatus=IteratingStatus.INIT;
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
	
	public abstract void iterate();
}
