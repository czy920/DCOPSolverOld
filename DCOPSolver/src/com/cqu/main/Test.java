package com.cqu.main;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
		
	}
	public static void main(String[] args) {
		Solver solver = new Solver();
		Settings.settings.setDisplayGraphFrame(false);
		Settings.settings.setCycleCount(30);
//		solver.batSolve("C:\\Users\\YanChenDeng\\git\\DCOPSolver\\problems\\150_0.3\\30", "MAXSUMRS", 1, new EventListener() {
//			@Override
//			public void onStarted() {
//
//			}
//
//			@Override
//			public void onFinished(Object result) {
//				System.out.println("finisihed");
//			}
//		}, new Solver.BatSolveListener() {
//			@Override
//			public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
//				System.out.println("problem count " + problemTotalCount +" current problem index " + problemIndex + "time index " + timeIndex);
//			}
//		});problems/150_0.05/5/RandomDCOP_150_10_2.xml
		Settings.settings.setDisplayGraphFrame(true);
		solver.solve("C:\\Users\\YanChenDeng\\Desktop\\新建文件夹\\scale free network_15_9_0_2_10.xml", "MAXSUMRS", false, false, new EventListener() {

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished(Object result) {
				// TODO Auto-generated method stub
				ResultCycle resultCycle = (ResultCycle) result;
				System.out.println("cost\tmessage");
				for (int i = 0; i < resultCycle.messageQuantityInCycle.length; i++){
					System.out.println(resultCycle.totalCostInCycle[i] + "\t" + resultCycle.messageQuantityInCycle[i]);
				}
				System.out.println(resultCycle.totalCost);
			}
		});
	}

}
