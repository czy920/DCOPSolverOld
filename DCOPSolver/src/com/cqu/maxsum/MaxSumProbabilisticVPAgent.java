package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by dyc on 2017/4/4.
 */
public class MaxSumProbabilisticVPAgent extends AgentCycle{

    public static final int GRANULARITY_AGENT = 0;
    public static final int GRANULARITY_MESSAGE = 1;

    private ADVPVariableNode variableNode;
    private Map<Integer,ADVPFunctionNode> functionNodeMap;
    private AgentInfoProvider agentInfoProvider;
    private Map<Integer,Integer> localView;
    private int previousValue;
    private int cycleInConvergence;
    private int currentConvergence;
    private int maxConvergence;
    private int stageSize;
    private int granularity;
    private double valuePropagationProbability;


    public MaxSumProbabilisticVPAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        functionNodeMap = new HashMap<>();
        localView = new HashMap<>();
        previousValue = valueIndex = 0;
        currentConvergence = 1;
        maxConvergence = Settings.settings.getRepeatTime();
        stageSize = Settings.settings.getStageSize();
        granularity = Settings.settings.getGranularity();
        valuePropagationProbability = Settings.settings.getValuePropagationProbability();
    }

    @Override
    protected void initRun() {
        super.initRun();
        agentInfoProvider = new AgentInfoProvider();
        agentInfoProvider.agentId = this.id;
        agentInfoProvider.neighbours = neighbours;
        agentInfoProvider.domainSize = domain.length;
        Map<Integer,Integer> neighbourDomainSize = new HashMap<>();
        Set<Integer> controlledFunctionNode = new HashSet<>();
        List<Integer> precursors = new LinkedList<>();
        for (int neighbourId : neighbours){
            neighbourDomainSize.put(neighbourId,neighbourDomains.get(neighbourId).length);
            localView.put(neighbourId,0);
            if (neighbourId < this.id){
                precursors.add(neighbourId);
            }
            else {
                controlledFunctionNode.add(neighbourId);
            }
        }
        agentInfoProvider.neighboursDomainSize = neighbourDomainSize;
        agentInfoProvider.controlledFunctionNodes = controlledFunctionNode;
        variableNode = new ADVPVariableNode(agentInfoProvider,precursors);
        for (int controlledId : controlledFunctionNode){
            functionNodeMap.put(controlledId,new ADVPFunctionNode(agentInfoProvider,controlledId,
                    new LocalFunction(constraintCosts.get(controlledId),this.id,controlledId,true),controlledId));
        }
        decisionAndInform();
        produceMessages();
    }

    private void decisionAndInform(){
        valueIndex = variableNode.findOptima();
        if (valueIndex != previousValue){
            for (int neighbourId : neighbours){
                sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_STATISTIC,valueIndex));
            }
            previousValue = valueIndex;
        }
    }



    private void produceMessages(){
        broadcastVariableNodeMessages(variableNode.handle());
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            broadcastMessages(functionNode.handle());
        }
    }

    private void broadcastVariableNodeMessages(Message[] messages){
        if (messages == null)
            return;
        for (Message message : messages){
            UtilityMessage utilityMessage = (UtilityMessage)message.getValue();
            utilityMessage.setValueIndex(valueIndex);
            sendMessage(message);
        }
    }

    private void broadcastMessages(Message[] messages){
        if (messages == null)
            return;
        for (Message message : messages){
            sendMessage(message);
        }
    }

    private void adjustFunctionNodes(){
        if (currentConvergence < Settings.settings.getVpTiming() + 1){
            return;
        }
        boolean valuePropagation = Math.random() < valuePropagationProbability;
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            if (granularity == GRANULARITY_AGENT){
                functionNode.setValuePropagation(valuePropagation);
            }
            else if (granularity == GRANULARITY_MESSAGE){
                functionNode.setValuePropagation(Math.random() < valuePropagationProbability);
            }
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
    public int getLocalCost() {
        int sum = 0;
        for (int neighbourId : neighbours){
            sum += constraintCosts.get(neighbourId)[valueIndex][localView.get(neighbourId)];
        }
        return sum;
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
        if (currentConvergence <= maxConvergence){
            decisionAndInform();
            if (cycleInConvergence++ == stageSize){
                currentConvergence++;
                cycleInConvergence = 0;
                for (ADVPFunctionNode functionNode : functionNodeMap.values()){
                    functionNode.changeDirection();
                }
                variableNode.changeDirection();
            }
            produceMessages();
            adjustFunctionNodes();
        }
        else {
            stopRunning();
        }
    }

    @Override
    protected void messageLost(Message msg) {

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
