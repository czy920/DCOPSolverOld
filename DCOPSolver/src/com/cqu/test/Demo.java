package com.cqu.test;

import com.cqu.core.AgentManager;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;
import com.cqu.parser.ProblemParser;

public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProblemParser parser=new ProblemParser("problems/random_dcop_1.xml");
		Problem problem=parser.parse();
		if(problem==null)
		{
			return;
		}
		
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
