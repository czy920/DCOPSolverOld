package com.cqu.maxsum;

/**
 * Created by YanChenDeng on 2016/5/17.
 */
public interface ParentInfoProvider {
    int getId();
    int getDomainSize();
    HyperCube getLocalFunction();
    HyperCube getLocalFunction(String constraintName);
}
