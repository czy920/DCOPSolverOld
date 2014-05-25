package com.cqu.core;

import java.util.HashMap;
import java.util.Map;

import com.cqu.adopt.AdoptAgent;

public class AgentManager {
	
	private Map<Integer, Agent> agents;
	
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
	
    public void setupAgents(Problem problem)
    {
    	agents=new HashMap<Integer, Agent>();
		for(int i=0;i<problem.agentCount;i++)
		{
			Agent agent=new AdoptAgent(problem.agentIds[i], problem.agentNames[i]);
			
			agents.put(agent.getId(), agent);
		}
    }
	
	public void startAlgorithm()
	{
		for(Agent agent : agents.values())
		{
			new Thread(agent).start();
		}
	}
}
