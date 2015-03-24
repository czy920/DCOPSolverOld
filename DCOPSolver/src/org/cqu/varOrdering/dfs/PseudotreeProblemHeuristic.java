package org.cqu.varOrdering.dfs;

import java.util.HashMap;
import java.util.Map;

import org.cqu.heuristics.MostConnectedHeuristic;
import org.cqu.problem.parser.PseudotreeProblem;
import org.cqu.structure.tree.BFSTree;
import org.cqu.structure.tree.TreeGenerator;
import org.cqu.structure.tree.TreeGenerator.PseudoTreeType;

public class PseudotreeProblemHeuristic extends PseudotreeProblem{

	private Map<String, Integer> relationCost; // 记录每个连接关系矩阵中的最小代价
	private Map<Integer, Integer> agentProperty; // 将agent中的评估代价作为其属性
	
	private Map<String, String> VariableRelation;
	private Map<String, String> VariableValue;
	
	public PseudotreeProblemHeuristic(String path, PseudoTreeType pseudotreeType) {
		super(path, pseudotreeType);
		// TODO Auto-generated constructor stub
		relationCost = new HashMap<String, Integer>(); //约束关系的最值
		agentProperty = new HashMap<Integer, Integer>(); 
		VariableRelation = new HashMap<String, String>(); //对应的relation variable pair
		VariableValue = new HashMap<String, String>(); //对应的relation value pair
	}
	
	@Override
	protected TreeGenerator getGenerator() {
		// TODO Auto-generated method stub
		TreeGenerator treeGenerator;
		if(pseudotreeType==PseudoTreeType.DFS)
		{
			//treeGenerator=new DFSTree(nodeNeighbours);
			treeGenerator = new DFSgeneration(nodeNeighbours);
			DFSgeneration.setRootHeuristics(new MostConnectedHeuristic(this));
			DFSgeneration.setNextNodeHeuristics(new MostConnectedHeuristic(this));
		}else
		{
			treeGenerator=new BFSTree(nodeNeighbours);
		}
		return treeGenerator;
	}
	
	public Integer getNodeProperty(Integer id)
	{
		return agentProperty.get(id);
	}

}
