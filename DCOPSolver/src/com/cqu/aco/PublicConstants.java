package com.cqu.aco;

import java.io.File;

import com.cqu.util.FileUtil;

public class PublicConstants {
	//蚂蚁的个数
	public static final String[] ACO_TYPE= {"ACO","ACO_tree","ACO_bf","ACO_phase","ACO_line","ACO_final"};
	public static String ACO_type = "ACO";
	public static int countAnt = 10;
	public static int[] antIds = new int[50];
	static {
		for(int i = 1; i <= 50; i++){
			antIds[i-1] =i;
		}
	}
	//轮数
	public static int MaxCycle = 100;
	public static int realCycle = 0;
	
	public static String path = "result\\";
	//public static String path = "C:\\Users\\hechen\\Desktop\\result\\";
	public static String solution = path + "solution.txt";
	public static String probility =path + "probility.txt";
	public static String bestcost = path+"bestCost.txt";
	public static String totalcost= path + "totalcost.txt";
	public static String tau = path + "tau.txt";
	
	

	//alpha参数
	public static int alpha = 2;
	//beta参数
	public static int beta = 8;
	//rho参数
	public static double rho = 0.02;
	//Min_tau参数
	public static double Min_tau = 0.1;
	//Max_tau参数
	public static double Max_tau = 10;
	
	public static int betterAntCount = 3;
	
	//用于流水线方式时定义存储结构的长度，分析应该是树的深度+2
	public static int dataLength = 50;   
	
	//保存每一轮的代价
	public static double[] aco_bestCostInCycle = new double[PublicConstants.MaxCycle];   //当前发现的最好的解的代价
	public static double[] aco_totalCostInCycle = new double[PublicConstants.MaxCycle];   //每一轮里最好蚂蚁的解的代价
	
	//Delta计算
	//public static double computeDelta(int solution_cost) {
	//	return 1.0 / (1 + solution_cost);
	//}
	
	//Delta计算
	public static double computeLogDelta(int solution_cost){
		
		return 1.0/(Math.log(2+solution_cost)/Math.log(3.0/2.0));
		//return 1.0/(Math.log(2+solution_cost)/Math.log(9.0/5.0));
		//return 1.0/(Math.log(2+solution_cost)/Math.log(2));
		//return 1.0/(Math.log(2+solution_cost));
		//return 1.0/(Math.sqrt(solution_cost));
	}
	
	//tau_agent更新
	public static double update_tau(double tau, double delta){
		double new_tau = -1;
		//new_tau = (1 - rho) * tau + delta;
		new_tau = tau + delta;
		if(new_tau < Min_tau) new_tau = Min_tau;
		if(new_tau > Max_tau) new_tau = Max_tau;
		return new_tau;
		
	}
	//蒸发信息素
	public static void evaporate(double[][] tau, int i, int j){
		tau[i][j] = (1 - rho) * tau[i][j];
		if(tau[i][j] < PublicConstants.Min_tau){
			tau[i][j] = PublicConstants.Min_tau;
		}
		if(tau[i][j] > PublicConstants.Max_tau){
			tau[i][j] = PublicConstants.Max_tau;
		}
	}
	
	/*public static void paramsInit(int maxCycle, int countAnt, int alpha, int beta, double rho, double max_tau, double min_tau){
		PublicConstants.MaxCycle = maxCycle;
		PublicConstants.countAnt = countAnt;
		PublicConstants.alpha = alpha;
		PublicConstants.beta = beta;
		PublicConstants.rho = rho;
		PublicConstants.Max_tau = max_tau;
		PublicConstants.Min_tau = min_tau;
	}*/
	
	//保存每个回合的totalCost
	protected static void dataInCycleIncrease(int cycle, int totalcost, int bestcost) {
		
		while (cycle >= aco_totalCostInCycle.length) {
			double[] templist1 = new double[aco_totalCostInCycle.length + PublicConstants.MaxCycle];
			double[] templist2 = new double[aco_totalCostInCycle.length + PublicConstants.MaxCycle];
			for (int i = 0; i < aco_totalCostInCycle.length; i++) {
				templist1[i] = aco_totalCostInCycle[i];
				templist2[i] = aco_bestCostInCycle[i];
			}
			aco_totalCostInCycle = templist1;
			aco_bestCostInCycle = templist2;
		}
		
		aco_totalCostInCycle[cycle] = totalcost;
		aco_bestCostInCycle[cycle] = bestcost;
	}
	
	//数组长度修正
	public static void dataInCycleCorrection(){
		double[] correcttotalCost = new double[PublicConstants.realCycle+1];
		double[] correctbestCost = new double[PublicConstants.realCycle+1];
		double totalcost = -1;
		double bestcost = -1;
		assert aco_totalCostInCycle[correcttotalCost.length-1] != 0;
		for(int i = 0; i < correcttotalCost.length; i++){
			if(aco_bestCostInCycle[i] != 0){
				totalcost = aco_totalCostInCycle[i];
				bestcost = aco_bestCostInCycle[i];
				correcttotalCost[i] = totalcost;
				correctbestCost[i] = bestcost;
			}else if(totalcost != -1){
				correcttotalCost[i] = totalcost;
				correctbestCost[i] = bestcost;
			}
		}
		aco_totalCostInCycle = correcttotalCost;
		aco_bestCostInCycle = correctbestCost;
	}
	
	public static void writeSolution(String context){
		FileUtil.writeStringAppend(context, solution);
	}
	
	public static void writeTau(String context){
		FileUtil.writeStringAppend(context, tau);
	}
	
	public static void writeBestCost(String context){
		FileUtil.writeStringAppend(context, bestcost);
	}
	
	public static void writeTotalCost(String context){
		FileUtil.writeStringAppend(context, totalcost);
	}
	
	public static void clearFile(){
		File f=new File(path);
		if(f.exists()==false)
		{
			f.mkdir();
		}
		
		FileUtil.writeString("", solution);
		FileUtil.writeString("", probility);
		FileUtil.writeString("", bestcost);
		FileUtil.writeString("", totalcost);
		FileUtil.writeString("", tau);
	}
	
    public static void writeProbility(String context){
    	FileUtil.writeStringAppend(context, probility);
	}

}
