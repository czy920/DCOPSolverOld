package com.cqu.core;

import java.util.Map;

public interface DFSTreeGenerator {
	
	void generate();
	Map<Integer, int[]> getChildrenNodes();
	Map<Integer, Integer> getParentNode();
	
	/**
	 * pseudoChildren包含children, pseudoParents包含parent
	 * @return Map<Integer, int[]>[2]{pseudoParents, pseudoChildren}
	 */
	@SuppressWarnings("rawtypes")
	Map[] getPseudoChildrenAndParentNodes();
}
