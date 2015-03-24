package org.cqu.algorithm.hybridmbdpop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.gui.settings.Settings;
import org.cqu.problem.parser.PseudotreeProblem;


public class LabelPhaseHybridMBDPOP extends LabelPhase{
	
	public LabelPhaseHybridMBDPOP(PseudotreeProblem problem) {
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
		for(int i=layers.size()-1;i>-1;i--)
		{
			List<Integer> nodesOfLayer=layerAgents.get(layers.get(i));
			for(Integer agentId : nodesOfLayer)
			{
				//sep(children)-self+allParents
				List<Integer> childSeparator=new ArrayList<Integer>();
				int[] children=problem.getNodeChildren(agentId);
				for(int j=0;j<children.length;j++)
				{
					merge(childSeparator, agentSeparators.get(children[j]));
					agentSeparators.remove(children[j]);
				}
				remove(childSeparator, agentId);
				
				List<Integer> separator=new ArrayList<Integer>(childSeparator);
				merge(separator, problem.getNodeAllParents(agentId));
				if(separator.size()>Settings.settings.getMaxDimensionsInMBDPOP())
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
	}
}
