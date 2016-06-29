package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.*;

/**
 * Created by YanChenDeng on 2016/5/19.
 */
public class VariableNode extends AbstractNode {

    private int optimalIndex;
    private Map<Integer,Integer> valueDistribution;
    private int decisionCount;

    public VariableNode(ParentInfoProvider parent, boolean blocking) {
        super(parent, blocking);
        nodeType = NODE_TYPE_VARIABLE_NODE;
        valueDistribution = new HashMap<>();
        for (int i = 0; i < parent.getDomainSize(); i++){
            valueDistribution.put(i,0);
        }
        decisionCount = 0;
    }

    @Override
    public Message[] handle() {
        if (!evaluateHandleCondition()){
            return null;
        }
        List<Integer> targetList = new ArrayList<>(getDest());
        if (targetId != -1) {
            targetList.retainAll(Arrays.asList(targetId));
        }

        //All messages received, starting find maximum utility
        HyperCube zeroCube = HyperCube.createZeroHyperCube(parent.getId(),parent.getDomainSize());
        if (evaluateFindOptimalCondition()){
            for (int id : comingMessages.keySet()){
                zeroCube.join(comingMessages.get(id));
            }
            HyperCube optimalCube = zeroCube.resolveVariable();
            int maxUtility = Integer.MIN_VALUE;
            int repeatCount = 0;
            for (int i = 0; i < parent.getDomainSize(); i++){
                int util = optimalCube.indexUtil(i);
                if (util > maxUtility){
                    maxUtility = util;
                    optimalIndex = i;
                    repeatCount = 0;
                }
                if (util == maxUtility)
                    repeatCount++;
            }
            decisionCount++;
            valueDistribution.put(optimalIndex, valueDistribution.get(optimalIndex) + 1);
            //System.out.println(parent.getId() + " decision:" + optimalCube +",result:" + optimalIndex + ",repeatCount:" + repeatCount);
        }

        Message[] sendMessages = new Message[targetList.size()];
        int messageIndex = 0;
        for (int id : targetList){
            for (int componentId : getSource()){
                if (id == componentId)
                    continue;
                zeroCube.join(comingMessages.get(componentId));
            }
            MessageContent content = new MessageContent();
            content.setCube(normalization(zeroCube.resolveVariable()));
            sendMessages[messageIndex++] = new Message(parent.getId(),id,AbstractNode.MSG_TYPE_VARIABLE_TO_FUNCTION, content);
        }
        targetId = -1;
        return sendMessages;
    }

    public int getOptimalIndex() {
        return optimalIndex;
    }

    private HyperCube normalization(HyperCube cube){
        assert cube.getVariableCount() == 1: "multi variables are found!";
        int sum = 0;
        for (int i = 0; i < cube.getDomainLength(); i++){
            sum += cube.indexUtil(i);
        }
        int regularizer = sum / cube.getDomainLength();
        int[] tmpUtil = new int[cube.getDomainLength()];
        for (int i = 0; i < tmpUtil.length; i++){
            tmpUtil[i] = cube.indexUtil(i) - regularizer;
        }
        return HyperCube.createSingleHyperCube(cube.getMainId(),tmpUtil,false);
    }

    public int getDecisionCount() {
        return decisionCount;
    }

    public Map<Integer, Integer> getValueDistribution() {
        return valueDistribution;
    }
}
