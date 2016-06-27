package com.cqu.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cqu.core.EventListener;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

public class TestMaxSum implements EventListener,Runnable {
	
	private List<Integer> result;
	private List<Integer> messageCount;
	private int currentIteration = 1;
	private AtomicBoolean flag;
	private File[] problems;
	private int currentProblemIndex;
	private int tmpResult = 0;
	private int tmpMessageCount = 0;
	private static String[] algorithm = {"MGM2","MAXSUM","MAXSUMRS"};
	private static int algorithmIndex = 0;

	public TestMaxSum() {
		result = new ArrayList<Integer>();
	//messageCount = new ArrayList<>();
		flag = new AtomicBoolean();
		flag.set(false);
		problems = new File("problems/test").listFiles();
		new Thread(this).start();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Solver solver = new Solver();
		Settings.settings.setDisplayGraphFrame(false);
		Settings.settings.setCycleCount(1);
		TestMaxSum testSuit = new TestMaxSum();
		solver.solve(testSuit.problems[0].getAbsolutePath(), algorithm[algorithmIndex], false, false, testSuit);
		
	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinished(Object result) {
		// TODO Auto-generated method stub
		tmpResult += ((ResultCycle)result).totalCost;
		tmpMessageCount += ((ResultCycle)result).messageQuantity;
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
					messageCount.add(tmpMessageCount / problems.length);
					tmpResult = 0;
					tmpMessageCount = 0;
					if (++currentIteration >= 100) {

						try {
							OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("D:\\" + algorithm[algorithmIndex]));
							writer.write("cost\tmessageQuantity");
							for(int i = 0; i < result.size();i++){
								writer.write(result.get(i) + "\t" + messageCount.get(i) + "\n");
							}
							writer.close();
						} catch (java.io.IOException e) {
							e.printStackTrace();
						}
						currentIteration = 0;
						result.clear();
						messageCount.clear();
						if (++algorithmIndex == algorithm.length)
							break;
					}
				}

					flag.set(false);
					Solver solver = new Solver();
					Settings.settings.setDisplayGraphFrame(false);
					Settings.settings.setCycleCount(currentIteration);
					solver.solve(problems[currentProblemIndex].getAbsolutePath(), algorithm[algorithmIndex], false, false, this);

			}			
		}
	}

}
