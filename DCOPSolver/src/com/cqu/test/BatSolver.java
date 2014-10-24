package com.cqu.test;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cqu.core.AgentManager;
import com.cqu.core.EventListener;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;
import com.cqu.core.ProblemParser;
import com.cqu.core.TreeGenerator;
import com.cqu.cyclequeue.AgentManagerCycle;
import com.cqu.cyclequeue.MessageMailerCycle;

public class BatSolver {
	
	private static Map<String, Object[]> results;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String problemDir="";
		String agentType="";
		File[] files=new File(problemDir).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if(pathname.getName().endsWith(".xml")==true)
				{
					return true;
				}
				return false;
			}
		});
		
		AtomicBoolean problemSolved=new AtomicBoolean(false);
		for(int i=0;i<files.length;i++)
		{
			solve(files[i].getPath(), agentType, problemSolved);
			
			synchronized (problemSolved) {
				while(problemSolved.get()==false)
				{
					try {
						problemSolved.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Thread.currentThread().interrupt();
						break;
					}
				}
				if(problemSolved.get()==true)
				{
					problemSolved.set(false);
				}
			}
		}
	}
	
	private static void solve(String problemPath, String algorithmType, final AtomicBoolean problemSolved)
	{
		ProblemParser parser = new ProblemParser(problemPath);
		
		Problem problem=null;
		if(algorithmType.equals("BFSDPOP"))
		{
			problem=parser.parse(TreeGenerator.TREE_GENERATOR_TYPE_BFS);
		}else
		{
			problem=parser.parse(TreeGenerator.TREE_GENERATOR_TYPE_DFS);
		}
		if(problem==null)
		{
			synchronized (problemSolved) {
				problemSolved.set(true);
			}
			return;
		}
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=false;
		
		//start agents and MessageMailer
		EventListener el=new EventListener() {
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				synchronized (problemSolved) {
					problemSolved.set(true);
					problemSolved.notifyAll();
				}
			}
		};
		
		//采用同步消息机制的算法
		if(algorithmType.equals("BNBADOPT")||algorithmType.equals("ADOPT"))
		{
			//construct agents
			AgentManagerCycle agentManager=new AgentManagerCycle(problem, algorithmType);
			MessageMailerCycle msgMailer=new MessageMailerCycle(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.start();
			agentManager.startAgents(msgMailer);
		}
		//采用异步消息机制的算法
		else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, algorithmType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.start();
			agentManager.startAgents(msgMailer);
		}
	}

}
