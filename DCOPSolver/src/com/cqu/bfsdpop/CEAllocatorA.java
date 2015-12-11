package com.cqu.bfsdpop;

import com.cqu.parser.Problem;

/**
 * cluster removing by cross edge count
 * @author CQU
 *
 */
public class CEAllocatorA extends CrossEdgeAllocator{

	public CEAllocatorA(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void allocate() {
		//交叉边分配方法一：根据交叉边数进行ClusterRemoving
		if(crossEdges.size()<=0)
		{
			return;
		}
		Integer[] keys=new ListSizeComparator<Edge>(crossEdges).sort();
		Integer maxAgent=keys[keys.length-1];
		while(crossEdges.get(maxAgent).size()>0)
		{
			removeCluster(maxAgent);
			
			keys=new ListSizeComparator<Edge>(crossEdges).sort();
			maxAgent=keys[keys.length-1];
		}
	}

}
