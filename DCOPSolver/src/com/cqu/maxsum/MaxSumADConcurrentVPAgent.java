package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by dyc on 2017/3/28.
 */
public class MaxSumADConcurrentVPAgent extends AgentCycle {
    private ADConcurrentVariableNode variableNode;
    private Map<Integer,ADConcurrentFunctionNode> functionNodeMap;
    private Map<Integer,Integer> localView;
    private AgentInfoProvider agentInfoProvider;
    private int maxRepeat;
    private int stageSize;
    private int currentRepeat = 1;
    private int cycleInConvergence;
    private int previousValue;

    public MaxSumADConcurrentVPAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        this.localView = new HashMap<>();
        maxRepeat = Settings.settings.getRepeatTime();
        stageSize = Settings.settings.getStageSize();
        previousValue = valueIndex = 0;
        functionNodeMap = new HashMap<>();
    }

    @Override
    protected void initRun() {
        super.initRun();
        agentInfoProvider = new AgentInfoProvider();
        agentInfoProvider.neighbours = neighbours;
        agentInfoProvider.agentId = id;
        agentInfoProvider.domainSize = domain.length;
        Map<Integer,Integer> neighbourDomainSize = new HashMap<>();
        for (int neighbourId : neighbours){
            neighbourDomainSize.put(neighbourId,neighbourDomains.get(neighbourId).length);
        }
        agentInfoProvider.neighboursDomainSize = neighbourDomainSize;
        Set<Integer> controlledFunctionNodes = new HashSet<>();
        for (int neighbourId : neighbours){
            if (this.id < neighbourId){
                controlledFunctionNodes.add(neighbourId);
            }
            localView.put(neighbourId,0);
        }
        agentInfoProvider.controlledFunctionNodes = controlledFunctionNodes;
        variableNode = new ADConcurrentVariableNode(agentInfoProvider,new LinkedList<>(controlledFunctionNodes));
        for (int neighbourId : controlledFunctionNodes){
            functionNodeMap.put(neighbourId,new ADConcurrentFunctionNode(agentInfoProvider,neighbourId,
                    new LocalFunction(constraintCosts.get(neighbourId),this.id,neighbourId,true),neighbourId));
        }
        if (variableNode.evaluateDecisionCondition()){
            valueIndex = variableNode.findOptima();
            if (valueIndex != previousValue){
                broadcastStatisticMessages();
//                System.out.println("convergence:" + currentRepeat + " id:" + id + " change " + previousValue + " to " + valueIndex);
                previousValue = valueIndex;
            }
        }
        if (controlledFunctionNodes.size() == neighbours.length){
            System.out.println("id:" + id + "  is root");
        }
        else if (controlledFunctionNodes.size() == 0) {
            System.out.println("id:" + id + "  is leaf");
        }
        broadcastValueMessages(variableNode.handle());
    }

    private void broadcastValueMessages(Message[] messages){
        if (messages == null){
            return;
        }
        for (Message message : messages){
            UtilityMessage utilityMessage = (UtilityMessage) message.getValue();
            utilityMessage.setValueIndex(valueIndex);
            sendMessage(message);
        }
    }

    private void broadcastMessage(Message[] messages){
        if (messages == null){
            return;
        }
        for (Message message : messages){
            sendMessage(message);
        }
    }

    private void broadcastStatisticMessages(){
        for (int neighbourId : neighbours){
            sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_STATISTIC,valueIndex));
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        if (currentRepeat <= maxRepeat){
            if (variableNode.evaluateDecisionCondition()){
                valueIndex = variableNode.findOptima();
                if (valueIndex != previousValue){
                    broadcastStatisticMessages();
//                    System.out.println("convergence:" + currentRepeat + " id:" + id + " change " + previousValue + " to " + valueIndex);
                    previousValue = valueIndex;
                }
            }
            broadcastValueMessages(variableNode.handle());
            for (ADConcurrentFunctionNode functionNode : functionNodeMap.values()){
                broadcastMessage(functionNode.handle());
            }
            if (cycleInConvergence++ == stageSize){
                currentRepeat++;
                cycleInConvergence = 0;
                variableNode.reset();
                for (ADConcurrentFunctionNode functionNode : functionNodeMap.values()){
                    functionNode.reset();
                }
            }

        }
        else {
            stopRunning();
        }
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double totalCost = 0;
        for (Map<String,Object> result : results){
            totalCost += (int)result.get("cost");
        }
        ResultCycle resultCycle = new ResultCycle();
        resultCycle.totalCost = (int) (totalCost / 2);
        return resultCycle;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        switch (msg.getType()){
            case MessageTypes.MSG_TYPE_FUNCTION_TO_VARIABLE:
                variableNode.addMessage(msg);
                break;
            case MessageTypes.MSG_TYPE_VARIABLE_TO_FUNCTION:
                UtilityMessage utilityMessage = (UtilityMessage) msg.getValue();
                functionNodeMap.get(utilityMessage.getFakeIndex()).addMessage(msg);
                break;
            case MessageTypes.MSG_TYPE_STATISTIC:
                localView.put(msg.getIdSender(),(int)msg.getValue());
                break;
        }
    }



    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        for (int neighbourId : neighbours){
            sum += constraintCosts.get(neighbourId)[valueIndex][localView.get(neighbourId)];
        }
        return sum;
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        System.out.println("Agent " + id + " stopped");
        Map<String,Object> result = new HashMap<>();
        result.put("id",this.id);
        result.put("val",valueIndex);
        result.put("cost",getLocalCost());
        msgMailer.setResult(result);
    }


}
