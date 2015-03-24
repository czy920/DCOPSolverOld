package org.cqu.algorithm.hybriddpop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.algorithm.hybridmbdpop.LabelPhase;
import org.cqu.gui.settings.Settings;
import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.utility.ArrayIndexComparator;


public class LabelPhaseHybridDPOP extends LabelPhase{
	
	public LabelPhaseHybridDPOP(PseudotreeProblem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void labelSearchingPolicyAgents() {
		// TODO Auto-generated method stub
		Map<Integer, List<Integer>> layerAgents=new HashMap<Integer, List<Integer>>();
		for(Integer agentId : problem.getNodeIds())
		{
			if(layerAgents.containsKey(problem.getNodeLayer(agentId))==false)
			{
				layerAgents.put(problem.getNodeLayer(agentId), new ArrayList<Integer>());
			}
			layerAgents.get(problem.getNodeLayer(agentId)).add(agentId);
		}
		List<Integer> layers=new ArrayList<Integer>(layerAgents.keySet());
		Collections.sort(layers);
		Map<Integer, List<Integer>> agentSeparators=new HashMap<Integer, List<Integer>>();
		List<Integer> searchingPolicyAgents=new ArrayList<Integer>();
		for(int i=layers.size()-1;i>-1;i--)
		{
			List<Integer> nodesOfLayer=layerAgents.get(layers.get(i));
			for(Integer agentId : nodesOfLayer)
			{
				//sep(children)
				List<Integer> childSeparator=new ArrayList<Integer>();
				int[] children=problem.getNodeChildren(agentId);
				for(int j=0;j<children.length;j++)
				{
					merge(childSeparator, agentSeparators.get(children[j]));
					agentSeparators.remove(children[j]);
				}
				//-self
				remove(childSeparator, agentId);
				//+allParents
				List<Integer> separator=new ArrayList<Integer>(childSeparator);
				merge(separator, problem.getNodeAllParents(agentId));
				//-already
				remove(separator, searchingPolicyAgents);
				
				int diff=separator.size()-Settings.settings.getMaxDimensionsInMBDPOP();
				if(diff>0)
				{
					sortByLayer(separator);
					for(int k=0;k<diff;k++)
					{
						isSearchingPolicyAgents.put(separator.get(k), true);
						searchingPolicyAgents.add(separator.get(k));
						remove(separator, separator.get(k));
					}
					isSearchingPolicyAgents.put(agentId, false);
					agentSeparators.put(agentId, separator);
				}else
				{
					if(isSearchingPolicyAgents.containsKey(agentId)==false)
					{
						isSearchingPolicyAgents.put(agentId, false);
					}
					agentSeparators.put(agentId, separator);
				}
			}
		}
	}
	
	private void sortByLayer(List<Integer> agents)
	{
		Integer[] agentArray=new Integer[agents.size()];
		agents.toArray(agentArray);
		
		Integer[] layers=new Integer[agents.size()];
		for(int i=0;i<agents.size();i++)
		{
			layers[i]=problem.getNodeLayer(agents.get(i));
		}
		ArrayIndexComparator<Integer> sorter=new ArrayIndexComparator<Integer>(layers);
		sorter.sort();
		
		agents.clear();
		for(int i=0;i<agentArray.length;i++)
		{
			agents.add(agentArray[i]);
		}
	}
}
