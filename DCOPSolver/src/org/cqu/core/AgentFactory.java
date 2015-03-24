package org.cqu.core;

import java.util.HashMap;
import java.util.Map;

import org.cqu.algorithm.dpop.DPOPAgent;
import org.cqu.core.control.MessageSink;
import org.cqu.problem.parser.Problem;
import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.statistics.StatisticsListener;


public class AgentFactory {
	
	public static final String[] AGENT_TYPES=new String[]{"DPOP", "BFSDPOP", "HybridDPOP", "HybridMBDPOP", "AgileDPOP", "ADOPT", "BNBADOPT","ADOPT_K","BDADOPT","SynAdopt","SynAdopt2"};
	
	private Map<Integer, Agent> agents;
	private Problem problem;
	private String agentType;
	
	public AgentFactory(Problem problem, String agentType) {
		// TODO Auto-generated constructor stub
		agents=new HashMap<Integer, Agent>();
		this.problem=problem;
		this.agentType=agentType;
	}
	
	public void constructAgents(MessageSink msgSink, StatisticsListener statisticsListener)
	{
		agents=new HashMap<Integer, Agent>();
		for(Integer id : problem.getNodeIds())
		{
			Agent agent=null;
			if(agentType.equals("DPOP"))
			{
				TreeNodeAgent treeNodeAgent=new DPOPAgent(id, problem.getNodeName(id), 
						problem.getNodeDomain(id), problem.getNodeNeighbours(id), msgSink, statisticsListener, -1);
				PseudotreeProblem pseudoTreeProblem=(PseudotreeProblem) problem;
				treeNodeAgent.setStructureInformation(pseudoTreeProblem.getNodeParent(id), 
						pseudoTreeProblem.getNodeChildren(id), pseudoTreeProblem.getNodeAllParents(id), 
						pseudoTreeProblem.getNodeAllChildren(id), pseudoTreeProblem.getNodeNeighbourDomains(id), 
						pseudoTreeProblem.getNodeConstraintValues(id), pseudoTreeProblem.getNodeLayer(id), 
						pseudoTreeProblem.getNodeNeighbourLayers(id));
				agent=treeNodeAgent;
			}else if(agentType.equals("BFSDPOP"))
			{
				
			}else
			{
				
			}
			agents.put(id, agent);
		}
		
		/*LabelPhase labelPhase=null;
		if(agentType.equals("HybridMBDPOP"))
		{
			labelPhase=new LabelPhaseHybridMBDPOP(problem);
			labelPhase.label();
		}else if(agentType.equals("HybridDPOP"))
		{
			labelPhase=new LabelPhaseHybridDPOP(problem);
			labelPhase.label();
		}
		agents=new HashMap<Integer, Agent>();
		for(Integer agentId : problem.agentNames.keySet())
		{
			Agent agent=null;
			if(agentType.equals("DPOP"))
			{
				agent=new DPOPAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}else if(agentType.equals("HybridMBDPOP"))
			{
				agent=new HybridMBDPOP(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)), labelPhase.getIsSearchingPolicyAgents().get(agentId), 
						labelPhase.getIsNeighborSearchingPolicyAgents().get(agentId), labelPhase.getContext());
			}else if(agentType.equals("HybridDPOP"))
			{
				agent=new HybridDPOP(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)), labelPhase.getIsSearchingPolicyAgents().get(agentId), 
						labelPhase.getIsNeighborSearchingPolicyAgents().get(agentId), labelPhase.getContext());
			}else if(agentType.equals("AgileDPOP"))
			{
				agent=new AgileDPOPAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}else if(agentType.equals("BFSDPOP"))
			{
				agent=new BFSDPOPAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)), problem.crossConstraintAllocation.get(agentId));
			}else
			{
				agent=new AdoptAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			Map<Integer, int[]> neighbourDomains=new HashMap<Integer, int[]>();
			Map<Integer, int[][]> constraintCosts=new HashMap<Integer, int[][]>();
			int[] neighbourAgentIds=problem.neighbourAgents.get(agentId);
			Map<Integer, Integer> neighbourLevels=new HashMap<Integer, Integer>();
			for(int i=0;i<neighbourAgentIds.length;i++)
			{
				neighbourDomains.put(neighbourAgentIds[i], problem.domains.get(problem.agentDomains.get(neighbourAgentIds[i])));
				neighbourLevels.put(neighbourAgentIds[i], problem.agentLevels.get(neighbourAgentIds[i]));
			}
			String[] neighbourAgentCostNames=problem.agentConstraintCosts.get(agentId);
			for(int i=0;i<neighbourAgentCostNames.length;i++)
			{
				constraintCosts.put(neighbourAgentIds[i], 
						CollectionUtil.toTwoDimension(problem.costs.get(neighbourAgentCostNames[i]), 
								problem.domains.get(problem.agentDomains.get(agentId)).length, 
								problem.domains.get(problem.agentDomains.get(neighbourAgentIds[i])).length));
			}
			
			agent.setNeibours(problem.neighbourAgents.get(agentId), problem.parentAgents.get(agentId), 
					problem.childAgents.get(agentId), problem.allParentAgents.get(agentId), 
					problem.allChildrenAgents.get(agentId), neighbourDomains, constraintCosts, neighbourLevels);
			
			agents.put(agent.getId(), agent);
			
			//get tree height
			if(treeHeight<problem.agentLevels.get(agentId))
			{
				treeHeight=problem.agentLevels.get(agentId);
			}
			
			{
				String str="-----------"+agent.name+"-----------\n";
				str+="Parent: "+agent.parent+"\n";
				str+="Children: "+CollectionUtil.arrayToString(agent.children)+"\n";
				str+="AllParents: "+CollectionUtil.arrayToString(agent.allParents)+"\n";
				str+="AllChildren: "+CollectionUtil.arrayToString(agent.allChildren)+"\n";
				FileUtil.writeStringAppend(str, "dfsTree.txt");
			}
		}
		treeHeight++;*/
	}
	
	public Map<Integer, Agent> getAgents()
	{
		return agents;
	}
	
	public void startAgents()
	{
		for(Agent agent : agents.values())
		{
			agent.start();
		}
	}
}
