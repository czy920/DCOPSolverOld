package com.cqu.main;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.maxsum.AbstractLocalRefiner;
import com.cqu.settings.Settings;
import org.omg.CORBA.SetOverrideType;

import java.io.File;
import java.util.Set;

/**
 * Created by YanChenDeng on 2016/3/22.
 */
public class TestMaxSumMessage {
    public static void main(String[] args){
        Solver solver = new Solver();
        Settings.settings.setEnableRefine(false);
        Settings.settings.setEnableVP(false);
        Settings.settings.setDistributionThreshold(0.8);
        Settings.settings.setStageSize(6);
        Settings.settings.setRepeatTime(2);
        Settings.settings.setCycleCount(20);
        Settings.settings.setRefineAlgorithm(AbstractLocalRefiner.REFINE_ALGORITHM_DSA);
        Settings.settings.setSelectProbability(0.9);
        Settings.settings.setDisplayGraphFrame(true);
        //_20_10_2         MAXSUMSPLITED
            solver.solve("DCOPSolver/problems/RandomDCOP_7_3_1.xml", "MAXSUMOH", false, false, new EventListener() {
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

//        solver.batSolve("C:\\Users\\dyc\\Desktop\\150_10_5\\5", "DSA_A", 3, new EventListener() {
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
