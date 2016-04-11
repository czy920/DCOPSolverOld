package com.cqu.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cqu.core.EventListener;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

public class TestMaxSum implements EventListener,Runnable {
	
	private List<Integer> result;
	private int currentIteration = 1;
	private AtomicBoolean flag;
	private File[] problems;
	private int currentProblemIndex;
	private int tmpResult = 0;
	private static String algorithm = "MAXSUM";

	public TestMaxSum() {
		result = new ArrayList<Integer>();
		flag = new AtomicBoolean();
		flag.set(false);
		problems = new File("problems/150_0.3/30").listFiles();
		new Thread(this).start();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Solver solver = new Solver();
		Settings.settings.setDisplayGraphFrame(false);
		Settings.settings.setCycleCount(1);
		TestMaxSum testSuit = new TestMaxSum();
		algorithm = "MAXSUM";
		solver.solve(testSuit.problems[0].getAbsolutePath(), algorithm, false, false, testSuit);
		
	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinished(Object result) {
		// TODO Auto-generated method stub
		tmpResult += ((ResultCycle)result).totalCost;
		synchronized (flag) {
			flag.set(true);
			flag.notifyAll();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			synchronized (flag) {
				while (!flag.get()) {
					try {
						flag.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				currentProblemIndex++;
				if (currentProblemIndex >= problems.length){
					currentProblemIndex = 0;
					result.add(tmpResult/problems.length);
					tmpResult = 0;
					if (++currentIteration >= 100) {
						for(int cost : this.result)
							System.out.println(cost);
						break;
					}
				}
				else {
					flag.set(false);
					Solver solver = new Solver();
					Settings.settings.setDisplayGraphFrame(false);
					Settings.settings.setCycleCount(currentIteration);
					solver.solve(problems[currentProblemIndex].getAbsolutePath(), algorithm, false, false, this);
				}												
			}			
		}
	}

}
