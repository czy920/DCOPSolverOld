package com.cqu.ga;

import com.cqu.core.EventListener;
import com.cqu.core.ResultCycleAls;
import com.cqu.core.Solver;

public class TesemMGM2 {
    public static void main(String[] args) {
//         TODO Auto-generated method stub
//        Solver solver = new Solver();
//        solver.batSolve("E:\\DCOP\\problems\\new_problem\\0.1", "Fb_LSGArouMedrouMu", 30, new EventListener() {
//
//            @Override
//            public void onStarted() {
//                // TODO Auto-generated method stub
//            }
//            @Override
//            public void onFinished(Object result) {
//                // TODO Auto-generated method stub
//            }
//        }, new BatSolveListener() {
//
//            public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
//                // TODO Auto-generated method stub
//            }
//        });

        Solver solver = new Solver();
        solver.solve("E:\\DCOP\\problems\\new_problem\\0.1\\RandomDCOP_120_10_10.xml",
                "LSGA_MGM2", false, false, new EventListener() {

                    @Override
                    public void onStarted() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onFinished(Object result) {

                        // TODO Auto-generated method stub
                        ResultCycleAls resultCycleAls = (ResultCycleAls) result;
                        for (int i = 0; i < resultCycleAls.bestCostInCycle.length; i++){
                            System.out.println( i + "\t" + resultCycleAls.bestCostInCycle[i]);
                        }
                    }
                });
    }
}
