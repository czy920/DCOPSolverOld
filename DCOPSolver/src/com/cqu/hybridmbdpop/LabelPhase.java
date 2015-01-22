package com.cqu.hybridmbdpop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.core.Context;
import com.cqu.dpop.Dimension;
import com.cqu.parser.Problem;
import com.cqu.util.CollectionUtil;

public abstract class LabelPhase {
	
	protected Problem problem;
	protected Map<Integer, Boolean> isSearchingPolicyAgents;
	private Map<Integer, boolean[]> isNeighborSearchingPolicyAgents;
	
	public LabelPhase(Problem problem) {
		// TODO Auto-generated constructor stub
		this.problem=problem;
		this.isSearchingPolicyAgents=new HashMap<Integer, Boolean>();
		this.isNeighborSearchingPolicyAgents=new HashMap<Integer, boolean[]>();
	}
	
	public Map<Integer, Boolean> getIsSearchingPolicyAgents() {
		return isSearchingPolicyAgents;
	}

	public Map<Integer, boolean[]> getIsNeighborSearchingPolicyAgents() {
		return isNeighborSearchingPolicyAgents;
	}
	
	public ContextWrapped getContext()
	{
		Context context=new Context();
		List<Dimension> dimensions=new ArrayList<Dimension>();
		int domainSize=problem.domains.values().iterator().next().length;
		for(Integer key : isSearchingPolicyAgents.keySet())
		{
			if(isSearchingPolicyAgents.get(key)==true)
			{
				context.addOrUpdate(key, 0);
				dimensions.add(new Dimension(key+"", domainSize, problem.agentLevels.get(key)));
			}
		}
		
		return new ContextWrapped(context, dimensions);
	}

	public void label()
	{
		labelSearchingPolicyAgents();
		calNeighborPolicy();
	}
	
	protected abstract void labelSearchingPolicyAgents();
	
	private void calNeighborPolicy()
	{
		for(Integer agentId : problem.neighbourAgents.keySet())
		{
			int[] neighbors=problem.neighbourAgents.get(agentId);
			boolean[] neighborPolicy=new boolean[neighbors.length];
			for(int i=0;i<neighbors.length;i++)
			{
				neighborPolicy[i]=isSearchingPolicyAgents.get(neighbors[i]);
			}
			isNeighborSearchingPolicyAgents.put(agentId, neighborPolicy);
		}
	}
	
	protected void remove(List<Integer> base, List<Integer> toRemove)
	{
		if(toRemove==null)
		{
			return;
		}
		int index=-1;
		for(int i=0; i<toRemove.size();i++)
		{
			index=CollectionUtil.indexOf(base, toRemove.get(i));
			if(index!=-1)
			{
				base.remove(index);
			}
		}
	}
	
	protected void remove(List<Integer> base, Integer value)
	{
		int index=CollectionUtil.indexOf(base, value);
		if(index!=-1)
		{
			base.remove(index);
		}
	}
	
	protected void merge(List<Integer> base, List<Integer> toMerge)
	{
		if(toMerge==null)
		{
			return;
		}
		for(Integer temp : toMerge)
		{
			if(CollectionUtil.indexOf(base, temp)==-1)
			{
				base.add(temp);
			}
		}
	}
	
	protected void merge(List<Integer> base, int[] toMerge)
	{
		if(toMerge==null)
		{
			return;
		}
		for(int i=0; i<toMerge.length;i++)
		{
			if(CollectionUtil.indexOf(base, toMerge[i])==-1)
			{
				base.add(toMerge[i]);
			}
		}
	}
}
