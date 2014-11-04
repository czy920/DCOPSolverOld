package com.cqu.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.adopt.AdoptAgent;
import com.cqu.bfsdpop.BFSDPOPAgent;
import com.cqu.dpop.DPOPAgent;
import com.cqu.util.CollectionUtil;
import com.cqu.util.FileUtil;

public class AgentManager {
	
	public static final String[] AGENT_TYPES=new String[]{"DPOP", "BFSDPOP", "ADOPT", "BNBADOPT","BDADOPT","SynAdopt"};
	
	private Map<Integer, Agent> agents;
	private int treeHeight=0;
	
	public AgentManager(Problem problem, String agentType) {
		// TODO Auto-generated constructor stub
		
		agents=new HashMap<Integer, Agent>();
		for(Integer agentId : problem.agentNames.keySet())
		{
			Agent agent=null;
			if(agentType.equals("DPOP"))
			{
				agent=new DPOPAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
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
		treeHeight++;
	}
	
	public int getTreeHeight()
	{
		return this.treeHeight;
	}
	
	public Agent getAgent(int agentId)
	{
		if(agents.containsKey(agentId))
		{
			return agents.get(agentId);
		}else
		{
			return null;
		}
	}
	
	public int getAgentCount()
	{
		return this.agents.size();
	}
	
	public void startAgents(MessageMailer msgMailer)
	{
		for(Agent agent : agents.values())
		{
			agent.setMessageMailer(msgMailer);
			agent.start();
		}
	}
	
	public Object printResults(List<Map<String, Object>> results)
	{
		if(results.size()>0)
		{
			Agent agent=null;
			for(Integer agentId : this.agents.keySet())
			{
				agent=this.agents.get(agentId);
				break;
			}
			return agent.printResults(results);
		}
		return null;
	}
	
	public String easyMessageContent(Message msg)
	{
		Agent senderAgent=this.getAgent(msg.getIdSender());
		Agent receiverAgent=this.getAgent(msg.getIdReceiver());
		return senderAgent.easyMessageContent(msg, senderAgent, receiverAgent);
	}
}
