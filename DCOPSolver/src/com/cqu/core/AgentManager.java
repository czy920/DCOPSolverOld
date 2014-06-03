package com.cqu.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.adopt.AdoptAgent;

public class AgentManager {
	
	private Map<Integer, Agent> agents;
	
	public AgentManager(Problem problem) {
		// TODO Auto-generated constructor stub
		
		agents=new HashMap<Integer, Agent>();
		for(int i=0;i<problem.agentCount;i++)
		{
			Agent agent=new AdoptAgent(problem.agentIds[i], problem.agentNames[i], problem.domains.get(problem.agentDomains[i]));
			Map<Integer, int[]> neighbourDomains=new HashMap<Integer, int[]>();
			Map<Integer, int[][]> constraintCosts=new HashMap<Integer, int[][]>();
			for(int j=0;j<problem.neighbourAgentDomains[i].length;j++)
			{
				neighbourDomains.put(problem.neighbourAgents.get(i)[j], problem.domains.get(problem.neighbourAgentDomains[i][j]));
			}
			for(int j=0;j<problem.agentConstraintCosts[i].length;j++)
			{
				constraintCosts.put(problem.neighbourAgents.get(i)[j], problem.costs.get(problem.agentConstraintCosts[i][j]));
			}
			
			agent.setNeibours(problem.neighbourAgents.get(i), problem.parentAgents.get(i), 
					problem.childAgents.get(i), problem.pseudoParentAgents.get(i), 
					problem.pseudoChildAgents.get(i), neighbourDomains, constraintCosts);
			
			agents.put(agent.getId(), agent);
		}
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
	
	public void printResults(List<Map<String, Object>> results)
	{
		if(results.size()>0)
		{
			this.agents.get(0).printResults(results);
		}
	}
	
	public String easyMessageContent(Message msg)
	{
		Agent senderAgent=this.getAgent(msg.getIdSender());
		Agent receiverAgent=this.getAgent(msg.getIdReceiver());
		return senderAgent.easyMessageContent(msg, senderAgent, receiverAgent);
	}
}
