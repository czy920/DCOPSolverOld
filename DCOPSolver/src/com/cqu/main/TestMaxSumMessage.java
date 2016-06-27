package com.cqu.main;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;
import org.omg.CORBA.SetOverrideType;

import java.util.Set;

/**
 * Created by YanChenDeng on 2016/3/22.
 */
public class TestMaxSumMessage {
    public static void main(String[] args){
        Solver solver = new Solver();
        Settings.settings.setEnableRefine(true);
        Settings.settings.setDistributionThreshold(0.8);
        Settings.settings.setCycleCount(130);
        //_20_10_2
        solver.solve("problems/150_0.05/5/RandomDCOP_150_10_2.xml", "MAXSUM", false, false, new EventListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onFinished(Object result) {
                ResultCycle resultCycle = (ResultCycle) result;
                for (int i = 0; i < resultCycle.totalCostInCycle.length; i++){
                    System.out.println( i + "\t" + resultCycle.totalCostInCycle[i]);
                }
                System.out.println("total costs:" + ((Result)result).totalCost);
            }
        });

//        solver.batSolve("problems/150_0.05/5", "MAXSUMRS", 1, new EventListener() {
//            @Override
//            public void onStarted() {
//
//            }
//
//            @Override
//            public void onFinished(Object result) {
//                System.out.println("finished");
//            }
//        }, new Solver.BatSolveListener() {
//            @Override
//            public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
//                System.out.println(problemTotalCount + "/" + problemIndex + "/" + timeIndex);
//            }
//        });
    }
}
