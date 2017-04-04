package com.cqu.maxsum;

import java.util.Map;
import java.util.Set;

/**
 * Created by dyc on 2017/3/6.
 * The wrapper class for host agent information
 */
public class AgentInfoProvider {
    public int domainSize;
    public int agentId;
    public int[] neighbours;
    public Map<Integer,Integer> neighboursDomainSize;
    public Set<Integer> controlledFunctionNodes;
}
