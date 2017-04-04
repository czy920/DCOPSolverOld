package com.cqu.maxsum;

import com.cqu.core.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The abstract parent class for all kind of variable nodes and function nodes
 */
public abstract class AbstractNode {

    protected Map<Integer,UtilityMessage> receivedMessages;
    protected AgentInfoProvider agentInfoProvider;

    /**
     * The constructor
     * @param agentInfoProvider The wrapper class for host agent information
     * */
    public AbstractNode(AgentInfoProvider agentInfoProvider){
        this.agentInfoProvider = agentInfoProvider;
        receivedMessages = new HashMap<>();
    }

    protected int getDomainLength(int id){
        if (id == agentInfoProvider.agentId)
            return agentInfoProvider.domainSize;
        return agentInfoProvider.neighboursDomainSize.get(id);
    }

    /**
     * Init received messages. Called after Constructor
     */
    protected abstract void initReceivedMessages();

    /**
     * Add received message
     * @param message the received message
     */
    public void addMessage(Message message){
        UtilityMessage utility = (UtilityMessage)message.getValue();
        receivedMessages.put(message.getIdSender(),utility);
    }


    /**
     * Determine whether can produce messages
     * @return The indicator
     */
    protected abstract boolean evaluateHandleCondition();

    /**
     * Produce Messages
     * @return The produced messages or null if it cannot produce messages now
     */
    public abstract Message[] handle();

    /**
     * Specify the message targets
     * @return target nodes
     */
    protected abstract List<Integer> getTarget();
}
