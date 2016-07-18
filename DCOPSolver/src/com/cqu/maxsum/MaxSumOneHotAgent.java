package com.cqu.maxsum;

import com.cqu.core.Message;
import com.cqu.cyclequeue.AgentCycle;

import java.util.*;

/**
 * Created by dyc on 2016/7/18.
 */
public class MaxSumOneHotAgent extends AgentCycle implements ParentInfoProvider{
    private MaxSumADVariableNode variableNode;
    private List<MaxSumADFunctionNode> functionNodeList;
    private Map<Integer,RouteInfo> route;
    private List<Integer> precursorList;
    private List<Integer> successorList;
    private Set<Integer> routeReceived;
    private Set<Integer> arrageReceive;
    private Set<Integer> lastSend;
    private int msgType;
    private boolean received;

    public static final int selectNode = 1;
    public static final int MSG_TYPE_ARRANGE = 64;
    public static final int MSG_TYPE_ROUTE = 128;
    public static final int MSG_TYPE_MARKER = 256;

    public MaxSumOneHotAgent(int id, String name, int level, int[] domain) {
        super(id, name, level, domain);
        variableNode = new MaxSumADVariableNode(this,true,0);
        route = new HashMap<>();
        precursorList = new LinkedList<>();
        successorList = new LinkedList<>();
        routeReceived = new HashSet<>();
        arrageReceive = new HashSet<>();
        lastSend = new HashSet<>();
    }

    @Override
    public Object printResults(List<Map<String, Object>> results) {
        for (Map<String,Object> map : results){
            System.out.println("aa");
        }
        return null;
    }

    @Override
    public String easyMessageContent(Message msg, AgentCycle sender, AgentCycle receiver) {
        return null;
    }

    @Override
    protected void disposeMessage(Message msg) {
        msgType = msg.getType();
        if (msg.getType() == MSG_TYPE_ARRANGE){
            System.out.println(msg);
            arrageReceive.add(msg.getIdSender());
        }
        else if (msg.getType() == MSG_TYPE_ROUTE){
            routeReceived.add(msg.getIdSender());
            mergeRoute((Map<Integer,RouteInfo>)msg.getValue());
        }
        else if (msg.getType() == MSG_TYPE_MARKER){
            Map<String,Object> data = (Map<String,Object>) msg.getValue();

        }
    }

    private void mergeRoute(Map<Integer,RouteInfo> hopInfo) {
        for (int key : hopInfo.keySet()){
            if (!route.containsKey(key)){
                route.put(key,hopInfo.get(key));
                continue;
            }
            if (route.get(key).routeLength > hopInfo.get(key).routeLength){
                route.put(key,hopInfo.get(key));
            }
        }
    }

    @Override
    protected void allMessageDisposed() {
        if (msgType == MSG_TYPE_ARRANGE){
            for (int id : arrageReceive){
                if(lastSend.contains(id)){
                    precursorList.remove((Object)id);
                    if (this.id < id){
                        successorList.add(id);
                    }
                    else {
                        precursorList.add(id);
                    }
                }
                else {
                    successorList.add(id);
                }
            }
            lastSend.clear();
            arrageReceive.clear();
            for (int id : neighbours){
                if (precursorList.contains(id) || successorList.contains(id))
                    continue;
                lastSend.add(id);
                precursorList.add(id);
                sendMessage(new Message(this.id,id,MSG_TYPE_ARRANGE,null));
            }
            if (lastSend.size() == 0 && successorList.size() == neighbours.length && precursorList.size() == 0)
                sendRoute();
        }
        else if (msgType == MSG_TYPE_ROUTE){
            if (routeReceived.size() != precursorList.size()){
                return;
            }
            if (id != selectNode)
                sendRoute();
            else {
                IterationMarker marker = new IterationMarker();
                marker.visited.add(id);
                for (int id : neighbours){
                    marker.unvisited.add(id);
                }
                Map<String,Object> data = new HashMap<>();
                data.put("marker",marker);
                data.put("route",getRouteInfo());
                sendMessage(new Message(id,neighbours[0],MSG_TYPE_MARKER,data));
                precursorList.remove((Object)neighbours[0]);
                successorList.add(neighbours[0]);
            }

        }
    }

    private void sendRoute() {
        Map<Integer, RouteInfo> hopInfos = getRouteInfo();
        for (int id : successorList){
            sendMessage(new Message(this.id,id,MSG_TYPE_ROUTE,hopInfos));
        }
    }

    private Map<Integer, RouteInfo> getRouteInfo() {
        Map<Integer,RouteInfo> hopInfos = new HashMap<>();
        for (int key : route.keySet()){
            hopInfos.put(key,route.get(key).changeAndIncrease(id));
        }
        for (int otherId : neighbours){
            if (hopInfos.containsKey(otherId))
                continue;
            hopInfos.put(otherId,new RouteInfo(id,2));
        }
        hopInfos.put(id,new RouteInfo(id,1));
        return hopInfos;
    }

    @Override
    protected void initRun() {
        super.initRun();
        if (id == selectNode){
            for (int neighbourId : neighbours){
                precursorList.add(neighbourId);
            }
            for (int neighbourId : neighbours){
                sendMessage(new Message(id,neighbourId,MSG_TYPE_ARRANGE,null));
            }

        }
    }

    @Override
    protected void messageLost(Message msg) {

    }

    @Override
    public int getDomainSize() {
        return domain.length;
    }

    @Override
    public HyperCube getLocalFunction() {
        return null;
    }

    @Override
    public HyperCube getLocalFunction(String constraintName) {
        return null;
    }

    @Override
    public void breakTie(List<Integer> tieList) {

    }

    private class IterationMarker{
        Set<Integer> visited;
        Set<Integer> unvisited;
        int iteration;
        public IterationMarker(){
            visited = new HashSet<>();
            unvisited = new HashSet<>();
            iteration = 0;
        }

        public boolean allNodeVisited(){
            return unvisited.size() == 0 && visited.size() != 0;
        }

        public boolean checkAndReset(){
            if (allNodeVisited()){
                iteration++;
                unvisited.clear();
                visited.clear();
                return true;
            }
            return false;
        }
    }

    private class RouteInfo{
        int nextHop;
        int routeLength;

        public RouteInfo(int nextHop, int routeLength) {
            this.nextHop = nextHop;
            this.routeLength = routeLength;
        }

        public RouteInfo() {
        }

        public RouteInfo changeAndIncrease(int id){
            return new RouteInfo(id,routeLength + 1);
        }



        @Override
        public String toString() {
            return "nextHop:" + nextHop + " length:" + routeLength;
        }
    }

    @Override
    protected void runFinished() {
        super.runFinished();
        msgMailer.setResult(new HashMap<String, Object>());
    }
}
