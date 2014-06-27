package com.cqu.test;

import com.cqu.core.AgentManager;
import com.cqu.core.DFSTree;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;
import com.cqu.parser.ProblemParser;
import com.cqu.visualtree.TreeFrame;

public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String instance="problems/random_dcop_1.xml";
		//String instance="problems/random_dcop_2.xml";
		String instance="problems/RandomDCOP_7_3_1.xml";
		//String instance="problems/RandomDCOP_7_3_2.xml";
		
		ProblemParser parser=new ProblemParser(instance);
		Problem problem=parser.parse();
		if(problem==null)
		{
			return;
		}
		
		TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(problem.agentNames, problem.parentAgents, problem.childAgents));
		treeFrame.showTreeFrame();
		
		Debugger.init(problem.agentNames);
		Debugger.debugOn=true;
		
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
