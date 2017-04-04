package com.cqu.dgibbs;

import com.cqu.core.Message;
import com.cqu.core.ResultCycle;
import com.cqu.cyclequeue.AgentCycle;
import com.cqu.settings.Settings;

import java.util.*;

/**
 * Created by JXQ on 2017/1/15.
 */
public class DGibbsAgent extends AgentCycle {
    private static final int MSG_TYPE_VALUE = 0;
    private static final int MSG_TYPE_BACKTRACK = 1;
    private static final int MSG_TYPE_D_STAR = 3;

    private int d_star;
    private int d_star_old;
    private int d_hat;
    private int d;
    private Map<Integer,Integer> context;
    private Map<Integer,Integer> optimalContext;
    private int delta_star;
    private int delta;
    private int t;
    private int t_star;
    private Set<Integer> highPriorityNodes;
    private int receivedHighPriorityCount;
    private Map<Integer,BacktrackMessage> childrenMessages;
    private int currentCycle = 1;

    public DGibbsAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        context = new HashMap<>();
        childrenMessages = new HashMap<>();
        optimalContext = new HashMap<>();
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        int cost = 0;
        for (Map<String,Object> map : results){
            cost += (int)map.get("cost");
            System.out.println("id:" + map.get("id") + " val:" + map.get("value"));
        }
        cost /= 2;
        ResultCycle resultCycle = new ResultCycle();
        resultCycle.totalCost = cost;
        return resultCycle;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        System.out.println(msg);
        switch (msg.getType()){
            case MSG_TYPE_VALUE:
                ValueMessage valueMessage = (ValueMessage) msg.getValue();
                context.put(msg.getIdSender(),valueMessage.value);
                if (highPriorityNodes.contains(msg.getIdSender()))
                    receivedHighPriorityCount++;
                if (receivedHighPriorityCount == highPriorityNodes.size()){
                    receivedHighPriorityCount = 0;
                    t += 1;
                    if (valueMessage.t_star == t){
                        d_star = d;
                    }
                    else if (valueMessage.t_star == t - 1 && valueMessage.t_star > t_star){
                        d_star = d_hat;
                    }
                    if (d_star != d_star_old){
                        d_star_old = d_star;
                        for (int neighbourId : neighbours)
                            sendMessage(new Message(this.id,neighbourId,MSG_TYPE_D_STAR,d_star));
                    }
                    delta = valueMessage.delta;
                    delta_star = valueMessage.delta_star;
                    t_star = valueMessage.t_star;
                    sample();
                    if (isLeafAgent()){
                        sendMessage(new Message(this.id,parent,MSG_TYPE_BACKTRACK,new BacktrackMessage(delta,delta_star)));
                        currentCycle++;
                        if (currentCycle > Settings.settings.getCycleCountEnd())
                            stopRunning();
                    }
                }
                break;
            case MSG_TYPE_BACKTRACK:
                childrenMessages.put(msg.getIdSender(),(BacktrackMessage)msg.getValue());
                if (childrenMessages.size() == children.length){
                    currentCycle++;
                    System.out.println("id:" + id + " cycle:" + currentCycle);

                    int deltaTmp = 0;
                    int deltaC = 0;
                    for (int child : childrenMessages.keySet()){
                        deltaTmp += childrenMessages.get(child).delta;
                        deltaC += childrenMessages.get(child).delta_star;
                    }
                    delta = deltaTmp - (children.length - 1) * delta;
                    deltaC -= (children.length - 1) * delta_star;
                    if (deltaC > delta_star){
                        delta_star = deltaC;
                        d_star = d;
                        t_star = t;
                    }
                    if (d_star != d_star_old){
                        d_star_old = d_star;
                        for (int neighbourId : neighbours)
                            sendMessage(new Message(this.id,neighbourId,MSG_TYPE_D_STAR,d_star));
                    }
                    if (!isRootAgent()){
                        sendMessage(new Message(this.id,parent,MSG_TYPE_BACKTRACK,new BacktrackMessage(delta,delta_star)));
                        childrenMessages.clear();
                        if (currentCycle > Settings.settings.getCycleCountEnd())
                            stopRunning();
                    }
                    else {
                        if (currentCycle > Settings.settings.getCycleCountEnd())
                            stopRunning();
                        else {
                            delta -= delta_star;
                            delta_star = 0;
                            t++;
                            childrenMessages.clear();
                            sample();

                        }
                    }
                }
                break;
            case MSG_TYPE_D_STAR:
                optimalContext.put(msg.getIdSender(),(int)msg.getValue());
        }
    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    protected void initRun() {
        super.initRun();
        highPriorityNodes = new HashSet<>();
        if (pseudoParents == null)
            pseudoParents = new int[0];
        for (int i = 0; i < pseudoParents.length; i++)
            highPriorityNodes.add(pseudoParents[i]);
        highPriorityNodes.add(parent);
        d = d_star = d_hat = 0;
        delta = delta_star = 0;
        t = t_star = 0;
        for (int neighbourId : neighbours)
            optimalContext.put(neighbourId,0);
        for (int neighbourId : neighbours){
            context.put(neighbourId,0);
        }
        if (isRootAgent()){
            t++;
            sample();
        }
    }

    private void sample(){
        d_hat =  d;
        double[] nominator = new double[domain.length];
        double denominator = 0;
        for (int i = 0; i < domain.length; i++){
            nominator[i] = Math.exp(-calculateCost(i));
            denominator += nominator[i];
        }
        for (int i = 0; i < domain.length; i++){
            nominator[i] /= denominator;
        }
        double[] pmf = new double[domain.length];
        double left = 0;
        for (int i = 0; i < domain.length; i++){
            pmf[i] = left + nominator[i];
            left += nominator[i];
        }
        double random = Math.random();
        for (int i = 0; i < domain.length; i++){
            if (random < pmf[i]){
                d = i;
                break;
            }
        }

        delta +=  calculateCost(d_hat) - calculateCost(d);
        if (delta > delta_star){
            delta_star = delta;
            d_star = d;
            t_star = t;
        }
        if (d_star != d_star_old){
            d_star_old = d_star;
            for (int neighbourId : neighbours)
                sendMessage(new Message(this.id,neighbourId,MSG_TYPE_D_STAR,d_star));
        }
        ValueMessage valueMessage = new ValueMessage(d,delta,delta_star,t_star);
        for (int neighbourId : neighbours)
            sendMessage(new Message(this.id,neighbourId,MSG_TYPE_VALUE,valueMessage));
    }

    private int calculateCost(int value){
        int sum = 0;
        for (int neighbourId : context.keySet()){
            sum += constraintCosts.get(neighbourId)[value][context.get(neighbourId)];
        }
        return sum;
    }

    private class ValueMessage{
        int value;
        int delta;
        int delta_star;
        int t_star;

        public ValueMessage(int value, int delta, int delta_star, int t_star) {
            this.value = value;
            this.delta = delta;
            this.delta_star = delta_star;
            this.t_star = t_star;
        }
    }

    private class BacktrackMessage{
        int delta;
        int delta_star;

        public BacktrackMessage(int delta, int delta_star) {
            this.delta = delta;
            this.delta_star = delta_star;
        }
    }

    @Override
    public int getLocalCost() {
        int sum = 0;
        for (int neighbourId : neighbours)
            sum += constraintCosts.get(neighbourId)[d_star][optimalContext.get(neighbourId)];
        return sum;
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        Map<String,Object> result = new HashMap<>();
        result.put("id",this.id);
        result.put("value",d_star);
        result.put("cost",getLocalCost());
        msgMailer.setResult(result);
    }
}
