package com.cqu.hybridmbdpop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.parser.Problem;
import com.cqu.settings.Settings;
import com.cqu.util.CollectionUtil;

public class LabelPhase {
	
	private Problem problem;
	
	public LabelPhase(Problem problem) {
		// TODO Auto-generated constructor stub
		this.problem=problem;
	}
	
	public Map<Integer, Boolean> labelSearchingPolicyAgents()
	{
		Map<Integer, Boolean> isSearchingPolicyAgents=new HashMap<Integer, Boolean>();
		
		Map<Integer, List<Integer>> layerAgents=new HashMap<Integer, List<Integer>>();
		for(Integer agentId : problem.agentLevels.keySet())
		{
			if(layerAgents.containsKey(problem.agentLevels.get(agentId))==false)
			{
				layerAgents.put(problem.agentLevels.get(agentId), new ArrayList<Integer>());
			}
			layerAgents.get(problem.agentLevels.get(agentId)).add(agentId);
		}
		List<Integer> layers=new ArrayList<Integer>(layerAgents.keySet());
		Collections.sort(layers);
		Map<Integer, List<Integer>> agentSeparators=new HashMap<Integer, List<Integer>>();
		for(int i=layers.size()-1;i>-1;i--)
		{
			List<Integer> nodesOfLayer=layerAgents.get(layers.get(i));
			for(Integer agentId : nodesOfLayer)
			{
				//sep(children)-self+allParents
				List<Integer> childSeparator=new ArrayList<Integer>();
				int[] children=problem.childAgents.get(agentId);
				for(int j=0;j<children.length;j++)
				{
					merge(childSeparator, agentSeparators.get(children[i]));
					agentSeparators.remove(children[i]);
				}
				remove(childSeparator, agentId);
				if(childSeparator.size()>Settings.settings.getMaxDimensionsInAgileDPOP())
				{
					isSearchingPolicyAgents.put(agentId, true);
					agentSeparators.put(agentId, new ArrayList<Integer>());
					continue;
				}
				
				List<Integer> separator=new ArrayList<Integer>(childSeparator);
				merge(separator, problem.allParentAgents.get(agentId));
				if(separator.size()>Settings.settings.getMaxDimensionsInAgileDPOP())
				{
					isSearchingPolicyAgents.put(agentId, true);
					agentSeparators.put(agentId, childSeparator);
				}else
				{
					isSearchingPolicyAgents.put(agentId, false);
					agentSeparators.put(agentId, separator);
				}
			}
		}
		return isSearchingPolicyAgents;
	}
	
	public void remove(List<Integer> base, int[] toRemove)
	{
		if(toRemove==null)
		{
			return;
		}
		for(int i=0; i<toRemove.length;i++)
		{
			if(CollectionUtil.indexOf(base, toRemove[i])!=-1)
			{
				base.add(toRemove[i]);
			}
		}
	}
	
	public void remove(List<Integer> base, Integer value)
	{
		int index=CollectionUtil.indexOf(base, value);
		if(index!=-1)
		{
			base.remove(index);
		}
	}
	
	public void merge(List<Integer> base, List<Integer> toMerge)
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
	
	public void merge(List<Integer> base, int[] toMerge)
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
