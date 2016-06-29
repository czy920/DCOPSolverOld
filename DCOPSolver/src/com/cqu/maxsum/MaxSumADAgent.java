package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/29.
 */
public class MaxSumADAgent extends AgentCycle implements ParentInfoProvider,PunchedAgentInfoProvider{

    private static final int OPERATION_ALLOCATE_NEIGHBOUR = 1;
    private static final int OPERATION_MESSAGE_PASSING = 2;


    private static Random random;
    private int randomTag;
    private List<Integer> controlledNeighbours;
    private MaxSumADVariableNode variableNode;
    private Map<Integer,MaxSumADFunctionNode> functionNodeMap;
    private int currentOperation;
    private Map<Integer,Integer> lastKnownValueIndex;
    private Map<Integer,Integer> punchCount = new HashMap<>();
    private List<Integer> punchedDomain;
    private Map<Integer,List<Integer>> punchedNeighbourDomains;
    private AbstractLocalRefiner refiner;
    private boolean refinerRunning;
    private boolean inited = false;
    private int stageSize;
    private int repeatTime;


    private static final Object obj = new Object();


    public MaxSumADAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);

        if (random == null){
            random = new Random();
        }
        randomTag = random.nextInt();
        controlledNeighbours = new LinkedList<>();
        functionNodeMap = new HashMap<>();
        lastKnownValueIndex = new HashMap<>();
        punchedDomain = new LinkedList<>();
        punchedNeighbourDomains = new HashMap<>();
        refinerRunning = false;
        stageSize = Settings.settings.getStageSize();
        repeatTime = Settings.settings.getRepeatTime();
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        double totalCost = 0;
        for (Map<String,Object> map : results){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("id:"+ map.get("id"));
            stringBuilder.append(" value:" + map.get("value"));
            stringBuilder.append(" dist:" + map.get("dist"));
            //system.out.println(stringBuilder.toString());
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
    public void setNeibours(int[] neighbours, int parent, int[] children, int[] allParents, int[] allChildren, Map<Integer, int[]> neighbourDomains, Map<Integer, int[][]> constraintCosts, Map<Integer, Integer> neighbourLevels) {
        super.setNeibours(neighbours, parent, children, allParents, allChildren, neighbourDomains, constraintCosts, neighbourLevels);
        for (int id : neighbours){
            controlledNeighbours.add(id);
        }
    }

    @Override
    protected void disposeMessage(Message msg) {
        if (msg.getType() > AbstractNode.MSG_TYPE_PUNCH_DOMAIN){
            currentOperation = msg.getType();
            if (msg.getType() == 64)
                System.out.println(msg + ":value");
            else
                System.out.println(msg + ":gain");
            broadcastMessages(refiner.disposeMessage(msg));
            return;
        }
        if ((msg.getType() & AbstractNode.MSG_TYPE_START) != 0){
            //system.out.println(msg.getType());
            int otherTag = (int) msg.getValue();
//            if (randomTag < otherTag)
//                controlledNeighbours.remove((Object)msg.getIdSender());
//            else if (randomTag == otherTag && id > msg.getIdSender())
//                controlledNeighbours.remove((Object)msg.getIdSender());
            if (id > msg.getIdSender())
                controlledNeighbours.remove((Object)msg.getIdSender());
            currentOperation = OPERATION_ALLOCATE_NEIGHBOUR;
        }
        else {
            if (msg.getIdSender() != id && msg.getValue() instanceof MessageContent)
                lastKnownValueIndex.put(msg.getIdSender(),((MessageContent)msg.getValue()).getOptimalIndex());
            currentOperation = OPERATION_MESSAGE_PASSING;
            if ((msg.getType() & AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION) != 0){
                if (msg.getIdSender() != id)
                    functionNodeMap.get(msg.getIdSender()).addMessage(msg);
                else
                    functionNodeMap.get(((MessageContent)msg.getValue()).getFakeIndex()).addMessage(msg);
//                synchronized (obj) {
//                    if (((MessageContent)msg.getValue()).getCube() != null)
//                        //system.out.println(msg + " v2f,fakeId:" + ((MessageContent) msg.getValue()).getFakeIndex() + ((MessageContent) msg.getValue()).getCube());
//                }

            }
            else if ((msg.getType() & AbstractNode.MSG_TYPE_FUNCTION_TO_VARIABLE) != 0){
//                synchronized (obj) {
//                    if (((MessageContent)msg.getValue()).getCube() != null)
//                        //system.out.println(msg + " f2v," + ((MessageContent) msg.getValue()).getCube());
//                }
                variableNode.addMessage(msg);
            }
            else if (msg.getType() == AbstractNode.MSG_TYPE_PUNCH_DOMAIN){
                //system.out.println(msg + ":punch");
                currentOperation = AbstractNode.MSG_TYPE_PUNCH_DOMAIN;
                punchedNeighbourDomains.put(msg.getIdSender(),(List<Integer>)msg.getValue());
                if (punchedNeighbourDomains.size() == neighbours.length && !inited && refiner != null){
                    broadcastMessages(refiner.initRun());
                    inited = true;
                    return;
                }
            }
        }
    }
    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    protected void allMessageDisposed() {
        super.allMessageDisposed();
        if (refiner != null && !inited){
            if (punchedNeighbourDomains.size() == neighbours.length){
                broadcastMessages(refiner.initRun());
                inited = true;
                return;
            }
        }
        if (currentOperation > AbstractNode.MSG_TYPE_PUNCH_DOMAIN && inited){
            refinerRunning = true;
            broadcastMessages(refiner.allMessageReceived());
            if (refiner.canTerminate())
                stopRunning();
            return;
        }
        if (refiner != null)
            return;
        if (currentOperation == OPERATION_ALLOCATE_NEIGHBOUR){
            currentOperation = OPERATION_MESSAGE_PASSING;
            variableNode = new MaxSumADVariableNode(this,false,Settings.settings.getCycleCountEnd());
            for (int id : controlledNeighbours){
                MaxSumADFunctionNode functionNode = new MaxSumADFunctionNode(this,false,String.valueOf(id),Settings.settings.getCycleCountEnd());
                functionNode.addNeighbours(id,neighbourDomains.get(id).length,this.id > id);
                functionNode.addNeighbours(this.id,domain.length,true);
                //system.out.println(functionNode);
                functionNodeMap.put(id,functionNode);
            }
            for (int id : neighbours){
                if (controlledNeighbours.contains(id)){
                    variableNode.addNeighbours(id,neighbourDomains.get(id).length,false);
                }
                else {
                    variableNode.addNeighbours(id,neighbourDomains.get(id).length,this.id > id);
                }
            }
            //system.out.println(variableNode);
        }
        broadcastFactorGraphMessages(variableNode.handle(),true);
        for (int id : functionNodeMap.keySet()){
            broadcastFactorGraphMessages(functionNodeMap.get(id).handle(),false);
        }
        boolean canTerminate = false;
        if (stageSize-- <= 0){
            canTerminate = repeatTime-- <= 0;
            if (Settings.settings.getRepeatTime() - repeatTime == 2){
                for (int id : functionNodeMap.keySet()){
                    functionNodeMap.get(id).setEnableVP(true);
                }
            }
            if (!canTerminate) {
                variableNode.reverse();
                for (int id : functionNodeMap.keySet()) {
                    functionNodeMap.get(id).reverse();
                }
                stageSize = Settings.settings.getStageSize();
            }
        }
        if (canTerminate){
            if (!Settings.settings.isEnableRefine())
                stopRunning();
            else {
                List<Map.Entry<Integer,Integer>> entryList = new ArrayList<>(variableNode.getValueDistribution().entrySet());
                entryList.sort(new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                valueIndex = variableNode.getOptimalIndex();
                int count = variableNode.getValueDistribution().get(valueIndex);
                punchedDomain.add(valueIndex);
                for (Map.Entry<Integer,Integer> entry : entryList){
                    if ((count * 1.0 / variableNode.getDecisionCount()) > Settings.settings.getDistributionThreshold()){
                        break;
                    }
                    if (entry.getKey() == valueIndex)
                        continue;
                    count += entry.getValue();
                    punchedDomain.add(entry.getKey());
                }
                //system.out.println("id:" + getId() + " domain punched:" + punchedDomain);
                refiner = new MgmRefiner(this,valueIndex);
                broadcastMessage(AbstractNode.MSG_TYPE_PUNCH_DOMAIN,punchedDomain);
            }
        }
    }

    @Override
    protected void initRun() {
        super.initRun();
        broadcastMessage(AbstractNode.MSG_TYPE_START,randomTag);
    }

    private boolean broadcastFactorGraphMessages(Message[] messages, boolean isValueMessage){
        if (messages == null)
            return false;
        for (Message msg : messages){
            if (isValueMessage){
                if (controlledNeighbours.contains(msg.getIdReceiver())){
                    ((MessageContent)msg.getValue()).setFakeIndex(msg.getIdReceiver());
                    msg = new Message(id,id,msg.getType(),msg.getValue());
                }
            }
            ((MessageContent)msg.getValue()).setOptimalIndex(variableNode.getOptimalIndex());
            sendMessage(msg);
        }
        return true;
    }

    private void broadcastMessage(int type,Object value){
        for (int id : neighbours){
            sendMessage(new Message(getId(),id,type,value));
        }
    }
    private void broadcastMessages(Message[] messages){
        if (messages == null)
            return;
        for (Message msg : messages){
            sendMessage(msg);
        }
    }

    @Override
    public int getDomainSize() {
        return domain.length;
    }

    @Override
    public HyperCube getLocalFunction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperCube getLocalFunction(String constraintName) {
        int oppositeId = Integer.parseInt(constraintName);
        Map<Integer,int[][]> utils = new HashMap<>();
        utils.put(oppositeId,constraintCosts.get(oppositeId));
        return new HyperCube(id,utils,new int[]{id,oppositeId},domain.length,true);
    }

    @Override
    public int getLocalCost() {
        try {
            if (refinerRunning) {
                return refiner.getLocalCost();
            }
        }
        catch (Exception e){

        }
        int sum = 0;
        for (int id : lastKnownValueIndex.keySet()){
            sum += constraintCosts.get(id)[variableNode.getOptimalIndex()][lastKnownValueIndex.get(id)];
        }
        return sum;
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("value",Settings.settings.isEnableRefine()?refiner.getValueIndex():variableNode.getOptimalIndex());
        map.put("cost",getLocalCost());
        map.put("dist",variableNode.getValueDistribution());
        msgMailer.setResult(map);
    }

    @Override
    public List<Integer> getPunchedDomain() {
        return punchedDomain;
    }

    @Override
    public Map<Integer, List<Integer>> getNeighbourPunchedDomain() {
        return punchedNeighbourDomains;
    }

    @Override
    public int[] getNeighbour() {
        return neighbours;
    }

    @Override
    public Map<Integer, int[][]> getConstraintCost() {
        return constraintCosts;
    }
}
