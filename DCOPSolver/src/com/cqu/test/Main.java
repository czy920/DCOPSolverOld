package com.cqu.test;

import java.util.ArrayList;

import com.cqu.core.AgentManager;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Problem problem=new Problem();
		problem.agentCount=4;
		problem.constraintCount=4;
		problem.agentIds=new int[]{0, 1, 2, 3};
		problem.agentNames=new String[]{"A", "B", "C", "D"};
		
		problem.domains=new ArrayList<int[]>();
		problem.domains.add(new int[]{0, 1});
		
		problem.neighbourAgents=new ArrayList<int[]>();
		problem.parentAgents=new ArrayList<Integer>();
		problem.pseudoParentAgents=new ArrayList<int[]>();
		problem.childAgents=new ArrayList<int[]>();
		problem.pseudoChildAgents=new ArrayList<int[]>();
		
		problem.neighbourAgents.add(new int[]{1, 2});
		problem.neighbourAgents.add(new int[]{0, 2, 3});
		problem.neighbourAgents.add(new int[]{0, 1});
		problem.neighbourAgents.add(new int[]{1});
		
		problem.parentAgents.add(-1);
		problem.parentAgents.add(0);
		problem.parentAgents.add(1);
		problem.parentAgents.add(1);
		
		problem.childAgents.add(new int[]{1});
		problem.childAgents.add(new int[]{2, 3});
		problem.childAgents.add(null);
		problem.childAgents.add(null);
		
		problem.pseudoChildAgents.add(new int[]{1, 2});
		problem.pseudoChildAgents.add(new int[]{2, 3});
		problem.pseudoChildAgents.add(null);
		problem.pseudoChildAgents.add(null);
		
		problem.neighbourAgentDomains=new int[]{0, 0, 0, 0};
		
		problem.costs=new ArrayList<int[][]>();
		problem.costs.add(new int[][]{{5, 8}, {20, 3}});
		problem.costs.add(new int[][]{{5, 10}, {20, 3}});
		problem.costs.add(new int[][]{{5, 4}, {3, 3}});
		problem.costs.add(new int[][]{{3, 8}, {10, 3}});
		
		problem.agentConstraintCosts=new ArrayList<int[]>();
		problem.agentConstraintCosts.add(new int[]{0, 1});
		problem.agentConstraintCosts.add(new int[]{0, 2, 3});
		problem.agentConstraintCosts.add(new int[]{1, 2});
		problem.agentConstraintCosts.add(new int[]{3});
		
		MessageMailer msgMailer=new MessageMailer(new AgentManager(problem));
		msgMailer.start();
	}

}
