package org.cqu.main;

import org.cqu.core.AgentFactory;
import org.cqu.core.control.MessageMailer;
import org.cqu.problem.parser.Problem;
import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.statistics.StatisticGatherer;
import org.cqu.structure.tree.TreeGenerator.PseudoTreeType;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String problemXmlPath="";
		String agentType="DPOP";
		
		Problem problem=new PseudotreeProblem(problemXmlPath, PseudoTreeType.DFS);
		problem.load();
		
		StatisticGatherer statisticGatherer=new StatisticGatherer();
		MessageMailer msgMailer=new MessageMailer(statisticGatherer, -1);
		AgentFactory agentFactory=new AgentFactory(problem, agentType);
		agentFactory.constructAgents(msgMailer, statisticGatherer);
		msgMailer.setMessageSinks(agentFactory.getAgents());
		
		agentFactory.startAgents();
		msgMailer.start();
	}

}
