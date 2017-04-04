package com.cqu.maxsum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dyc on 2017/3/15.
 */
public class ADVPVariableNode extends VariableNode {
    private List<Integer> precursorList;
    private List<Integer> successorList;
    public ADVPVariableNode(AgentInfoProvider agentInfoProvider,List<Integer> precursorList) {
        super(agentInfoProvider);
        this.precursorList = precursorList;
        successorList = new LinkedList<>();
        for (int neighbourId : agentInfoProvider.neighbours){
            if (!precursorList.contains(neighbourId)){
                successorList.add(neighbourId);
            }
        }
    }

    public void changeDirection(){
        List<Integer> tmp = precursorList;
        precursorList = successorList;
        successorList = tmp;
    }

    @Override
    protected List<Integer> getTarget() {
        return successorList;
    }
}
