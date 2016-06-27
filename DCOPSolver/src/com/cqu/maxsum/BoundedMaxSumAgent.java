package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.cyclequeue.AgentCycle;

import java.util.List;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/6/16.
 */
public class BoundedMaxSumAgent extends AgentCycle {

    private List<Integer> fragmentView;
    private Map<Integer,Integer> suggestReceived;

    public BoundedMaxSumAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        return null;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {

    }

    @Override
    protected void messageLost(Message msg) {

    }


}
