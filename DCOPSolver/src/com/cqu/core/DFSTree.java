package com.cqu.core;

import java.util.List;
import java.util.Map;

public class DFSTree {
	
	private Map<Integer, int[]> neighbourNodes;//无向图（邻接表存储）
	private Map<Integer, List<Integer>> childrenNodes;//生成树
	
	public DFSTree(Map<Integer, int[]> neighbourNodes) {
		// TODO Auto-generated constructor stub
		this.neighbourNodes=neighbourNodes;
	}
	
	public void generate()
	{
		
	}
}
