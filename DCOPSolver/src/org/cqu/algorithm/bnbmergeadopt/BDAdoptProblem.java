package org.cqu.algorithm.bnbmergeadopt;

import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.structure.tree.TreeGenerator.PseudoTreeType;

public class BDAdoptProblem extends PseudotreeProblem{

	public BDAdoptProblem(String path, PseudoTreeType pseudotreeType) {
		super(path, pseudotreeType);
		// TODO Auto-generated constructor stub
	}
	
	public long getPseduHeight() {
		long pseduHeight=0;
		
		Integer curNodeId=findRoot();		
		boolean link=true;
		while(link==true&&curNodeId!=(-1))
		{
			if(this.nodeChildren.get(curNodeId).length>1){
				link=false;
			}else {
				pseduHeight++;
				if(this.nodeChildren.get(curNodeId).length==1){
					curNodeId=this.nodeChildren.get(curNodeId)[0];
				}
				if(this.nodeChildren.get(curNodeId).length==0){
					break;
				}
			}
		}
		return pseduHeight;
	}
	
	private Integer findRoot()
	{
		for(Integer id : nodeIds)
		{
			if(nodeParents.get(id)==-1)
			{
				return id;
			}
		}
		return -1;
	}

}
