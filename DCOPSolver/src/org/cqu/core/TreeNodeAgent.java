package org.cqu.core;

import java.util.Map;

import org.cqu.core.control.MessageSink;
import org.cqu.statistics.StatisticsListener;
import org.cqu.utility.CollectionUtil;


public abstract class TreeNodeAgent extends Agent{

	protected int layer;
	protected int parent;
	protected int[] allParents;
	protected int[] pseudoParents;
	protected int[] allChildren;
	protected int[] children;
	protected int[] pseudoChildren;
	
	protected Map<Integer, int[]> neighbourDomains;
	protected Map<Integer, Integer> neighbourLayers;
	protected Map<Integer, int[][]> constraintCosts;
	
	public TreeNodeAgent(int id, String name, int[] domain, int[] neighbours,
			MessageSink msgSink, StatisticsListener statisticsListener,
			int capacity) {
		super(id, name, domain, neighbours, msgSink, statisticsListener, capacity);
		// TODO Auto-generated constructor stub
	}
	
	public void setStructureInformation(int parent, int[] children, int[] allParents, 
			int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, 
			int[][]> constraintCosts, int layer, Map<Integer, Integer> neighbourLayers)
	{
		this.parent=parent;
		this.children=children;
		this.allParents=allParents;
		this.allChildren=allChildren;
		if(this.allChildren!=null&&this.children!=null)
		{
			this.pseudoChildren=CollectionUtil.except(this.allChildren, this.children);
		}
		if(this.allParents!=null&&this.parent!=-1)
		{
			this.pseudoParents=CollectionUtil.except(this.allParents, new int[]{this.parent});
		}
		
		this.neighbourDomains=neighbourDomains;
		this.constraintCosts=constraintCosts;
		this.layer=layer;
		this.neighbourLayers=neighbourLayers;
	}
	
	public boolean isLeafAgent()
	{
		return this.children==null||this.children.length==0;
	}
	
	public boolean isRootAgent()
	{
		return this.parent==-1;
	}
}
