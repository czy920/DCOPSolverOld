package com.cqu.aco;

public class PublicConstants {
	//蚂蚁的个数
	public static int countAnt = 2;
	public static int[] antIds = new int[countAnt];
	static {
		for(int i = 1; i <= countAnt; i++){
			antIds[i-1] =i;
		}
	}
	//轮数
	public static long MaxCycle = 100;
	public static int currentCycle;
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
	
	//保存每一轮的代价
	public static int[] aco_bestCostInCycle = new int[999];   //当前发现的最好的解的代价
	public static int[] aco_totalCostInCycle = new int[999];   //每一轮里最好蚂蚁的解的代价
	
	//Delta计算
	public static double computeDelta(int solution_cost){
		return 1.0/(1+solution_cost);
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
	}
	
	public static void paramsInit(long maxCycle, int countAnt, int alpha, int beta, double rho, double max_tau, double min_tau){
		PublicConstants.MaxCycle = maxCycle;
		PublicConstants.countAnt = countAnt;
		PublicConstants.alpha = alpha;
		PublicConstants.beta = beta;
		PublicConstants.rho = rho;
		PublicConstants.Max_tau = max_tau;
		PublicConstants.Min_tau = min_tau;
	}
	
	//保存每个回合的totalCost
	protected static void dataInCycleIncrease(int totalcost, int bestcost) {
		if (currentCycle == 0) // 除去初始化时Cost混乱时的统计
			return;
		if (currentCycle > aco_totalCostInCycle.length) {
			int[] templist1 = new int[2 * aco_totalCostInCycle.length];
			int[] templist2 = new int[2 * aco_totalCostInCycle.length];
			for (int i = 0; i < aco_totalCostInCycle.length; i++) {
				templist1[i] = aco_totalCostInCycle[i];
				templist2[i] = aco_bestCostInCycle[i];
			}
			aco_totalCostInCycle = templist1;
			aco_bestCostInCycle = templist2;
		}
		// System.out.println(cycleCount);
		aco_totalCostInCycle[currentCycle - 1] = totalcost;
		aco_bestCostInCycle[currentCycle - 1] = bestcost;
	}

}
