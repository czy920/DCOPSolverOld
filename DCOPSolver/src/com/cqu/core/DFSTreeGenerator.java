package com.cqu.core;

import java.util.Map;

public interface DFSTreeGenerator {
	
	void generate();
	Map<Integer, int[]> getChildrenNodes();
	Map<Integer, Integer> getParentNode();
	
	/**
	 * allChildren包含children和pseudoChildren, allParents包含parent和pseudoParents
	 * @return Map<Integer, int[]>[2]{allParents, allChildren}
	 */
	@SuppressWarnings("rawtypes")
	Map[] getAllChildrenAndParentNodes();
}