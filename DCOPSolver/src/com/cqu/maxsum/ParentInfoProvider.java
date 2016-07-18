package com.cqu.maxsum;

import java.util.List;

/**
 * Created by YanChenDeng on 2016/5/17.
 */
public interface ParentInfoProvider {
    int getId();
    int getDomainSize();
    HyperCube getLocalFunction();
    HyperCube getLocalFunction(String constraintName);
    void breakTie(List<Integer> tieList);
}
