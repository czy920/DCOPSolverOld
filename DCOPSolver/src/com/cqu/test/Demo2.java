package com.cqu.test;

import com.cqu.core.AgentManager;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;

public class Demo2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Problem problem=new Problem();
		problem.agentNames.put(0, "A");
		problem.agentNames.put(1, "B");
		problem.agentNames.put(2, "C");
		problem.agentNames.put(3, "D");
		
		problem.domains.put("D0", new int[]{0, 1});
		
		problem.agentDomains.put(0, "D0");
		problem.agentDomains.put(1, "D0");
		problem.agentDomains.put(2, "D0");
		problem.agentDomains.put(3, "D0");
		
		problem.neighbourAgents.put(0, new int[]{1, 2});
		problem.neighbourAgents.put(1, new int[]{0, 2, 3});
		problem.neighbourAgents.put(2, new int[]{0, 1});
		problem.neighbourAgents.put(3, new int[]{1});
		
		problem.parentAgents.put(0, -1);
		problem.parentAgents.put(1, 0);
		problem.parentAgents.put(2, 1);
		problem.parentAgents.put(3, 1);
		
		problem.pseudoParentAgents.put(0, null);
		problem.pseudoParentAgents.put(1, new int[]{0});
		problem.pseudoParentAgents.put(2, new int[]{0, 1});
		problem.pseudoParentAgents.put(3, new int[]{1});
		
		problem.childAgents.put(0, new int[]{1});
		problem.childAgents.put(1, new int[]{2, 3});
		problem.childAgents.put(2, null);
		problem.childAgents.put(3, null);
		
		problem.pseudoChildAgents.put(0, new int[]{1, 2});
		problem.pseudoChildAgents.put(1, new int[]{2, 3});
		problem.pseudoChildAgents.put(2, null);
		problem.pseudoChildAgents.put(3, null);
		
		problem.costs.put("R0", new int[]{5, 8, 20, 3});
		problem.costs.put("R1", new int[]{5, 10, 20, 3});
		problem.costs.put("R2", new int[]{5, 4, 3, 3});
		problem.costs.put("R3", new int[]{3, 8, 10, 3});
		
		problem.agentConstraintCosts.put(0, new String[]{"R0", "R1"});
		problem.agentConstraintCosts.put(1, new String[]{"R0", "R2", "R3"});
		problem.agentConstraintCosts.put(2, new String[]{"R1", "R2"});
		problem.agentConstraintCosts.put(3, new String[]{"R3"});
		
		start(problem);
	}
	
	private static void start(Problem problem)
	{
		AgentManager agentManager=new AgentManager(problem); 
		MessageMailer msgMailer=new MessageMailer(agentManager);
		
		agentManager.startAgents(msgMailer);
		msgMailer.start();
	}
}
