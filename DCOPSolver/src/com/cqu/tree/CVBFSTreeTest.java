package com.cqu.tree;

import java.util.HashMap;
import java.util.Map;

import com.cqu.visualtree.GraphFrame;
import com.cqu.visualtree.TreeFrame;

public class CVBFSTreeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<Integer, int[]> neighbors=new HashMap<Integer, int[]>();
		neighbors.put(1, new int[]{2, 3, 6, 7, 8});
		neighbors.put(2, new int[]{1, 4, 5});
		neighbors.put(3, new int[]{1, 6, 7});
		neighbors.put(4, new int[]{2, 8, 9});
		neighbors.put(5, new int[]{2});
		neighbors.put(6, new int[]{1, 3, 10, 12});
		neighbors.put(7, new int[]{1, 3});
		neighbors.put(8, new int[]{1, 4});
		neighbors.put(9, new int[]{4});
		neighbors.put(10, new int[]{6, 11, 12});
		neighbors.put(11, new int[]{10});
		neighbors.put(12, new int[]{6, 10});
		
		CVBFSTree cvbfsTree=new CVBFSTree(neighbors);
		cvbfsTree.generate();
		
		GraphFrame graphFrame=new GraphFrame(neighbors);
		graphFrame.showGraphFrame();
		
		Map<Integer, String> names=new HashMap<Integer, String>();
		for(Integer nodeId : neighbors.keySet())
		{
			names.put(nodeId, "A"+nodeId);
		}
		TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(names, cvbfsTree.getParents(), cvbfsTree.getChildren()));
		treeFrame.showTreeFrame();
	}

}
