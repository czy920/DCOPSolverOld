package com.cqu.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.core.Problem;
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
		
		Map<String, Integer> agentNameIndexes=parseAgents(root.getChild(AGENTS), problem);
		if(agentNameIndexes==null)
		{
			this.printMessage("parseAgents()=false");
			return null;
		}
		
		Map<String, Integer> domainNameIndexes=parseDomains(root.getChild(DOMAINS), problem);
		if(domainNameIndexes==null)
		{
			this.printMessage("parseDomains()=false");
			return null;
		}
		
		Map<String, Integer> variableNameIndexes=parseVariables(root.getChild(VARIABLES), problem, agentNameIndexes, domainNameIndexes);
		if(variableNameIndexes==null)
		{
			this.printMessage("parseVariables()=false");
			return null;
		}
		
		Map<String, Integer> relationNameIndexes=parseRelations(root.getChild(RELATIONS), problem);
		if(relationNameIndexes==null)
		{
			this.printMessage("parseRelations()=false");
			return null;
		}
		
		
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
		
		Map<String, Integer> agentNameIndexes=new HashMap<String, Integer>();
		int[] agentIds=new int[nbAgents];
		String[] agentNames=new String[nbAgents];
		try{
			for(int i=0;i<agentIds.length;i++)
			{
				agentIds[i]=Integer.parseInt(elementList.get(i).getAttributeValue(ID));
				agentNames[i]=elementList.get(i).getAttributeValue(NAME);
				agentNameIndexes.put(agentNames[i], i);
			}
		}catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		problem.agentCount=nbAgents;
		problem.agentIds=agentIds;
		problem.agentNames=agentNames;
		
		return agentNameIndexes;
	}
	
	private Map<String, Integer> parseDomains(Element element, Problem problem)
	{
		if(element==null)
		{
			return null;
		}
		int nbDomains=-1;
		try {
			nbDomains=Integer.parseInt(element.getAttributeValue(NBDOMAINS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbDomains!=elementList.size())
		{
			printMessage("nbDomains!=elementList.size()");
			return null;
		}
		
		Map<String, Integer> domainNameIndexes=new HashMap<String, Integer>();
		List<int[]> domains=new ArrayList<int[]>();
		for(int i=0;i<nbDomains;i++)
		{
			domainNameIndexes.put(elementList.get(i).getAttributeValue(NAME), i);
			int[] domain=parseFromTo(elementList.get(i).getValue());
			int nbValues=Integer.parseInt(elementList.get(i).getAttributeValue(NBVALUES));
			if(nbValues!=domain.length)
			{
				printMessage("nbValues!=domain.length");
				return null;
			}
			domains.add(domain);
		}
		
		problem.domains=domains;
		return domainNameIndexes;
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
	
	private Map<String, Integer> parseVariables(Element element, Problem problem, Map<String, Integer> agentNameIndexes, Map<String, Integer> domainNameIndexes)
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
		if(nbVariables!=problem.agentCount)
		{
			printMessage("nbVariables!=problem.agentCount，要求每个agent中只包含一个variable");
			return null;
		}
		
		Map<String, Integer> variableNameIndexes=new HashMap<String, Integer>();
		int[] agentDomains=new int[nbVariables];
		for(int i=0;i<nbVariables;i++)
		{
			variableNameIndexes.put(elementList.get(i).getAttributeValue(NAME), i);
			agentDomains[agentNameIndexes.get(elementList.get(i).getAttributeValue(AGENT))]=domainNameIndexes.get(elementList.get(i).getAttributeValue(DOMAIN));
		}
		
		problem.agentDomains=agentDomains;
		return variableNameIndexes;
	}
	
	private Map<String, Integer> parseRelations(Element element, Problem problem)
	{
		if(element==null)
		{
			return null;
		}
		int nbRelations=-1;
		try {
			nbRelations=Integer.parseInt(element.getAttributeValue(NBRELATIONS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbRelations!=elementList.size())
		{
			printMessage("nbRelations!=elementList.size()");
			return null;
		}
		
		Map<String, Integer> relationNameIndexes=new HashMap<String, Integer>();
		List<int[][]> costs=new ArrayList<int[][]>();
		for(int i=0;i<nbRelations;i++)
		{
			int arity=Integer.parseInt(elementList.get(i).getAttributeValue(ARITY));
			if(arity!=2)
			{
				printMessage("arity!=2");
				return null;
			}
			relationNameIndexes.put(elementList.get(i).getAttributeValue(NAME), i);
			int[][] cost=paseConstraintCost(elementList.get(i).getValue());
			int nbTuples=Integer.parseInt(elementList.get(i).getAttributeValue(NBTUPLES));
			if(nbTuples!=cost.length*cost[0].length)
			{
				printMessage("nbValues!=cost length");
				return null;
			}
			costs.add(cost);
		}
		
		problem.costs=costs;
		return relationNameIndexes;
	}
	
	private int[][] paseConstraintCost(String costStr)
	{
		String[] parts=costStr.split("|");
		int[] costs=new int[parts.length];
		for(int i=0;i<parts.length;i++)
		{
			String[] cost_value_tuple=parts[i].split(":");
			costs[i]=Integer.parseInt(cost_value_tuple[0]);
		}
		return null;
	}
	
	private void printMessage(String msg)
	{
		System.out.println(msg);
	}
}
