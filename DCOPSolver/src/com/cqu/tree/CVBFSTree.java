package com.cqu.tree;

import java.util.Map;

public class CVBFSTree extends BFSTree{

	public CVBFSTree(Map<Integer, int[]> neighbors) {
		super(neighbors);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void generate() {
		// TODO Auto-generated method stub
		findRootByCutVertex();
		super.generate();
	}
	
	private void findRootByCutVertex()
	{
		DynamicCutVertex dcv=new DynamicCutVertex(this.neighbors);
		dcv.init();
		
		Integer bestCutVertext=dcv.selectBest(null, nodeIterated, this.neighbors.keySet());
		if(bestCutVertext!=null)
		{
			this.rootId=bestCutVertext;
		}else
		{
			this.rootId=this.neighbors.keySet().iterator().next();
		}
	}

}
