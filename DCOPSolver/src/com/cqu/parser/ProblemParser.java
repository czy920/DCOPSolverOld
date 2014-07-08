package com.cqu.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.core.DFSTree;
import com.cqu.core.Problem;
import com.cqu.util.CollectionUtil;
import com.cqu.util.XmlUtil;

public class ProblemParser {
	
	public static final String ID="id";
	public static final String NAME="name";
	public static final String TYPE="type";
	public static final String ARITY="arity";
	
	public static final String INSTANCE="instance";
	
	public static final String PRESENTATION="presentation";
	
	public static final String AGENTS="agents";
	public static final String NBAGENTS="nbAgents";
	public static final String AGENT="agent";
	
	public static final String DOMAINS="domains";
	public static final String DOMAIN="domain";
	public static final String NBDOMAINS="nbDomains";
	public static final String NBVALUES="nbValues";
	
	public static final String VARIABLES="variables";
	public static final String NBVARIABLES="nbVariables";
	
	public static final String CONSTRAINTS="constraints";
	public static final String NBCONSTRAINTS="nbConstraints";
	public static final String SCOPE="scope";
	public static final String REFERENCE="reference";
	
	public static final String RELATIONS="relations";
	public static final String NBRELATIONS="nbRelations";
	public static final String NBTUPLES="nbTuples";
	
	
	public static final String TYPE_DCOP="DCOP";
	
	private String xmlPath;
	
	public ProblemParser(String path) {
		// TODO Auto-generated constructor stub
		this.xmlPath=path;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Problem parse()
	{
		Problem problem=new Problem();
		
		Document doc=XmlUtil.openXmlDocument(this.xmlPath);
		Element root=doc.getRootElement();
		
		if(parsePresentation(root.getChild(PRESENTATION))==false)
		{
			this.printMessage("parsePresentation()=false");
			return null;
		}
		
		Map<String, Integer> agentNameIds=parseAgents(root.getChild(AGENTS), problem);
		if(agentNameIds==null)
		{
			this.printMessage("parseAgents() fails!");
			return null;
		}
		
		if(parseDomains(root.getChild(DOMAINS), problem)==false)
		{
			this.printMessage("parseDomains() fails!");
			return null;
		}
		
		Map<String, Integer> variableNameAgentIds=parseVariables(root.getChild(VARIABLES), problem, agentNameIds);
		if(variableNameAgentIds==null)
		{
			this.printMessage("parseVariables() fails!");
			return null;
		}
		
		if(parseRelations(root.getChild(RELATIONS), problem)==false)
		{
			this.printMessage("parseRelations() fails!");
			return null;
		}
		
		if(parseConstraints(root.getChild(CONSTRAINTS), problem, variableNameAgentIds)==false)
		{
			this.printMessage("parseConstraints() fails!");
			return null;
		}
		
		DFSTree dfsTree=new DFSTree(problem.neighbourAgents);
		dfsTree.generate();
		
		problem.parentAgents=dfsTree.getParentNode();
		problem.childAgents=dfsTree.getChildrenNodes();
		Map[] allParentsAndChildren=dfsTree.getAllChildrenAndParentNodes();
		problem.allParentAgents=allParentsAndChildren[0];
		problem.allChildrenAgents=allParentsAndChildren[1];
		
		return problem;
	}
	
	private boolean parsePresentation(Element element)
	{
		if(element==null)
		{
			return false;
		}
		String type=element.getAttributeValue(TYPE);
		if(type.equals(TYPE_DCOP)==false)
		{
			return false;
		}
		return true;
	}
	
	private Map<String, Integer> parseAgents(Element element, Problem problem)
	{
		if(element==null)
		{
			return null;
		}
		int nbAgents=-1;
		try {
			nbAgents=Integer.parseInt(element.getAttributeValue(NBAGENTS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbAgents!=elementList.size())
		{
			printMessage("nbAgents!=elementList.size()");
			return null;
		}
		Map<String, Integer> agentNameIds=new HashMap<String, Integer>();
		try{
			for(int i=0;i<nbAgents;i++)
			{
				int id=Integer.parseInt(elementList.get(i).getAttributeValue(ID));
				String name=elementList.get(i).getAttributeValue(NAME);
				
				agentNameIds.put(name, id);
				problem.agentNames.put(id, name);
			}
		}catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return agentNameIds;
	}
	
	private boolean parseDomains(Element element, Problem problem)
	{
		if(element==null)
		{
			return false;
		}
		int nbDomains=-1;
		try {
			nbDomains=Integer.parseInt(element.getAttributeValue(NBDOMAINS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbDomains!=elementList.size())
		{
			printMessage("nbDomains!=elementList.size()");
			return false;
		}
		
		for(int i=0;i<nbDomains;i++)
		{
			int[] domain=parseFromTo(elementList.get(i).getValue());
			int nbValues=Integer.parseInt(elementList.get(i).getAttributeValue(NBVALUES));
			if(nbValues!=domain.length)
			{
				printMessage("nbValues!=domain.length");
				return false;
			}
			problem.domains.put(elementList.get(i).getAttributeValue(NAME), domain);
		}
		
		return true;
	}
	
	private int[] parseFromTo(String fromToStr)
	{
		int from=-1;
		int to=-1;
		String separator="..";
		
		fromToStr=fromToStr.trim();
		int sepIndex=fromToStr.indexOf(separator);
		from=Integer.parseInt(fromToStr.substring(0, sepIndex));
		to=Integer.parseInt(fromToStr.substring(sepIndex+separator.length()));
		
		int[] ret=new int[to-from+1];
		for(int i=0;i<ret.length;i++)
		{
			ret[i]=i+from;
		}
		
		return ret;
	}
	
	private Map<String, Integer> parseVariables(Element element, Problem problem, Map<String, Integer> agentNameIds)
	{
		if(element==null)
		{
			return null;
		}
		int nbVariables=-1;
		try {
			nbVariables=Integer.parseInt(element.getAttributeValue(NBVARIABLES));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbVariables!=elementList.size())
		{
			printMessage("nbVariables!=elementList.size()");
			return null;
		}
		if(nbVariables!=problem.agentNames.size())
		{
			printMessage("nbVariables!=problem.agentCount，要求每个agent中只包含一个variable");
			return null;
		}
		Map<String, Integer> variableNameAgentIds=new HashMap<String, Integer>();
		for(int i=0;i<nbVariables;i++)
		{
			int agentId=agentNameIds.get(elementList.get(i).getAttributeValue(AGENT));
			variableNameAgentIds.put(elementList.get(i).getAttributeValue(NAME), agentId);
			problem.agentDomains.put(agentId, elementList.get(i).getAttributeValue(DOMAIN));
		}
		return variableNameAgentIds;
	}
	
	private boolean parseRelations(Element element, Problem problem)
	{
		if(element==null)
		{
			return false;
		}
		int nbRelations=-1;
		try {
			nbRelations=Integer.parseInt(element.getAttributeValue(NBRELATIONS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbRelations!=elementList.size())
		{
			printMessage("nbRelations!=elementList.size()");
			return false;
		}
		
		for(int i=0;i<nbRelations;i++)
		{
			int arity=Integer.parseInt(elementList.get(i).getAttributeValue(ARITY));
			if(arity!=2)
			{
				printMessage("arity!=2");
				return false;
			}
			int[] cost=paseConstraintCost(elementList.get(i).getValue());
			int nbTuples=Integer.parseInt(elementList.get(i).getAttributeValue(NBTUPLES));
			if(nbTuples!=cost.length)
			{
				printMessage("nbValues!=cost length");
				return false;
			}
			problem.costs.put(elementList.get(i).getAttributeValue(NAME), cost);
		}
		return true;
	}
	
	private int[] paseConstraintCost(String costStr)
	{
		String[] items=costStr.split("\\|");
		String[] costParts=new String[items.length];
		Map<String, Integer> valuePairParts=new HashMap<String, Integer>();
		int index=0;
		for(int i=0;i<items.length;i++)
		{
			index=items[i].indexOf(':');
			costParts[i]=items[i].substring(0, index);
			valuePairParts.put(items[i].substring(index+1), i);
		}
		Object[] valuePairPartsKeyArray=valuePairParts.keySet().toArray();
		Arrays.sort(valuePairPartsKeyArray);
		
		int[] costs=new int[items.length];
		for(int i=0;i<items.length;i++)
		{
			costs[i]=Integer.parseInt(costParts[valuePairParts.get(valuePairPartsKeyArray[i])]);
		}
		return costs;
	}
	
	private boolean parseConstraints(Element element, Problem problem, Map<String, Integer> variableNameAgentIds)
	{
		if(element==null)
		{
			return false;
		}
		int nbConstraints=-1;
		try {
			nbConstraints=Integer.parseInt(element.getAttributeValue(NBCONSTRAINTS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbConstraints!=elementList.size())
		{
			printMessage("nbConstraints!=elementList.size()");
			return false;
		}
		
		//图的邻接表存储结构
		Map<Integer, List<Integer>> neighbourAgents=new HashMap<Integer, List<Integer>>();
		Map<Integer, Map<Integer, String>> neighbourConstraintCosts=new HashMap<Integer, Map<Integer, String>>();
		for(Integer agentId : problem.agentNames.keySet())
		{
			neighbourAgents.put(agentId, new ArrayList<Integer>());
			neighbourConstraintCosts.put(agentId, new HashMap<Integer, String>());
		}
		
		for(int i=0;i<nbConstraints;i++)
		{
			int arity=Integer.parseInt(elementList.get(i).getAttributeValue(ARITY));
			if(arity!=2)
			{
				printMessage("arity!=2");
				return false;
			}
			String[] constraintedParts=elementList.get(i).getAttributeValue(SCOPE).split(" ");
			int leftAgentId=variableNameAgentIds.get(constraintedParts[0]);
			int rightAgentId=variableNameAgentIds.get(constraintedParts[1]);
			if(leftAgentId>rightAgentId)//保证id小的agent放在行的位置，id大的放在列的位置
			{
				int temp=leftAgentId;
				leftAgentId=rightAgentId;
				rightAgentId=temp;
			}
			neighbourAgents.get(leftAgentId).add(rightAgentId);
			neighbourAgents.get(rightAgentId).add(leftAgentId);
			neighbourConstraintCosts.get(leftAgentId).put(rightAgentId, elementList.get(i).getAttributeValue(REFERENCE));
			neighbourConstraintCosts.get(rightAgentId).put(leftAgentId, elementList.get(i).getAttributeValue(REFERENCE));
		}
		for(Integer agentId : problem.agentNames.keySet())
		{
			List<Integer> temp=neighbourAgents.get(agentId);
			Integer[] buff=new Integer[temp.size()];
			problem.neighbourAgents.put(agentId, CollectionUtil.toInt(temp.toArray(buff)));
			
			String[] costNames=new String[temp.size()];
			for(int i=0;i<buff.length;i++)
			{
				costNames[i]=neighbourConstraintCosts.get(agentId).get(buff[i]);
			}
			problem.agentConstraintCosts.put(agentId, costNames);
		}
		
		return true;
	}
	
	private void printMessage(String msg)
	{
		System.out.println(msg);
	}
}
