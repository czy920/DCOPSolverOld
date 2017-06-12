package com.cqu.kopt;

import java.util.List;
import java.util.Map;

/**
 * Created by dyc on 2017/6/5.
 */
public abstract class AbstractEnumerator {
    protected Map<Integer,Map<Integer,int[][]>> constraintView;
    protected Map<Integer,int[]> domainView;
    protected Map<Integer,Integer> valueView;
    protected List<Integer> activeAgents;
    protected int center;

    public AbstractEnumerator(Map<Integer, Map<Integer, int[][]>> constraintView, Map<Integer, int[]> domainView, Map<Integer, Integer> valueView, List<Integer> activeAgents, int center) {
        this.constraintView = constraintView;
        this.domainView = domainView;
        this.valueView = valueView;
        this.activeAgents = activeAgents;
        this.center = center;
    }

    public abstract Assignment enumerate();
}
