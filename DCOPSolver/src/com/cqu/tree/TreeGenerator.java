package com.cqu.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;

/**
 * 生成树构造器
 * @author hz
 *
 */
public abstract class TreeGenerator {
	
	/**
	 * DFS生成树
	 */
	public static final String TREE_GENERATOR_TYPE_DFS="DFS";
	
	/**
	 * BFS生成树
	 */
	public static final String TREE_GENERATOR_TYPE_BFS="BFS";
	
	/**
	 * 无向图（邻接表存储）
	 */
	protected Map<Integer, int[]> neighbors;
	
	/**
	 * 父节点
	 */
	protected Map<Integer, Integer> parents;
	
	/**
	 * 父节点和伪父节点
	 */
	protected Map<Integer, List<Integer>> allParents;
	
	/**
	 * 子节点
	 */
	protected Map<Integer, List<Integer>> children;
	
	/**
	 * 子节点和伪子节点
	 */
	protected Map<Integer, List<Integer>> allChildren;
	
	/**
	 * 节点层次，根节点为0层
	 */
	protected Map<Integer, Integer> levels;
	
	/**
	 * 树高
	 */
	protected long height=0;
	
	/**
	 * 根节点
	 */
	protected Integer rootId=-1;
	
	public TreeGenerator(Map<Integer, int[]> neighbors)
	{
		this.neighbors=neighbors;
		
		this.parents=new HashMap<Integer, Integer>();
		this.allParents=new HashMap<Integer, List<Integer>>();
		this.children=new HashMap<Integer, List<Integer>>();
		this.allChildren=new HashMap<Integer, List<Integer>>();
		this.levels=new HashMap<Integer, Integer>();
		
		for(Integer nodeId : this.neighbors.keySet())
		{
			this.allParents.put(nodeId, new ArrayList<Integer>());
			this.children.put(nodeId, new ArrayList<Integer>());
			this.allChildren.put(nodeId, new ArrayList<Integer>());
		}
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
		return this.rootId;
	}
	
	/**
	 * 构造生成树
	 */
	public abstract void generate();
	
	/**
	 * 获取父节点
	 * @return
	 */
	public Map<Integer, Integer> getParents()
	{
		return this.parents;
	}
	
	/**
	 * 获取父节点和伪父节点
	 * @return
	 */
	public Map<Integer, int[]> getAllParents()
	{
		return CollectionUtil.transform(this.allParents);
	}
	
	/**
	 * 获取子节点
	 * @return
	 */
	public Map<Integer, int[]> getChildren()
	{
		return CollectionUtil.transform(this.children);
	}
	
	/**
	 * 获取子节点和伪子节点
	 * @return
	 */
	public Map<Integer, int[]> getAllChildren()
	{
		return CollectionUtil.transform(this.allChildren);
	}
	
	/**
	 * 获取层次
	 * @return
	 */
	public Map<Integer, Integer> getLevels()
	{
		return this.levels;
	}
	
	/**
	 * 获取高度
	 * @return
	 */
	public long getHeight()
	{
		return this.height;
	}
	
}
