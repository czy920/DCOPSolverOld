package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/6/14.
 */
public class MaxSumRefineStructureAgent extends AgentCycle implements ParentInfoProvider{

    public static final int PROCESS_TYPE_FUNCTION = 1;
    public static final int PROCESS_TYPE_VARIABLE = 2;

    private VariableNode variableNode;
    private FunctionNode functionNode;
    private Map<Integer,Integer> lastKnownValueIndex;
    private int randomTag;
    private List<Integer> controlledNeighbours;
    private int currentMsgType;
    private HyperCube localFunction;
    private int processType;
    private int currentIteration;

    private static final Random random = new Random();

    public MaxSumRefineStructureAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        variableNode = new VariableNode(this,false);
        lastKnownValueIndex = new HashMap<>();
        randomTag = random.nextInt();
        controlledNeighbours = new LinkedList<>();
        processType = PROCESS_TYPE_FUNCTION;
        currentIteration = Settings.settings.getCycleCountEnd() + 1;
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
        currentMsgType = msg.getType();
        if (msg.getValue() instanceof MessageContent){
            lastKnownValueIndex.put(msg.getIdSender(),((MessageContent)msg.getValue()).getOptimalIndex());
        }
        if ((currentMsgType & AbstractNode.MSG_TYPE_START) != 0){
            StartMessageContent startMessageContent = (StartMessageContent)msg.getValue();
            if(startMessageContent.degree > neighbours.length){
                controlledNeighbours.remove((Object)msg.getIdSender());
            }
            else if (startMessageContent.degree == neighbours.length){
                if (startMessageContent.randomTag > randomTag){
                    controlledNeighbours.remove((Object)msg.getIdSender());
                }
                else if (startMessageContent.randomTag == randomTag){
                    if (id > msg.getIdSender()){
                        controlledNeighbours.remove((Object)msg.getIdSender());
                    }
                }
            }
        }
        if ((currentMsgType & AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE) != 0){
            variableNode.addMessage(msg);
        }
        else if ((currentMsgType & AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION) != 0){
            functionNode.addMessage(msg);
        }

    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        if ((currentMsgType & AbstractNode.MSG_TYPE_START) != 0){
            for (int neighbourId : neighbours){
                if (!controlledNeighbours.contains(neighbourId)){
                    variableNode.addNeighbours(neighbourId,neighbourDomains.get(neighbourId).length);
                }
            }
            if (controlledNeighbours.size() != 0) {
                functionNode = new FunctionNode(this, false);
                Map<Integer, int[][]> util = new HashMap<>();
                int index = 0;
                int[] ids = new int[controlledNeighbours.size() + 1];
                for (int neighbourId : controlledNeighbours) {
                    util.put(neighbourId, constraintCosts.get(neighbourId));
                    ids[index++] = neighbourId;
                    functionNode.addNeighbours(neighbourId,neighbourDomains.get(neighbourId).length);
                }
                ids[index] = id;
                localFunction = new HyperCube(id, util, ids, domain.length, true);
                functionNode.addNeighbours(id,domain.length);
                variableNode.addNeighbours(id,domain.length);
            }
        }
        if (processType == PROCESS_TYPE_FUNCTION) {
            processType = PROCESS_TYPE_VARIABLE;
            if (functionNode != null) {
                broadcastMessages(functionNode.handle());
            }
        } else if (processType == PROCESS_TYPE_VARIABLE) {
            processType = PROCESS_TYPE_FUNCTION;
            broadcastMessages(variableNode.handle());
        }

        if (currentIteration-- <= 0) {
            stopRunning();
        }

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
    public HyperCube getLocalFunction(String constraintName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void breakTie(List<Integer> tieList) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void initRun() {
        super.initRun();
        StartMessageContent startMessageContent = new StartMessageContent();
        startMessageContent.degree = neighbours.length;
        startMessageContent.randomTag = this.randomTag;
        broadcastMessages(AbstractNode.MSG_TYPE_START,startMessageContent);

    }

    private void broadcastMessages(Message[] messages){
        if (messages == null)
            return;
        for (Message msg : messages){
            ((MessageContent)msg.getValue()).setOptimalIndex(variableNode.getOptimalIndex());
            sendMessage(msg);
        }
    }

    private void broadcastMessages(int type,Object value){
        for (int targetId : neighbours){
            sendMessage(new Message(id,targetId,type,value));
        }
    }

    private class StartMessageContent{
        int randomTag;
        int degree;
    }

    @Override
    public void setNeibours(int[] neighbours, int parent, int[] children, int[] allParents, int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, int[][]> constraintCosts, Map<Integer, Integer> neighbourLevels) {
        super.setNeibours(neighbours, parent, children, allParents, allChildren, neighbourDomains, constraintCosts, neighbourLevels);
        for (int neighbourId : neighbours){
            controlledNeighbours.add(neighbourId);
        }
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("id",id);
        resultMap.put("value",domain[variableNode.getOptimalIndex()]);
        resultMap.put("cost",getLocalCost());
        resultMap.put("dist",variableNode.getValueDistribution());
        msgMailer.setResult(resultMap);
    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        valueIndex = variableNode.getOptimalIndex();
        for (int id : lastKnownValueIndex.keySet()){
            if (id == this.id)
                continue;
            sum += constraintCosts.get(id)[valueIndex][lastKnownValueIndex.get(id)];
        }
        localCost = sum;
        return super.getLocalCost();
    }
}
