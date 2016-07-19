package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YanChenDeng on 2016/5/19.
 */

public class MaxSumAgent extends AgentCycle implements ParentInfoProvider {


    private static final int PROCESS_TYPE_VALUE = 1;
    private static final int PROCESS_TYPE_FUNCTION = 2;

    private VariableNode variableNode;
    private FunctionNode functionNode;
    private HyperCube localFunction;
    private int processType; // messages to be processed
    private Map<Integer,Integer> lastKnownIndex;
    private int iteration;
    private static Object syncObj = new Object();

    public MaxSumAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        variableNode = new VariableNode(this,false);
        functionNode = new FunctionNode(this,false);
        processType = PROCESS_TYPE_FUNCTION;
        lastKnownIndex = new HashMap<>();
        iteration = Settings.settings.getCycleCountEnd();
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double totalCost = 0;
        for (Map<String,Object> map : results){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("id:"+ map.get("id"));
            stringBuilder.append(" value:" + map.get("value"));
            stringBuilder.append(" dist:" + map.get("dist"));
            System.out.println(stringBuilder.toString());
            totalCost += (int)map.get("cost");
        }
        ResultCycle resultCycle = new ResultCycle();
        resultCycle.totalCost = (int)(totalCost / 2);
        return resultCycle;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        synchronized (syncObj){
           // System.out.println(msg + ":" + ((MessageContent)msg.getValue()).getCube());
        }
        lastKnownIndex.put(msg.getIdSender(),((MessageContent)msg.getValue()).getOptimalIndex());
        if ((msg.getType() & AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE) != 0)
            variableNode.addMessage(msg);
        else if ((msg.getType() & AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION) != 0)
            functionNode.addMessage(msg);
    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    public int getDomainSize() {
        return domain.length;
    }

    @Override
    public HyperCube getLocalFunction() {
        return localFunction;
    }

    @Override
    public void setNeibours(int[] neighbours, int parent, int[] children, int[] allParents,
                            int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer,
            int[][]> constraintCosts, Map<Integer, Integer> neighbourLevels)
    {
        super.setNeibours(neighbours,parent,children,allParents,allChildren,neighbourDomains,constraintCosts,neighbourLevels);
        int[] ids = new int[neighbours.length + 1];
        for (int i = 0; i < neighbours.length; i++){
            ids[i] = neighbours[i];
        }
        ids[ids.length - 1] = id;
        localFunction = new HyperCube(id,constraintCosts,ids,domain.length,true);
        for (int id : neighbours){
            variableNode.addNeighbours(id,neighbourDomains.get(id).length);
            functionNode.addNeighbours(id,neighbourDomains.get(id).length);
        }
        variableNode.addNeighbours(id,domain.length);
        functionNode.addNeighbours(id,domain.length);
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        if (processType == PROCESS_TYPE_FUNCTION){
            broadcastMessages(functionNode.handle());
            processType = PROCESS_TYPE_VALUE;
        }
        else if (processType == PROCESS_TYPE_VALUE){
            broadcastMessages(variableNode.handle());
            processType = PROCESS_TYPE_FUNCTION;
        }
        if (--iteration <= 0)
            stopRunning();
    }

    @Override
    protected void initRun() {
        super.initRun();
        Message[] sendMessages = variableNode.handle();
        for (Message message : sendMessages){
            sendMessage(message);
        }
    }

    private void broadcastMessages(Message[] messages){
        for (Message msg : messages){
            ((MessageContent)msg.getValue()).setOptimalIndex(variableNode.getOptimalIndex());
            sendMessage(msg);
        }
    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        valueIndex = variableNode.getOptimalIndex();
        for (int id : lastKnownIndex.keySet()){
            if (id == this.id)
                continue;
            sum += constraintCosts.get(id)[valueIndex][lastKnownIndex.get(id)];
        }
        localCost = sum;
        return super.getLocalCost();
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("id",id);
        resultMap.put("value",variableNode.getOptimalIndex());
        resultMap.put("cost",getLocalCost());
        resultMap.put("dist",variableNode.getValueDistribution());
        msgMailer.setResult(resultMap);
    }

    @Override
    public HyperCube getLocalFunction(String constraintName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void breakTie(List<Integer> tieList) {

    }
}
