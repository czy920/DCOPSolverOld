package org.cqu.problem.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqu.utility.CollectionUtil;
import org.jdom2.Element;


public abstract class XDisCSPParser extends ContentParser{

	public XDisCSPParser(Problem problem, Element root,
			ProblemPresentation presentation) {
		super(problem, root, presentation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parseContent() {
		// TODO Auto-generated method stub
		Map<String, Integer> nodeNameIds=parseNodes(root.getChild(NodeNames.AGENTS), problem);
		if(nodeNameIds==null)
		{
			System.out.println("parseNodes() fails!");
			return;
		}
		
		if(parseDomains(root.getChild(NodeNames.DOMAINS), problem)==false)
		{
			System.out.println("parseDomains() fails!");
			return;
		}
		
		Map<String, Integer> variableNameNodeIds=parseVariables(root.getChild(NodeNames.VARIABLES), problem, nodeNameIds);
		if(variableNameNodeIds==null)
		{
			System.out.println("parseVariables() fails!");
			return;
		}
		
		if(parseRelations(root.getChild(NodeNames.RELATIONS), problem)==false)
		{
			System.out.println("parseRelations() fails!");
			return;
		}
		
		if(parseConstraints(root.getChild(NodeNames.CONSTRAINTS), problem, variableNameNodeIds)==false)
		{
			System.out.println("parseConstraints() fails!");
			return;
		}
	}
	
	protected Map<String, Integer> parseNodes(Element element, Problem problem)
	{
		if(element==null)
		{
			return null;
		}
		int nbNodes=-1;
		try {
			nbNodes=Integer.parseInt(element.getAttributeValue(NodeNames.NBAGENTS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbNodes!=elementList.size())
		{
			System.out.println("nbNodes!=elementList.size()");
			return null;
		}
		Map<String, Integer> nodeNameIds=new HashMap<String, Integer>();
		try{
			for(int i=0;i<nbNodes;i++)
			{
				int id=Integer.parseInt(elementList.get(i).getAttributeValue(NodeNames.ID));
				String name=elementList.get(i).getAttributeValue(NodeNames.NAME);
				
				nodeNameIds.put(name, id);
				problem.putNodeName(id, name);
			}
		}catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return nodeNameIds;
	}
	
	protected boolean parseDomains(Element element, Problem problem)
	{
		if(element==null)
		{
			return false;
		}
		int nbDomains=-1;
		try {
			nbDomains=Integer.parseInt(element.getAttributeValue(NodeNames.NBDOMAINS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbDomains!=elementList.size())
		{
			System.out.println("nbDomains!=elementList.size()");
			return false;
		}
		
		for(int i=0;i<nbDomains;i++)
		{
			int[] domain=parseFromTo(elementList.get(i).getValue());
			int nbValues=Integer.parseInt(elementList.get(i).getAttributeValue(NodeNames.NBVALUES));
			if(nbValues!=domain.length)
			{
				System.out.println("nbValues!=domain.length");
				return false;
			}
			problem.putDomain(elementList.get(i).getAttributeValue(NodeNames.NAME), domain);
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
	
	protected Map<String, Integer> parseVariables(Element element, Problem problem, Map<String, Integer> nodeNameIds)
	{
		if(element==null)
		{
			return null;
		}
		int nbVariables=-1;
		try {
			nbVariables=Integer.parseInt(element.getAttributeValue(NodeNames.NBVARIABLES));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbVariables!=elementList.size())
		{
			System.out.println("nbVariables!=elementList.size()");
			return null;
		}
		if(nbVariables!=problem.getNodeIds().size())
		{
			System.out.println("nbVariables!=problem.nodeCount，要求每个node中只包含一个variable");
			return null;
		}
		Map<String, Integer> variableNameNodeIds=new HashMap<String, Integer>();
		for(int i=0;i<nbVariables;i++)
		{
			int nodeId=nodeNameIds.get(elementList.get(i).getAttributeValue(NodeNames.AGENT));
			variableNameNodeIds.put(elementList.get(i).getAttributeValue(NodeNames.NAME), nodeId);
			problem.putNodeDomain(nodeId, elementList.get(i).getAttributeValue(NodeNames.DOMAIN));
		}
		return variableNameNodeIds;
	}
	
	protected abstract boolean parseRelations(Element element, Problem problem);
	
	protected boolean parseConstraints(Element element, Problem problem, Map<String, Integer> variableNameNodeIds)
	{
		if(element==null)
		{
			return false;
		}
		int nbConstraints=-1;
		try {
			nbConstraints=Integer.parseInt(element.getAttributeValue(NodeNames.NBCONSTRAINTS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbConstraints!=elementList.size())
		{
			System.out.println("nbConstraints!=elementList.size()");
			return false;
		}
		
		Map<Integer, List<Integer>> neighbourNodes=new HashMap<Integer, List<Integer>>();
		Map<Integer, Map<Integer, String>> neighbourConstraints=new HashMap<Integer, Map<Integer, String>>();
		for(Integer nodeId : problem.getNodeIds())
		{
			neighbourNodes.put(nodeId, new ArrayList<Integer>());
			neighbourConstraints.put(nodeId, new HashMap<Integer, String>());
		}
		
		for(int i=0;i<nbConstraints;i++)
		{
			int arity=Integer.parseInt(elementList.get(i).getAttributeValue(NodeNames.ARITY));
			if(arity!=2)
			{
				System.out.println("arity!=2");
				return false;
			}
			String[] constraintedParts=elementList.get(i).getAttributeValue(NodeNames.SCOPE).split(" ");
			int leftNodeId=variableNameNodeIds.get(constraintedParts[0]);
			int rightNodeId=variableNameNodeIds.get(constraintedParts[1]);
			neighbourNodes.get(leftNodeId).add(rightNodeId);
			neighbourNodes.get(rightNodeId).add(leftNodeId);
			neighbourConstraints.get(leftNodeId).put(rightNodeId, elementList.get(i).getAttributeValue(NodeNames.REFERENCE));
			neighbourConstraints.get(rightNodeId).put(leftNodeId, elementList.get(i).getAttributeValue(NodeNames.REFERENCE));
		}
		
		for(Integer nodeId : problem.getNodeIds())
		{
			List<Integer> temp=neighbourNodes.get(nodeId);
			Integer[] buff=new Integer[temp.size()];
			problem.putNodeNeighbours(nodeId, CollectionUtil.toInt(temp.toArray(buff)));
			
			String[] costNames=new String[temp.size()];
			for(int i=0;i<buff.length;i++)
			{
				costNames[i]=neighbourConstraints.get(nodeId).get(buff[i]);
			}
			problem.putNodeConstraints(nodeId, costNames);
			
		}
		return true;
	}
}
