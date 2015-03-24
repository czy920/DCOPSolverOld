package org.cqu.structure.tree;

import java.util.Map;

public interface TreeGenerator {
	
	public static enum PseudoTreeType
	{
		DFS, BFS
	}
	
	void generate();
	Map<Integer, int[]> getChildrenNodes();
	Map<Integer, Integer> getParentNode();
	Map<Integer, Integer> getNodeLayers();
	/**
	 * allChildren包含children和pseudoChildren
	 * @return
	 */
	Map<Integer, int[]> getAllChildren();
	/**
	 * allParents包含parent和pseudoParents
	 * @return
	 */
	Map<Integer, int[]> getAllParents();
}
