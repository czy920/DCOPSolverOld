package com.cqu.varOrdering.priority;

import java.util.List;
import java.util.Map;

import com.cqu.heuristics.ScoringHeuristic;
import com.cqu.util.CollectionUtil;
import com.cqu.varOrdering.dfs.DFSgeneration;

public class PriorityGeneration {
	
	private static PriorityView orderingView;
	private static int priorityBegin;
	
	/** The heuristic used to choose the maxPriority */
	private static ScoringHeuristic<Short> rootElectionHeuristic;
	/** The heuristic used to choose the next node */
	private static ScoringHeuristic<Short> NextNodeHeuristics ;
	
	public PriorityGeneration(Map<Integer, int[]> neighbourNodes){
		orderingView = new PriorityView(neighbourNodes);
		priorityBegin = 1;
	}
	
	public void generate(){
		
		int iteratedCount=0;
		
		orderingView.maxPriority = PriorityGeneration.rootElectionHeuristic.getScores();
		
		Integer curNodeId = orderingView.maxPriority;
		orderingView.nodeIterated.put(curNodeId, true);
		orderingView.priority.put(curNodeId, priorityBegin++);
		iteratedCount++;//根节点已遍历
		int totalCount = orderingView.neighbourNodes.size();
		
		//优先级生成
		while(iteratedCount<totalCount){
			Integer nextNodeId=PriorityGeneration.NextNodeHeuristics.getScores(orderingView);
			orderingView.nodeIterated.put(nextNodeId, true);
			orderingView.priority.put(nextNodeId, priorityBegin++);
		    System.out.println("node: " + nextNodeId + "priority: " + orderingView.priority.get(nextNodeId));
			
			if(iteratedCount == totalCount - 1){
				orderingView.minPriority = nextNodeId;
			}	
			iteratedCount++;
		}
		
		//结点高优先级结点与低优先级结点
		for(Integer nodeId : orderingView.neighbourNodes.keySet()){
			int[] neighbours=orderingView.neighbourNodes.get(nodeId);

			for(Integer Node : neighbours){
				if(orderingView.priority.get(nodeId) > orderingView.priority.get(Node)){
					orderingView.highNodes.get(nodeId).add(Node);
				}else{
					orderingView.lowNodes.get(nodeId).add(Node);
				}
			}
		}	
		
	}
	
	/*
	 * 选择最高优先级结点策略
	 */
	public static void setRootHeuristics (ScoringHeuristic<Short> rootHeuristic){
		PriorityGeneration.rootElectionHeuristic = rootHeuristic;
	}
	/*
	 * 选择下一优先级结点策略
	 */
	public static void setNextNodeHeuristics (ScoringHeuristic<Short> nextNodeHeuristics) 
	{
		PriorityGeneration.NextNodeHeuristics = nextNodeHeuristics ;
	}
	
	public Map<Integer, int[]> getHighNodes(){
		return CollectionUtil.transform(orderingView.highNodes);
	}
	
    public Map<Integer, int[]> getLowNodes(){
    	return CollectionUtil.transform(orderingView.lowNodes);
	}
    
    public Map<Integer, Integer> getPriorities(){
		return orderingView.priority;
	}
    
    public int getMaxPriority(){
    	return orderingView.maxPriority;
    }
    
    public int getMinPriority(){
    	return orderingView.minPriority;
    }
    
    public int[] getAllNodes(){
    	return orderingView.allNodes;
    }

}
