package com.cqu.bfsdpop;

import java.util.List;

import com.cqu.parser.Problem;
import com.cqu.util.CollectionUtil;

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
		List<Edge> edgeList=crossEdges.get(keys[keys.length-1]);
		while(edgeList.size()>0)
		{
			for(int i=0;i<edgeList.size();i++)
			{
				Edge edge=edgeList.get(i);
				
				int index=CollectionUtil.indexOf(neighbourAgents.get(edge.getNodeB()), edge.getNodeA());
				considerCrossConstraint.get(edge.getNodeB())[index]=true;
				
				this.removeEdge(crossEdges.get(edge.getNodeB()), edge);
			}
			edgeList.clear();
			
			keys=new ListSizeComparator<Edge>(crossEdges).sort();
			edgeList=crossEdges.get(keys[keys.length-1]);
		}
	}

}
