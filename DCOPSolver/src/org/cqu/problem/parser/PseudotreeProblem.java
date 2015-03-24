package org.cqu.problem.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cqu.structure.tree.BFSTree;
import org.cqu.structure.tree.DFSTree;
import org.cqu.structure.tree.TreeGenerator;
import org.cqu.structure.tree.TreeGenerator.PseudoTreeType;

public class PseudotreeProblem extends Problem{
	
	protected PseudoTreeType pseudotreeType;
	
	protected Map<Integer, Integer> nodeLayers;
	protected Map<Integer, Integer> nodeParents;
	protected Map<Integer, int[]> nodeAllParents;
	protected Map<Integer, int[]> nodeChildren;
	protected Map<Integer, int[]> nodeAllChildren;
	
	public PseudotreeProblem(String path, PseudoTreeType pseudotreeType) {
		// TODO Auto-generated constructor stub
		super(path);
		this.pseudotreeType=pseudotreeType;
		
		nodeLayers=new HashMap<Integer, Integer>();
	}
	
	@Override
	public void load() {
		// TODO Auto-generated method stub
		super.load();
		TreeGenerator treeGenerator=getGenerator();
		treeGenerator.generate();
		setTreeStructureData(treeGenerator);
	}
	
	protected TreeGenerator getGenerator()
	{
		TreeGenerator treeGenerator;
		if(pseudotreeType==PseudoTreeType.DFS)
		{
			treeGenerator=new DFSTree(nodeNeighbours);
		}else
		{
			treeGenerator=new BFSTree(nodeNeighbours);
		}
		return treeGenerator;
	}
	
	private void setTreeStructureData(TreeGenerator treeGenerator)
	{
		nodeLayers=treeGenerator.getNodeLayers();
		nodeParents=treeGenerator.getParentNode();
		nodeAllParents=treeGenerator.getAllParents();
		nodeChildren=treeGenerator.getChildrenNodes();
		nodeAllChildren=treeGenerator.getAllChildren();
	}
	
	public Integer getNodeLayer(Integer id)
	{
		return nodeLayers.get(id);
	}
	
	public Map<Integer, Integer> getNodeNeighbourLayers(Integer id)
	{
		Map<Integer, Integer> neighbourLayers=new HashMap<Integer, Integer>();
		int[] neighbours=nodeNeighbours.get(id);
		for(int i=0;i<neighbours.length;i++)
		{
			neighbourLayers.put(neighbours[i], nodeLayers.get(neighbours[i]));
		}
		return neighbourLayers;
	}
	
	public Integer getNodeParent(Integer id)
	{
		return nodeParents.get(id);
	}
	
	public int[] getNodeAllParents(Integer id)
	{
		return nodeAllParents.get(id);
	}
	
	public int[] getNodeChildren(Integer id)
	{
		return nodeChildren.get(id);
	}
	
	public int[] getNodeAllChildren(Integer id)
	{
		return nodeAllChildren.get(id);
	}
	
	public int getTreeHeight()
	{
		return Collections.max(nodeLayers.values());
	}
}
