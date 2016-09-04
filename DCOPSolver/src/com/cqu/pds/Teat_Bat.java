package com.cqu.pds;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.ResultCycleAls;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

public class Teat_Bat {
    public static void main(String[] args){
        Solver solver = new Solver();
        
        Settings.settings.setCycleCount(200);
        Settings.settings.setSelectProbability(0.3);
        Settings.settings.setSelectProbabilityA(0.6);
        Settings.settings.setSelectProbabilityB(0.2);
        Settings.settings.setSelectProbabilityC(0.4);
        Settings.settings.setSelectProbabilityD(0.8);
        Settings.settings.setSelectInterval(1000);
        Settings.settings.setSelectRound(100);
        
        Settings.settings.setDisplayGraphFrame(false);
        EventListener el=new EventListener(){

			public void onStarted() {
				
			}

			public void onFinished(Object result) {
				
			}
        };
        solver.batSolve("D:/Application/DCOPSolver/DCOPSolver/problems/Temp1", "MGM2", 30, el, new Solver.BatSolveListener(){
        
			public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
				
			}
			
        });
    }
}
