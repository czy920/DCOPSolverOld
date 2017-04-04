package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.Map;

/**
 * Created by dyc on 2017/3/20.
 */
public abstract class AbstractRefiner {

    public static final String REFINER_DSA = "DSA";
    public static final String REFINER_MGM = "MGM";

    protected int valueIndex;
    protected Map<Integer,Integer> localView;
    protected int currentCycle;
    protected int maxCycle;
    protected Map<Integer,int[][]> constraintCost;
    protected int[] neighbours;
    protected int domainLength;
    protected int id;


    public AbstractRefiner(int valueIndex,Map<Integer,Integer> localView,int maxCycle,Map<Integer,int[][]> constraintCost,int[] neighbours,int domainLength,int id){
        this.valueIndex = valueIndex;
        this.localView = localView;
        this.maxCycle = maxCycle;
        this.constraintCost = constraintCost;
        this.neighbours = neighbours;
        this.domainLength = domainLength;
        this.id = id;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public abstract Message[] initRun();

    public abstract Message[] disposeMessage(Message message);

    public abstract Message[] allMessageDisposed();

    public boolean hasMoreCycle(){
        return currentCycle < maxCycle;
    }
}
