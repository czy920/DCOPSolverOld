package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by dyc on 2017/3/15.
 */
public class MaxSumADVPAgent extends AgentCycle {

    private ADVPVariableNode variableNode;
    private Map<Integer,ADVPFunctionNode> functionNodeMap;
    private AgentInfoProvider agentInfoProvider;
    private Map<Integer,Integer> localView;
    private int currentCycle;
    private int previousValueIndex;

    public MaxSumADVPAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        functionNodeMap = new HashMap<>();
        agentInfoProvider = new AgentInfoProvider();
        localView = new HashMap<>();
        previousValueIndex = valueIndex = 0;
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
        switch (msg.getType()) {
            case MessageTypes.MSG_TYPE_FUNCTION_TO_VARIABLE:
                variableNode.addMessage(msg);
                break;
            case MessageTypes.MSG_TYPE_VARIABLE_TO_FUNCTION:
                UtilityMessage utilityMessage = (UtilityMessage) msg.getValue();
                functionNodeMap.get(utilityMessage.getFakeIndex()).addMessage(msg);
                break;
            case MessageTypes.MSG_TYPE_STATISTIC:
                localView.put(msg.getIdSender(),(int)msg.getValue());
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        // calculate current optima
        valueIndex = variableNode.findOptima();
        if (valueIndex != previousValueIndex){
            for (int neighbourId : neighbours){
                sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_STATISTIC,valueIndex));
            }
            previousValueIndex = valueIndex;
        }
        // if there are more available cycles
        if (currentCycle++ < Settings.settings.getStageSize() * Settings.settings.getRepeatTime()) {
            //if current cycle reaches the longest path in current convergence
            if (currentCycle % Settings.settings.getStageSize() == 0){
                // reverse the direction
                variableNode.changeDirection();
                for (ADVPFunctionNode functionNode : functionNodeMap.values()){
                    functionNode.changeDirection();
                    // set the value propagation flag
                    int currentConvergence = currentCycle / Settings.settings.getStageSize();
                    if (Settings.settings.isEnableVP() &&  currentConvergence >= Settings.settings.getVpTiming()){
                        functionNode.setValuePropagation(true);
                    }

                }
            }
            // produce new messages
            broadcastValueMessages(variableNode.handle());
            for (ADVPFunctionNode functionNode : functionNodeMap.values()) {
                broadcastFunctionMessage(functionNode.handle());
            }
        }
        else {
            stopRunning();
        }
    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    protected void initRun() {
        super.initRun();
        //Initialization for AgentInfoProvider
        agentInfoProvider.agentId = this.id;
        agentInfoProvider.neighbours = neighbours;
        agentInfoProvider.domainSize = domain.length;
        Map<Integer,Integer> neighbourDomainSize = new HashMap<>();
        Set<Integer> controlledFunctionNodes = new HashSet<>();
        List<Integer> precursorList = new LinkedList<>(); // The function nodes which order before variable node
        for (int neighbourId : neighbours){
            neighbourDomainSize.put(neighbourId,neighbourDomains.get(neighbourId).length);
            // If I have a smaller id, then take charge of the constraint (function node)
            if (this.id < neighbourId){
                controlledFunctionNodes.add(neighbourId);
            }
            else {
                // else the function node orders before variable node
                precursorList.add(neighbourId);
            }
            localView.put(neighbourId,0);
        }
        agentInfoProvider.neighboursDomainSize = neighbourDomainSize;
        agentInfoProvider.controlledFunctionNodes = controlledFunctionNodes;
        for (Integer controlledId : controlledFunctionNodes){
            //Initialize function nodes
            functionNodeMap.put(controlledId,new ADVPFunctionNode(agentInfoProvider,controlledId,
                    new LocalFunction(constraintCosts.get(controlledId),this.id,controlledId,true),controlledId));
        }
        variableNode = new ADVPVariableNode(agentInfoProvider,precursorList);
        valueIndex = variableNode.findOptima();

        // All nodes are initialized, now can make decision and perform message-passing
        broadcastValueMessages(variableNode.handle());
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            broadcastFunctionMessage(functionNode.handle());
        }
    }

    /**
     * Attach each message with the value selected by variable node, and broadcast messages
     * @param messages messages produced by variable node
     */
    private void broadcastValueMessages(Message[] messages){
        if (messages == null)
            return;
        for (Message message : messages){
            UtilityMessage utilityMessage = (UtilityMessage) message.getValue();
            utilityMessage.setValueIndex(valueIndex);
            sendMessage(message);
        }
    }

    private void broadcastFunctionMessage(Message[] messages){
        if (messages == null)
            return;
        for (Message message : messages){
            sendMessage(message);
        }
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
