package com.cqu.core;

import java.util.Map;

public interface TreeGenerator {
	
	public static final String TREE_GENERATOR_TYPE_DFS="DFS";
	public static final String TREE_GENERATOR_TYPE_BFS="BFS";
	
	void generate();
	Map<Integer, int[]> getChildrenNodes();
	Map<Integer, Integer> getParentNode();
	public long getPseduHeight();
	
	/**
	 * 层次
	 * @return
	 */
	Map<Integer, Integer> getNodeLevels();
	
	/**
	 * allChildren包含children和pseudoChildren, allParents包含parent和pseudoParents
	 * @return Map<Integer, int[]>[2]{allParents, allChildren}
	 */
	@SuppressWarnings("rawtypes")
	Map[] getAllChildrenAndParentNodes();
}
