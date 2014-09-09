package com.cqu.test;

import com.cqu.adopt.AdoptAgent;
import com.cqu.bnbadopt.BnBAdoptAgent;
import com.cqu.bnbandadopt.AdoptAgentTwo;
import com.cqu.bnbandadopt.BnBAdoptAgentTwo;
import com.cqu.bnbandadopt.BnBandAdoptAgent;
import com.cqu.bnbmergeadopt.AgentModel;
import com.cqu.core.Agent;
import com.cqu.core.AgentConstructor;
import com.cqu.core.AgentManager;
import com.cqu.core.DFSTree;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;
import com.cqu.core.TreeGenerator;
import com.cqu.parser.ProblemParser;
import com.cqu.visualtree.TreeFrame;

public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String instance="problems/RandomDCOP_10_3_2.xml";
		//parse problem xml
		ProblemParser parser=new ProblemParser(instance);
		Problem problem=parser.parse(TreeGenerator.TREE_GENERATOR_TYPE_DFS);
		if(problem==null)
		{
			return;
		}
		
		//display DFS tree，back edges not included
		TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(problem.agentNames, problem.parentAgents, problem.childAgents));
		treeFrame.showTreeFrame();
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=false;
		
		//construct agents
		AgentManager agentManager=new AgentManager(problem, new AgentConstructor() {
			
			@Override
			public Agent constructAgent(int id, String name, int level, int[] domain) {
				// 返回不同的agent类型
			    
				//返回一个adopt类型的agent
				//return new AdoptAgent(id, name, level, domain);
				//return new AdoptAgentTwo(id,name,level,domain);
				
				//测试adopt与bnbadopt的结合，这里是简单的测试
				//BnBandAdoptAgent agentmodel = new BnBandAdoptAgent(id, name, level, domain);
				//return agentmodel.getAgent();
				
				//返回一个不同策略的agent
				return new AgentModel(id, name, level, domain);
				
				//返回一个bnbadopt类型的agent
				//return new BnBAdoptAgent(id,name,level,domain);
				//return new BnBAdoptAgentTwo(id,name,level,domain);
			}
		});
		
		//start agents and MessageMailer
		MessageMailer msgMailer=new MessageMailer(agentManager);
		agentManager.startAgents(msgMailer);
		msgMailer.start();
	}
}
