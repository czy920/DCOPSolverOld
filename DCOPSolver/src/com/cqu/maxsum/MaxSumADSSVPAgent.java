package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by dyc on 2017/3/21.
 */
public class MaxSumADSSVPAgent extends AgentCycle {

    private static final int PHASE_EXPLORATION = 0;
    private static final int PHASE_VALUE_PROPAGATION = 1;
    private static final int PHASE_REFINING = 2;
    private static final int PHASE_MODIFICATION = 3;

    private ADVPVariableNode variableNode;
    private Map<Integer,ADVPFunctionNode> functionNodeMap;
    private AgentInfoProvider agentInfoProvider;
    private Map<Integer,Integer> localView;
    private int currentPhase;
    private int currentCycle;
    private int previousValue;
    private int cycleInConvergence;
    private int currentConvergence;
    private AbstractRefiner refiner;
    private int stageSize;
    private String refineAlgorithm;
    private int refineCycle;
    private int maxCycle;
    private boolean enableRefine;
    private int vpTiming;

    public MaxSumADSSVPAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        localView = new HashMap<>();
        previousValue = valueIndex = 0;
        functionNodeMap = new HashMap<>();
        stageSize = Settings.settings.getStageSize();
        refineAlgorithm = Settings.settings.getRefineAlgorithm();
        refineCycle = Settings.settings.getRefineCycle();
        maxCycle = Settings.settings.getCycleCountEnd();
        enableRefine = Settings.settings.isEnableRefine();
        vpTiming = Settings.settings.getVpTiming();
    }

    @Override
    protected void initRun() {
        super.initRun();
        agentInfoProvider = new AgentInfoProvider();
        agentInfoProvider.agentId = this.id;
        agentInfoProvider.domainSize = domain.length;
        agentInfoProvider.controlledFunctionNodes = new HashSet<>();
        agentInfoProvider.neighbours = neighbours;
        agentInfoProvider.neighboursDomainSize = new HashMap<>();
        List<Integer> precursorList = new LinkedList<>();
        for (int neighbourId : neighbours){
            agentInfoProvider.neighboursDomainSize.put(neighbourId,neighbourDomains.get(neighbourId).length);
            if (this.id < neighbourId){
                agentInfoProvider.controlledFunctionNodes.add(neighbourId);
            }
            else {
                precursorList.add(neighbourId);
            }
            localView.put(neighbourId,0);
        }
        for (int controlledNeighbourId : agentInfoProvider.controlledFunctionNodes){
            functionNodeMap.put(controlledNeighbourId,new ADVPFunctionNode(agentInfoProvider,controlledNeighbourId,
                    new LocalFunction(constraintCosts.get(controlledNeighbourId),this.id,controlledNeighbourId,true),controlledNeighbourId));
        }
        variableNode = new ADVPVariableNode(agentInfoProvider,precursorList);
        valueIndex = variableNode.findOptima();
        currentPhase = PHASE_EXPLORATION;
        broadcastValueMessages(variableNode.handle());
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            broadcastMessages(functionNode.handle());
        }
    }

    private void broadcastValueMessages(Message[] messages){
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
            default:
                broadcastMessages(refiner.disposeMessage(msg));
        }
    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        if (currentCycle++ < maxCycle) {
            switch (currentPhase) {
                case PHASE_EXPLORATION:
                    decisionViaMaxSum();
                    if (cycleInConvergence++ == stageSize){
                        currentConvergence++;
                        cycleInConvergence = 0;
                        boolean enableValuePropagation = currentConvergence >= vpTiming;
                        changeDirection(enableValuePropagation);
                        currentPhase = PHASE_VALUE_PROPAGATION;
                    }
                    produceMessages();
                    break;
                case PHASE_VALUE_PROPAGATION:
                    decisionViaMaxSum();
                    if (cycleInConvergence++ == stageSize){
                        currentConvergence++;
                        cycleInConvergence = 0;
                        if (enableRefine){
                            currentPhase = PHASE_REFINING;
                            // Initialize refiner
                            if (AbstractRefiner.REFINER_MGM.equals(refineAlgorithm))
                                refiner = new MGMRefiner(valueIndex,localView,refineCycle,constraintCosts,neighbours,domain.length,id);
                            else
                                refiner = new DSARefiner(valueIndex,localView,refineCycle,constraintCosts,neighbours,domain.length,id);
                            broadcastMessages(refiner.initRun());
                        }
                        else {
                            changeDirection(false);
                            currentPhase = PHASE_EXPLORATION;
                            produceMessages();
                        }
                    }
                    else {
                        produceMessages();
                    }
                    break;
                case PHASE_REFINING:
                    decisionViaRefiner();
                    if (refiner.hasMoreCycle()){
                        broadcastMessages(refiner.allMessageDisposed());
                    }
                    else {
                        currentPhase = PHASE_MODIFICATION;
                        produceMessages();
//                        currentPhase = PHASE_EXPLORATION;
//                        changeDirection(false);
//                        produceMessages();
                    }
                    break;
                case PHASE_MODIFICATION:
                    if (cycleInConvergence++ == stageSize){
                        cycleInConvergence = 0;
                        changeDirection(false);
                        currentPhase = PHASE_EXPLORATION;
                    }
                    produceMessages();
            }
        }
        else {
            stopRunning();
        }
    }

    private void decisionViaMaxSum(){
        valueIndex = variableNode.findOptima();
        sendStatisticMessage();
    }

    private void decisionViaRefiner(){
        valueIndex = refiner.getValueIndex();
        sendStatisticMessage();
    }

    private void sendStatisticMessage(){
        if (valueIndex != previousValue){
            for (int neighbourId : neighbours){
                sendMessage(new Message(this.id,neighbourId,MessageTypes.MSG_TYPE_STATISTIC,valueIndex));
                previousValue = valueIndex;
            }
        }
    }

    private void produceMessages(){
        broadcastValueMessages(variableNode.handle());
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            broadcastMessages(functionNode.handle());
        }
    }
    private void changeDirection(boolean enableValuePropagation){
        variableNode.changeDirection();
        for (ADVPFunctionNode functionNode : functionNodeMap.values()){
            functionNode.changeDirection();
            functionNode.setValuePropagation(enableValuePropagation);
        }
    }

    @Override
    protected void messageLost(Message msg) {
        System.out.println("message lost");
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
