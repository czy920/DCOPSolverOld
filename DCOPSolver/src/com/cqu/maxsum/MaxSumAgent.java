package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyc on 2017/3/8.
 */
public class MaxSumAgent extends AgentCycle {
    private AgentInfoProvider agentInfoProvider;
    private VariableNode variableNode;
    private Map<Integer,FunctionNode> functionNodeMap; // key: opposite id; value: functionNode
    private int messageReceivedCount;
    private int previousValueIndex;
    private Map<Integer,Integer> localView; // key: opposite id; value: the value that neighbours takes
    private int currentCycle;

    public MaxSumAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        functionNodeMap = new HashMap<>();
        agentInfoProvider = new AgentInfoProvider();
        previousValueIndex = valueIndex = 0;
        localView = new HashMap<>();
        currentCycle = 0;
    }

    @Override
    protected void initRun() {
        super.initRun();
        //Determine the constraints belongers by degree
        for (int neighbourId : neighbours){
            sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_DETERMINE_BELONGER,neighbours.length));
        }
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        int cost = 0;
        for (Map<String,Object> result : results){
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("id:");
            stringBuffer.append(result.get("id"));
            stringBuffer.append(" value:");
            stringBuffer.append(result.get("value"));
            cost += (int)result.get("cost");
        }
        ResultCycle resultCycle = new ResultCycle();
        resultCycle.totalCost = cost / 2;
        return resultCycle;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        switch (msg.getType()){
            case MessageTypes.MSG_TYPE_DETERMINE_BELONGER:
                int oppositeDegree = (int) msg.getValue();
                // if my degree is greater than opposite, then control the constraint matrix
                if (oppositeDegree < neighbours.length){
                    // the construction of FunctionNode need AgentInfoProvider, so the initialization is deferred
                    functionNodeMap.put(msg.getIdSender(),null);
                }
                else if (oppositeDegree == neighbours.length){
                    //if my degree equals opposite, break the tie using agent id
                    if (this.id < msg.getIdSender()){
                        functionNodeMap.put(msg.getIdSender(),null);
                    }
                }
                messageReceivedCount++;
                //if I received all message from neighbours
                if (messageReceivedCount == neighbours.length){
                    //construct AgentInfoProvider
                    Map<Integer,Integer> neighbourDomainSize = new HashMap<>();
                    for (int neighbourId : neighbourDomains.keySet()){
                        neighbourDomainSize.put(neighbourId,neighbourDomains.get(neighbourId).length);
                    }
                    agentInfoProvider.neighboursDomainSize = neighbourDomainSize;
                    agentInfoProvider.neighbours = neighbours;
                    agentInfoProvider.domainSize = domain.length;
                    agentInfoProvider.agentId = id;
                    agentInfoProvider.controlledFunctionNodes = functionNodeMap.keySet();

                    //Initialize localView - default is 0
                    for (int neighbourId : neighbours){
                        localView.put(neighbourId,0);
                    }

                    //Initialize variable node and  function nodes
                    variableNode = new VariableNode(agentInfoProvider);
                    for (int functionId : functionNodeMap.keySet()){
                        functionNodeMap.put(functionId,new FunctionNode(agentInfoProvider,functionId,
                                new LocalFunction(constraintCosts.get(functionId),this.id,functionId,true)));
                    }

                }

                break;
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
        // if current value differs previous value index
        if (valueIndex != previousValueIndex){
            previousValueIndex = valueIndex;
            // inform all neighbours the new value
            for (int neighbourId : neighbours){
                sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_STATISTIC,valueIndex));
            }
        }
        // if there are more available cycles
        if (currentCycle++ < Settings.settings.getCycleCountEnd()) {
            // produce new messages
            broadcastMessages(variableNode.handle());
            for (FunctionNode functionNode : functionNodeMap.values()) {
                broadcastMessages(functionNode.handle());
            }
        }
        else {
            stopRunning();
        }
    }

    private void broadcastMessages(Message[] messages){
        if (messages == null){
            return;
        }
        for (Message msg : messages){
            sendMessage(msg);
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
        Map<String,Object> result = new HashMap<>();
        result.put("id",this.id);
        result.put("value",this.valueIndex);
        result.put("cost",getLocalCost());
        msgMailer.setResult(result);
    }

    @Override
    protected void messageLost(Message msg) {

    }


}
