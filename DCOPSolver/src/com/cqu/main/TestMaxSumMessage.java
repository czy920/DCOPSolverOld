package com.cqu.main;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

/**
 * Created by YanChenDeng on 2016/3/22.
 */
public class TestMaxSumMessage {
    public static void main(String[] args){
        Solver solver = new Solver();
        Settings.settings.setCycleCount(3);
        solver.solve("problems/RandomDCOP_3_2_1.xml", "MAXSUMRS", false, false, new EventListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onFinished(Object result) {
                System.out.println("total costs:" + ((Result)result).totalCost);
            }
        });
    }
}
