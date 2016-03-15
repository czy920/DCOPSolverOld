package com.cqu.core;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cqu.cyclequeue.AgentManagerCycle;
import com.cqu.cyclequeue.MessageMailerCycle;
import com.cqu.main.DOTrenderer;
import com.cqu.main.Debugger;
import com.cqu.parser.Problem;
import com.cqu.parser.ProblemParser;
import com.cqu.settings.Settings;
import com.cqu.tree.TreeGenerator;
import com.cqu.util.FileUtil;
import com.cqu.varOrdering.dfs.DFSgeneration;
import com.cqu.visualtree.GraphFrame;
import com.cqu.visualtree.TreeFrame;

public class Solver {
	
	private List<Result> results=new ArrayList<Result>();
	private List<Result> resultsRepeated;
	private String algorithmType = null;

	public void solve(String problemPath, String agentType, boolean showTreeFrame, boolean debug, EventListener el)
	{
		//parse problem xml
		String treeGeneratorType=null;
		if(agentType.equals("BFSDPOP")||agentType.equals("ALSDSA")||agentType.equals("ALSMGM")||agentType.equals("ALSMGM2")||agentType.equals("ALS_DSA")||
				agentType.equals("ALS_H1_DSA")||agentType.equals("ALS_H2_DSA")||agentType.equals("ALSLMUSDSA4")||agentType.equals("ALSDSAMGM"))
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_BFS;
		}else
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_DFS;
		}
		
		Problem problem=null;
		ProblemParser parser=new ProblemParser(problemPath, treeGeneratorType);
		problem=parser.parse();
		
		if(problem==null)
		{
			return;
		}
		
		if(Settings.settings.isDisplayGraphFrame()==true)
		{
			//display constraint graph
			GraphFrame graphFrame=new GraphFrame(problem.neighbourAgents);
			graphFrame.showGraphFrame();
			//new DOTrenderer ("Constraint graph", ProblemParser.toDOT(problemPath));
		}
		//display DFS tree，back edges not included
		if(showTreeFrame==true)
		{
			//TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(problem.agentNames, problem.parentAgents, problem.childAgents));
			//treeFrame.showTreeFrame();
			new DOTrenderer ("DFS tree", DFSgeneration.dfsToString());
		}
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=debug;
		
		//采用同步消息机制的算法
		if(agentType.equals("BNBADOPT")||agentType.equals("BDADOPT")||agentType.equals("ADOPT_K")||agentType.equals("SynAdopt1")||agentType.equals("SynAdopt2")||									
				agentType.equals("DSA_A")||agentType.equals("DSA_B")||agentType.equals("DSA_C")||agentType.equals("DSA_D")||agentType.equals("DSA_E")||
				agentType.equals("MGM")||agentType.equals("MGM2")||agentType.equals("ALSDSA")||agentType.equals("ALSMGM")||agentType.equals("ALSMGM2")||
				agentType.equals("ALS_DSA")||agentType.equals("ALS_H1_DSA")||agentType.equals("ALS_H2_DSA")||agentType.equals("ALSLMUSDSA4")||agentType.equals("ALSDSAMGM")||
				agentType.equals("ACO")||agentType.equals("MAXSUM"))
		//if(agentType.equals("BNBADOPT")||agentType.equals("ADOPT"))
		{
			//construct agents
			AgentManagerCycle agentManagerCycle=new AgentManagerCycle(problem, agentType);
			MessageMailerCycle msgMailer=new MessageMailerCycle(agentManagerCycle);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			
			//先让MsgMailer启动并初始化完成，用while循环判断变量不行，会导致死循环，加一个sleep就行。
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//msgMailer.initWait();						//为避免出现Agent线程开始而Mailer未初始化完成而出现错误
			
			agentManagerCycle.startAgents(msgMailer);
		}
		//采用异步消息机制的算法
		else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, agentType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			agentManager.startAgents(msgMailer);
		}
	}
	
	public void batSolve(final String problemDir, final String agentType, final int repeatTimes, final EventListener el, final BatSolveListener bsl)
	{
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub

				final File[] files = new File(problemDir).listFiles(new FileFilter() {
					
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
				int i=0;
				for(i=0;i<files.length;i++)
				{
					resultsRepeated=new ArrayList<Result>();
					int k=0;
					for(k=0;k<repeatTimes;k++)
					{
						batSolveEach(files[i].getPath(), agentType, problemSolved);
						
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
						
						final int problemIndex=i;
						final int timeIndex=k;
						//refresh progress
						EventQueue.invokeLater(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								bsl.progressChanged(files.length, problemIndex, timeIndex);
							}
							
						});
					}
					if(k<repeatTimes)
					{
						break;
					}
					results.add(disposeRepeated(repeatTimes));
				}
				if(i>=files.length)
				{
					//write results to storage
					writeResultToStorage(problemDir);
					
					el.onFinished(null);
				}
			}
			
		}).start();
	}
	
	private void writeResultToStorage(String problemDir)
	{
		String path;
		if(problemDir.endsWith("\\"))
		{
			path=problemDir+"result";
		}else
		{
			path=problemDir+"\\result";
		}
		File f=new File(path);
		if(f.exists()==false)
		{
			f.mkdir();
		}
		
		Result rs=results.get(0);
		if(rs instanceof ResultAdopt)
		{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			String nccc="";
			for(int i=0;i<results.size();i++)
			{
				ResultAdopt result=(ResultAdopt) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				nccc+=result.nccc+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");
		}else if(rs instanceof ResultDPOP)
		{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			String messageSizeMax="";
			String messageSizeAvg="";
			for(int i=0;i<results.size();i++)
			{
				ResultDPOP result=(ResultDPOP) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=Math.round(result.messageQuantity)+"\n";
				messageSizeMax+=Math.round(result.utilMsgSizeMax)+"\n";
				messageSizeAvg+=Math.round(result.utilMsgSizeAvg)+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(messageSizeMax, path+"\\messageSizeMax.txt");
			FileUtil.writeString(messageSizeAvg, path+"\\messageSizeAvg.txt");
		}
		else if(rs instanceof ResultCycleAls){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myBestCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";
			for(int i=0;i<results.size();i++)
			{
				ResultCycleAls result=(ResultCycleAls) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myBestCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				for(int j = 0; j < result.bestCostInCycle.length; j++)
					myBestCostInCycle += result.bestCostInCycle[j] + "\n";
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myBestCostInCycle, path+"\\bestCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");
		}
		else if(rs instanceof ResultCycle && !this.algorithmType.equals("ACO")){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
			String nccc="";
			String myTotalCostInCycle = "";
			String myTimeCostInCycle = "";
			String myMessageQuantityInCycle = "";
			for(int i=0;i<results.size();i++)
			{
				ResultCycle result=(ResultCycle) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				nccc+=result.nccc+"\n";
				myTotalCostInCycle = "";
				myTimeCostInCycle = "";
				myMessageQuantityInCycle = "";
				for(int j=0; j < result.totalCostInCycle.length; j++){
					myTotalCostInCycle += result.totalCostInCycle[j] + "\n";
					myTimeCostInCycle +=result.timeCostInCycle[j] + "\n";
					myMessageQuantityInCycle +=result.messageQuantityInCycle[j] + "\n";
				}
				FileUtil.writeString(myTotalCostInCycle, path+"\\totalCostInCycle_"+i+".txt");
				FileUtil.writeString(myTimeCostInCycle, path+"\\timeCostInCycle_"+i+".txt");
				FileUtil.writeString(myMessageQuantityInCycle, path+"\\messageQuantityInCycle_"+i+".txt");
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(nccc, path+"\\nccc.txt");
		}
		//蚁群算法引入
		else if(rs instanceof ResultCycle && this.algorithmType.equals("ACO")){
			String totalTime="";
			String messageQuantity="";
			String totalCost="";
		
			String AnttotalCostInCycle = "";
			String AntbestCostInCycle = "";
			
			for(int i=0;i<results.size();i++)
			{
				ResultCycle result=(ResultCycle) results.get(i);
				totalTime+=result.totalTime+"\n";
				messageQuantity+=result.messageQuantity+"\n";
				totalCost+=result.totalCost+"\n";
				
				AnttotalCostInCycle = "";
				AntbestCostInCycle = "";
				
				for(int j = 0; j < result.ant_totalCostInCyle.length; j++){
					AnttotalCostInCycle += result.ant_totalCostInCyle[j] +"\n";
					AntbestCostInCycle += result.ant_bestCostInCycle[j] + "\n";
				}
				FileUtil.writeString(AnttotalCostInCycle, path + "\\AnttotalCostInCycle_" + i + ".txt");
				FileUtil.writeString(AntbestCostInCycle, path + "\\AntbestCostInCycle_" + i + ".txt");
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
		}
		else{
			String totalTime="";
			String totalCost="";
			String messageQuantity="";
			for(int i=0;i<results.size();i++)
			{
				ResultDPOP result=(ResultDPOP) results.get(i);
				totalTime+=result.totalTime+"\n";
				totalCost+=result.totalCost+"\n";
				messageQuantity+=result.messageQuantity+"\n";
			}
			FileUtil.writeString(totalTime, path+"\\totalTime.txt");
			FileUtil.writeString(totalCost, path+"\\totalCost.txt");
			FileUtil.writeString(messageQuantity, path+"\\messageQuantity.txt");
		}
	}
	
	private Result disposeRepeated(int repeatTimes)
	{
		Result rs=resultsRepeated.get(0);
		Result min;
		Result max;
		Result avg;
		if(rs instanceof ResultAdopt)
		{
			min=new ResultAdopt(rs);
			max=new ResultAdopt(rs);
			avg=new ResultAdopt();
		}else if(rs instanceof ResultDPOP)
		{
			min=new ResultDPOP(rs);
			max=new ResultDPOP(rs);
			avg=new ResultDPOP();
		}else if(rs instanceof ResultCycleAls)
		{
			min=new ResultCycleAls(rs);
			max=new ResultCycleAls(rs);
			avg=new ResultCycleAls();
		}else if(rs instanceof ResultCycle)
		{
			min=new ResultCycle(rs);
			max=new ResultCycle(rs);
			avg=new ResultCycle();
		}else
		{
			min=new Result(rs);
			max=new Result(rs);
			avg=new Result();
		}
		avg.add(rs, repeatTimes-2);
		
		for(int i=1;i<resultsRepeated.size();i++)
		{
			Result result=resultsRepeated.get(i);
			min.min(result);
			max.max(result);
			avg.add(result, repeatTimes-2);
		}
		avg.minus(min, repeatTimes-2);
		avg.minus(max, repeatTimes-2);
		return avg;
	}
	
	private void batSolveEach(String problemPath, String algorithmType, final AtomicBoolean problemSolved)
	{
		String treeGeneratorType=null;
		this.algorithmType = algorithmType;
		if(algorithmType.equals("BFSDPOP") || algorithmType.equals("ALSDSA") || algorithmType.equals("ALSMGM")  || algorithmType.equals("ALSMGM2") || algorithmType.equals("ALS_DSA") 
				|| algorithmType.equals("ALS_H1_DSA") || algorithmType.equals("ALS_H2_DSA") || algorithmType.equals("ALSLMUSDSA4") || algorithmType.equals("ALSDSAMGM"))
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_BFS;
		}else
		{
			treeGeneratorType=TreeGenerator.TREE_GENERATOR_TYPE_DFS;
		}
		
		Problem problem=null;
		ProblemParser parser=new ProblemParser(problemPath, treeGeneratorType);
		problem=parser.parse();

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
			public void onFinished(Object result) {
				// TODO Auto-generated method stub
				synchronized (problemSolved) {
					resultsRepeated.add((Result)result);
					problemSolved.set(true);
					problemSolved.notifyAll();
				}
			}
		};
		
		//采用同步消息机制的算法
		if(algorithmType.equals("BNBADOPT")||algorithmType.equals("ADOPT_K")||algorithmType.equals("BDADOPT")||algorithmType.equals("ACO")||algorithmType.equals("SynAdopt1")||algorithmType.equals("SynAdopt2")||
				algorithmType.equals("DSA_A")||algorithmType.equals("DSA_B")||algorithmType.equals("DSA_C")||algorithmType.equals("DSA_D")||algorithmType.equals("DSA_E")||
				algorithmType.equals("MGM")||algorithmType.equals("MGM2")||algorithmType.equals("ALSDSA")||algorithmType.equals("ALSMGM")||algorithmType.equals("ALSMGM2")||algorithmType.equals("ALS_DSA")||
				algorithmType.equals("ALS_H1_DSA")||algorithmType.equals("ALS_H2_DSA")||algorithmType.equals("ALSLMUSDSA4")||algorithmType.equals("ALSDSAMGM"))
		//if(algorithmType.equals("BNBADOPT")||algorithmType.equals("ADOPT"))
		{
			//construct agents
			AgentManagerCycle agentManagerCycle=new AgentManagerCycle(problem, algorithmType);
			MessageMailerCycle msgMailer=new MessageMailerCycle(agentManagerCycle);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			
			//先让MsgMailer启动并初始化完成，用while循环判断变量不行，会导致死循环，加一个sleep就行。
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//msgMailer.initWait();						//为避免出现Agent线程开始而Mailer未初始化完成而出现错误
			
			agentManagerCycle.startAgents(msgMailer);
		}
		//采用异步消息机制的算法
		else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, algorithmType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.startProcess();
			agentManager.startAgents(msgMailer);
		}
	}
	
	public interface BatSolveListener
	{
		void progressChanged(int problemTotalCount, int problemIndex, int timeIndex);
	}

}
