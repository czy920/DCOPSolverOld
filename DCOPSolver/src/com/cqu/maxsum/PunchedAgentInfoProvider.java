package com.cqu.maxsum;

import java.util.List;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/6/21.
 */
public interface PunchedAgentInfoProvider {
    List<Integer> getPunchedDomain();
    Map<Integer,List<Integer>> getNeighbourPunchedDomain();
    int[] getNeighbour();
    int getId();
    Map<Integer,int[][]> getConstraintCost();
}
