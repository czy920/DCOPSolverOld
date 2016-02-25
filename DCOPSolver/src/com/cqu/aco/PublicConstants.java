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
	public static long currentCycle;
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
	
	//Delta计算
	public static double computeDelta(int solution_cost){
		return 1.0/(1+solution_cost);
	}
	
	//tau_agent更新
	public static double update_tau(double tau, double delta){
		double new_tau = -1;
		new_tau = (1 - rho) * tau + delta;
		if(new_tau < Min_tau) new_tau = Min_tau;
		if(new_tau > Max_tau) new_tau = Max_tau;
		return new_tau;
		
	}	

}
