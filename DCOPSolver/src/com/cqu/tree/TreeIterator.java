package com.cqu.tree;

import java.util.Map;

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
	
	public TreeIterator(Map<Integer, Integer> parentNodesMap, Map<Integer, int[]> childrenNodesMap, Integer rootNodeId, NodeOperation nodeOp) {
		// TODO Auto-generated constructor stub
		this.parentNodesMap=parentNodesMap;
		this.childrenNodesMap=childrenNodesMap;
		this.rootNodeId=rootNodeId;
		this.nodeOp=nodeOp;
	}
	
	public abstract void iterate();
}
