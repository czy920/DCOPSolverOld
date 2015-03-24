package org.cqu.problem.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.utility.CollectionUtil;


public class Problem{
	
	private String path;
	
	protected Map<String, int[]> domains;
	protected Map<String, int[]> constraints;
	
	protected List<Integer> nodeIds;
	protected Map<Integer, String> nodeNames;
	protected Map<Integer,String> nodeDomains;
	protected Map<Integer, int[]> nodeNeighbours;
	protected Map<Integer, String[]> nodeConstraints;
	
	public Problem(String path) {
		// TODO Auto-generated constructor stub
		this.path=path;
		
		domains=new HashMap<String, int[]>();
		constraints=new HashMap<String, int[]>();
		
		nodeIds=new ArrayList<Integer>();
		nodeNames=new HashMap<Integer, String>();
		nodeDomains=new HashMap<Integer, String>();
		nodeNeighbours=new HashMap<Integer, int[]>();
		nodeConstraints=new HashMap<Integer, String[]>();
	}
	
	public void load()
	{
		if(path==null||path.isEmpty())
		{
			return;
		}
		File f=new File(path);
		if(f.exists()==false||f.isFile()==false)
		{
			return;
		}
		
		ProblemParser parser=new ProblemParser(path);
		parser.parse(this);
	}
	
	public List<Integer> getNodeIds()
	{
		return this.nodeIds;
	}
	
	public String getNodeName(Integer id)
	{
		return nodeNames.get(id);
	}
	
	public void putNodeName(Integer id, String name)
	{
		nodeNames.put(id, name);
	}
	
	public void putDomain(String name, int[] domain)
	{
		domains.put(name, domain);
	}
	
	public int[] getNodeDomain(Integer id)
	{
		return domains.get(nodeDomains.get(id));
	}
	
	public void putNodeDomain(Integer id, String domainName)
	{
		nodeDomains.put(id, domainName);
	}
	
	public Map<Integer, int[]> getNodeNeighbourDomains(Integer id)
	{
		Map<Integer, int[]> neighbourDomains=new HashMap<Integer, int[]>();
		int[] neighbours=nodeNeighbours.get(id);
		for(int i=0;i<neighbours.length;i++)
		{
			neighbourDomains.put(neighbours[i], domains.get(nodeDomains.get(neighbours[i])));
		}
		return neighbourDomains;
	}
	
	public int[] getNodeNeighbours(Integer id)
	{
		return nodeNeighbours.get(id);
	}
	
	public void putNodeNeighbours(Integer id, int[] neighbours)
	{
		nodeNeighbours.put(id, neighbours);
	}
	
	public void putConstraintValues(String name, int[] values)
	{
		constraints.put(name, values);
	}
	
	public Map<Integer, int[][]> getNodeConstraintValues(Integer id)
	{
		Map<Integer, int[][]> nodeConstraintMatrixes=new HashMap<Integer, int[][]>();
		String[] indexes=nodeConstraints.get(id);
		int[] neighbours=nodeNeighbours.get(id);
		for(int i=0;i<indexes.length;i++)
		{
			nodeConstraintMatrixes.put(neighbours[i], 
					CollectionUtil.toTwoDimension(constraints.get(indexes), domains.get(id).length, domains.get(neighbours[i]).length));
		}
		return nodeConstraintMatrixes;
	}
	
	public void putNodeConstraints(Integer id, String[] constraintNames)
	{
		nodeConstraints.put(id, constraintNames);
	}
}
