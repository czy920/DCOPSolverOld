package com.cqu.cyclequeue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cqu.aco.AcoAgent;
import com.cqu.aco.PublicConstants;
import com.cqu.adopt.AdoptAgentCycle;
import com.cqu.adopt.AdoptAgentCycle_2;
import com.cqu.bnbadopt.ADOPT_K;
import com.cqu.bnbadopt.BnBAdoptAgent;
import com.cqu.bnbmergeadopt.AgentModel;
import com.cqu.core.Infinity;
import com.cqu.core.Message;
import com.cqu.dgibbs.DGibbsAgent;
import com.cqu.dsa.*;
import com.cqu.maxsum.*;
import com.cqu.dsan.*;
import com.cqu.mgm.*;
import com.cqu.mus.*;
import com.cqu.pds.*;
import com.cqu.parser.Problem;
import com.cqu.sbb.SynchBBAgent;
import com.cqu.settings.Settings;
import com.cqu.util.CollectionUtil;
import com.cqu.util.FileUtil;

public class AgentManagerCycle {

    private Map<Integer, AgentCycle> agents;
	
	public AgentManagerCycle(Problem problem, String agentType) {
		// TODO Auto-generated constructor stub
		
		agents=new HashMap<Integer, AgentCycle>();
		for(Integer agentId : problem.agentNames.keySet())
		{
			AgentCycle agent=null;;
			if(agentType.equals("DPOP"))
			{
			}else if(agentType.equals("BNBADOPT"))
			{
				agent=new BnBAdoptAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
				
				/*int []domain = new int[problem.variableDomains.get(agentId).size()];
				Iterator<Integer> iter = problem.variableDomains.get(agentId).iterator();
				//System.out.println("agent:" + agentId);
				for (int i = 0; iter.hasNext();i++)
				{
					domain[i] = iter.next();
					
				}*/
				
	
				/*agent=new BnBAdoptAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.VariableDomains.get(agentId));
				
				BnBAdoptAgent.agentDomains.put(agentId, problem.VariableDomains.get(agentId));*/
				
			}
			else if(agentType.equals("BDADOPT")){
				agent=new AgentModel(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)),problem.treeDepth,problem.pseudoHeight);
			}
			else if(agentType.equals("ADOPT_K")){
				agent=new ADOPT_K(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)),Settings.settings.getADOPT_K());
			}
			else if(agentType.equals("SynAdopt2")){
				agent=new AdoptAgentCycle_2(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_A")){
				agent=new DsaA_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_B")){
				agent=new DsaB_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_C")){
				agent=new DsaC_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_D")){
				agent=new DsaD_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_E")){
				agent=new DsaE_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSAN")){
				agent=new DsanAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("MGM")){
				agent=new MgmAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("MGM2")){
				agent=new Mgm2Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDSA")){
				agent=new AlsDsaAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALS_DSA")){
				agent=new AlsDsa_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_PPIRA")){
				agent=new DsaPPIRA_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("DSA_SDP")){
				agent=new DsaSDP_Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALS_GDBA")){
				agent=new AlsGdbaAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSMLUDSA")){
				agent=new AlsMluDsaAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDSAMGM")){
				agent=new AlsDsaMgmAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDSAMGMEVO")){
				agent=new AlsDsaMgmEvoAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDSADSAEVO")){
				agent=new AlsDsaDsaEvoAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDGA")){
				agent=new AlsDgaAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ALSDGAFB")){
				agent=new AlsDgaFBAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSALSDSA")){
				agent=new Pds_AlsDsaAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSALSDSAN")){
				agent=new Pds_AlsDsanAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSALSMGM")){
				agent=new Pds_AlsMgmAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSALSMGM2")){
				agent=new Pds_AlsMgm2Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSDSAN")){
				agent=new Pds_DsanAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSDSASDP")){
				agent=new Pds_DsaSdpAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSMGM")){
				agent=new Pds_MgmAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("PDSMGM2")){
				agent=new Pds_Mgm2Agent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if(agentType.equals("ACO")||agentType.equals("ACO_tree")||agentType.equals("ACO_bf")||agentType.equals("ACO_phase")||
					agentType.equals("ACO_line")||agentType.equals("ACO_final")){
				agent=new AcoAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)), problem.treeDepth);
			}
			else if (agentType.equals("MAXSUM")) {
				agent=new MaxSumAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if (agentType.equals("MAXSUMAD")) {
				agent=new MaxSumADAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if (agentType.equals("MAXSUMRS")) {
				agent=new MaxSumRefineStructureAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
//			else if (agentType.equals("MAXSUMSPLITED")) {
//				agent=new SplitedMaxSumAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
//						problem.domains.get(problem.agentDomains.get(agentId)));
//			}
			else if (agentType.equals("SBB")) {
				agent=new SynchBBAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if (agentType.equals("MAXSUMOH")) {
				agent=new MaxSumOneHotAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else if (agentType.equals("DGIBBS")){
				agent=new DGibbsAgent(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId),
						problem.domains.get(problem.agentDomains.get(agentId)));
			}
			else{
				agent=new AdoptAgentCycle(agentId, problem.agentNames.get(agentId), problem.agentLevels.get(agentId), 
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
				if(agentId < neighbourAgentIds[i]){
					constraintCosts.put(neighbourAgentIds[i], 
							CollectionUtil.toTwoDimension(problem.costs.get(neighbourAgentCostNames[i]), 
									problem.domains.get(problem.agentDomains.get(agentId)).length, 
									problem.domains.get(problem.agentDomains.get(neighbourAgentIds[i])).length));
				}else{
					int[][] temp = CollectionUtil.toTwoDimension(problem.costs.get(neighbourAgentCostNames[i]), 
							problem.domains.get(problem.agentDomains.get(neighbourAgentIds[i])).length, 
							problem.domains.get(problem.agentDomains.get(agentId)).length);
					
					constraintCosts.put(neighbourAgentIds[i], CollectionUtil.reverse(temp));
				}
			}
			
			if(agentType.equals("ACO")||agentType.equals("ACO_tree")||agentType.equals("ACO_bf")||agentType.equals("ACO_phase")||
					agentType.equals("ACO_line")||agentType.equals("ACO_final")||agentType.equals("SBB")){
				agent.setNeibours(problem.neighbourAgents.get(agentId), problem.parentAgents.get(agentId), 
						problem.childAgents.get(agentId), problem.allParentAgents.get(agentId), 
						problem.allChildrenAgents.get(agentId), neighbourDomains, constraintCosts, neighbourLevels,
						problem.highNodes.get(agentId),problem.lowNodes.get(agentId), problem.priorities.get(agentId), problem.allNodes, problem.maxPriority, problem.minPriority);
			}else{
				agent.setNeibours(problem.neighbourAgents.get(agentId), problem.parentAgents.get(agentId), 
						problem.childAgents.get(agentId), problem.allParentAgents.get(agentId), 
						problem.allChildrenAgents.get(agentId), neighbourDomains, constraintCosts, neighbourLevels);
			}		
			
			agents.put(agent.getId(), agent);
			
			AgentCycle.totalHeight=0;
			for(AgentCycle tempAgent : agents.values()){
				if(AgentCycle.totalHeight < tempAgent.level)
					AgentCycle.totalHeight = tempAgent.level;
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
	
	public int getTotalCost(){
		int totalCost = 0;
		for(AgentCycle agent : agents.values()){
			if(agent != null)
				totalCost += agent.getLocalCost();
		}
		totalCost = totalCost/2;
		return totalCost;
	}
	
}
