package com.cqu.maxsum;

import com.cqu.core.Message;

/**
 * Created by YanChenDeng on 2016/6/21.
 */
public abstract class AbstractLocalRefiner {
    protected PunchedAgentInfoProvider punchedParent;
    protected int valueIndex;
    public AbstractLocalRefiner(PunchedAgentInfoProvider punchedParent,int initValue){
        this.punchedParent = punchedParent;
        this.valueIndex = initValue;
    }

    public abstract Message[] initRun();
    public abstract Message[] disposeMessage(Message msg);
    public abstract Message[] allMessageReceived();
    public abstract boolean canTerminate();
    public abstract int getLocalCost();

    public int getValueIndex() {
        return valueIndex;
    }
}
