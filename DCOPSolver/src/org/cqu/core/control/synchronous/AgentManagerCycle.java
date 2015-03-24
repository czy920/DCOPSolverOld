package org.cqu.core.control.synchronous;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.algorithm.adopt.AdoptAgentCycle;
import org.cqu.algorithm.adopt.AdoptAgentCycle_2;
import org.cqu.algorithm.bnbadopt.ADOPT_K;
import org.cqu.algorithm.bnbadopt.BnBAdoptAgent;
import org.cqu.algorithm.bnbmergeadopt.AgentModel;
import org.cqu.algorithm.bnbmergeadopt.BDAdoptProblem;
import org.cqu.core.control.Message;
import org.cqu.gui.settings.Settings;
import org.cqu.problem.parser.Problem;
import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.utility.CollectionUtil;
import org.cqu.utility.FileUtil;


public class AgentManagerCycle {

    private Map<Integer, AgentCycle> agents;
	
	public AgentManagerCycle(Problem problem, String agentType) {
		// TODO Auto-generated constructor stub
		PseudotreeProblem pseudotreeProblem=(PseudotreeProblem) problem;
		agents=new HashMap<Integer, AgentCycle>();
		for(Integer agentId : problem.getNodeIds())
		{
			AgentCycle agent=null;;
			if(agentType.equals("DPOP"))
			{
			}else if(agentType.equals("BNBADOPT"))
			{
				agent=new BnBAdoptAgent(agentId, pseudotreeProblem.getNodeName(agentId), pseudotreeProblem.getNodeLayer(agentId), 
						pseudotreeProblem.getNodeDomain(agentId));
			}else if(agentType.equals("BDADOPT"))
			{
				agent=new AgentModel(agentId, pseudotreeProblem.getNodeName(agentId), pseudotreeProblem.getNodeLayer(agentId), 
						pseudotreeProblem.getNodeDomain(agentId),pseudotreeProblem.getTreeHeight(),((BDAdoptProblem)problem).getPseduHeight());
			}else if(agentType.equals("ADOPT_K"))
			{
				agent=new ADOPT_K(agentId, pseudotreeProblem.getNodeName(agentId), pseudotreeProblem.getNodeLayer(agentId), 
						pseudotreeProblem.getNodeDomain(agentId),Settings.settings.getADOPT_K());
			}else if(agentType.equals("SynAdopt2"))
			{
				agent=new AdoptAgentCycle_2(agentId, pseudotreeProblem.getNodeName(agentId), pseudotreeProblem.getNodeLayer(agentId), 
						pseudotreeProblem.getNodeDomain(agentId));
			}else
			{
				agent=new AdoptAgentCycle(agentId, pseudotreeProblem.getNodeName(agentId), pseudotreeProblem.getNodeLayer(agentId), 
						pseudotreeProblem.getNodeDomain(agentId));
			}
			agent.setNeibours(pseudotreeProblem.getNodeNeighbours(agentId), pseudotreeProblem.getNodeParent(agentId), 
					pseudotreeProblem.getNodeChildren(agentId), pseudotreeProblem.getNodeAllParents(agentId), 
					pseudotreeProblem.getNodeAllChildren(agentId), pseudotreeProblem.getNodeNeighbourDomains(agentId), 
					pseudotreeProblem.getNodeConstraintValues(agentId), pseudotreeProblem.getNodeNeighbourLayers(agentId));
			
			agents.put(agent.getId(), agent);
			
			{
				String str="-----------"+agent.name+"-----------\n";
				str+="Parent: "+agent.parent+"\n";
				str+="Children: "+CollectionUtil.arrayToString(agent.children)+"\n";
				str+="AllParents: "+CollectionUtil.arrayToString(agent.allParents)+"\n";
				str+="AllChildren: "+CollectionUtil.arrayToString(agent.allChildren)+"\n";
				FileUtil.writeStringAppend(str, "dfsTree.txt");
			}
		}
	}
	
	public AgentCycle getAgent(int agentId)
	{
		if(agents.containsKey(agentId))
		{
			return agents.get(agentId);
		}else
		{
			return null;
		}
	}
	
	public Map<Integer, Integer> getAgentValues()
	{
		Map<Integer, Integer> agentValues=new HashMap<Integer, Integer>();
		for(AgentCycle agent : agents.values())
		{
			agentValues.put(agent.getId(), agent.getValue());
		}
		return agentValues;
	}
	
	public Map<Integer, AgentCycle> getAgents()
	{
		return this.agents;
	}
	
	public int getAgentCount()
	{
		return this.agents.size();
	}
	
	public void startAgents(MessageMailerCycle msgMailer)
	{
		for(AgentCycle agent : agents.values())
		{
			agent.setMessageMailer(msgMailer);
			agent.startProcess();
		}
	}
	
	public void stopAgents()
	{
		for(AgentCycle agent : agents.values())
		{
			agent.stopRunning();
		}
	}
	
	public Object printResults(List<Map<String, Object>> results)
	{
		if(results.size()>0)
		{
			AgentCycle agent=null;
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
		AgentCycle senderAgent=this.getAgent(msg.getIdSender());
		AgentCycle receiverAgent=this.getAgent(msg.getIdReceiver());
		return senderAgent.easyMessageContent(msg, senderAgent, receiverAgent);
	}
}
