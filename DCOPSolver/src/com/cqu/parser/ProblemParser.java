package com.cqu.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.bfsdpop.CrossEdgeAllocator;
import com.cqu.core.BFSTree;
import com.cqu.core.TreeGenerator;
import com.cqu.heuristics.MostConnectedHeuristic;
import com.cqu.heuristics.MostConstributionHeuristic;
import com.cqu.main.DOTrenderer;
import com.cqu.util.XmlUtil;
import com.cqu.varOrdering.dfs.DFSgeneration;

public class ProblemParser {
	
	private static final String PRESENTATION="presentation";
	private static final String FORMAT="format";
	private static final String TYPE="type";
	
	private static final String FORMAT_DISCHOCO="XDisCSP 1.0";
	private static final String FORMAT_FRODO="XCSP 2.1_FRODO";
	
	private static final String TYPE_DCOP="DCOP";
	private static final String TYPE_GRAPH_COLORING="DisCSP";
	
	private String xmlPath;
	private String problemFormat;
	private String problemType;
	private String treeGeneratorType;
	protected Element root;
	
	public ProblemParser(String xmlPath, String treeGeneratorType) {
		// TODO Auto-generated constructor stub
		this.xmlPath=xmlPath;
		this.treeGeneratorType=treeGeneratorType;
	}
	public ProblemParser(String xmlPath){
		this.xmlPath = xmlPath;
		Document doc = XmlUtil.openXmlDocument(xmlPath);
		this.root = doc.getRootElement();
	}

	public Problem parse()
	{
		Problem problem = new Problem();
		
		Document doc=XmlUtil.openXmlDocument(xmlPath);
		
		String docURL=doc.getBaseURI();
		String[] docName=docURL.split("/");
		System.out.println(docName[docName.length-1]);
		
		Element root=doc.getRootElement();
		if(parsePresentation(root.getChild(PRESENTATION))==false)
		{
			System.out.println("parsePresentation()=false");
			return null;
		}
		
		ContentParser parser=null;
		if(problemFormat.equals(FORMAT_DISCHOCO))
		{
			parser=new ParserGeneral(root, problemType);
		}else if(problemFormat.equals(FORMAT_FRODO))
		{
			parser=new ParserMeetingScheduling(root, problemType);
		}else
		{
			parser=null;
		}
		
		if(parser!=null)
		{
			parser.parseContent(problem);
			this.generateAgentProperty(problem);
			this.generateCommunicationStructure(problem);
			return problem;
		}else
		{
			return null;
		}
	}
	
	private void generateAgentProperty(Problem problem)
	{
		for (Map.Entry<Integer, String[]> entry : problem.agentConstraintCosts.entrySet())
		{
			int sumCost = 0;
			for (int i = 0; i < entry.getValue().length; i++)
			{
				sumCost += problem.relationCost.get(entry.getValue()[i]);
			}
			problem.agentProperty.put(entry.getKey(), sumCost);
		}
		/*
		 * 需要对其进行排序
		 */
		/*for (Map.Entry<Integer, Integer> entry : problem.agentProperty.entrySet())
		{
			System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
		}*/
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateCommunicationStructure(Problem problem)
	{
		TreeGenerator treeGenerator;
		if(treeGeneratorType.equals(TreeGenerator.TREE_GENERATOR_TYPE_DFS))
		{
			//treeGenerator=new DFSTree(problem.neighbourAgents);
			treeGenerator = new DFSgeneration(problem.neighbourAgents);
		}else
		{
			treeGenerator=new BFSTree(problem.neighbourAgents);
		}
		DFSgeneration.setRootHeuristics(new MostConstributionHeuristic(problem));
		DFSgeneration.setNextNodeHeuristics(new MostConstributionHeuristic(problem));
		treeGenerator.generate();
		
		problem.agentLevels=treeGenerator.getNodeLevels();
		for(Integer level:problem.agentLevels.values())
			if(problem.treeDepth<(level+1))problem.treeDepth=level+1;
		problem.pseudoHeight=treeGenerator.getPseduHeight();
		problem.parentAgents=treeGenerator.getParentNode();
		problem.childAgents=treeGenerator.getChildrenNodes();
		Map[] allParentsAndChildren=treeGenerator.getAllChildrenAndParentNodes();
		problem.allParentAgents=allParentsAndChildren[0];
		problem.allChildrenAgents=allParentsAndChildren[1];
		
		if(treeGeneratorType.equals(TreeGenerator.TREE_GENERATOR_TYPE_BFS))
		{
			CrossEdgeAllocator allocator=new CrossEdgeAllocator(problem);
			allocator.allocate();
			problem.crossConstraintAllocation=allocator.getConsideredConstraint();
		}
	}
	
	private boolean parsePresentation(Element element)
	{
		if(element==null)
		{
			return false;
		}
		problemFormat=element.getAttributeValue(FORMAT);
		if(problemFormat.equals(FORMAT_DISCHOCO))
		{
			problemType=element.getAttributeValue(TYPE);
			if(problemType.equals(TYPE_DCOP)==true||problemType.equals(TYPE_GRAPH_COLORING)==true)
			{
				return true;
			}else
			{
				return false;
			}
		}else if(problemFormat.equals(FORMAT_FRODO))
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public static void main (String[] args) throws Exception{
		new DOTrenderer ("Constraint graph", ProblemParser.toDOT("ins.xml"));
		//System.out.println(ProblemParser.toDOT("random_Max-DisCSP.xml"));
	}
	//display constraint graph
	public static String toDOT (String path) throws Exception{
		StringBuilder out = new StringBuilder ("graph {\n\tnode [shape = \"circle\"];\n");
		
		
		ProblemParser parser = new ProblemParser (path);
		for (String agent : parser.getAgents())
		{
			out.append("\tsubgraph cluster_" + agent + " {\n");
			
			out.append("\t\tlabel = " + agent + ";\n");
			
			for (String var : parser.getVariables(agent)){
				out.append("\t\t" + var);
				
				out.append(";\n");
			}
			out.append("\t}\n");
		}
		out.append("\n");
		for (String var : parser.getVariables())
		{
			for (String neighbor : parser.getNeighborVars(var))
			{
				if (var.compareTo(neighbor) >= 0)
				{
					out.append("\t" + var + "--" + neighbor + ";\n");
				}
			}
		}
		out.append("}\n");
		return out.toString();
	}
	public Set<String> getAgents(){
		Set<String> agents = new HashSet<String>();
		for(Element var : this.root.getChild("agents").getChildren())
		{
			agents.add(var.getAttributeValue("name"));
		}
			
		return agents;
	}
	public String getOwner (String var)
	{
		for(Element varElmt :this.root.getChild("variables").getChildren())
			if(varElmt.getAttributeValue("name").equals(var))
				return varElmt.getAttributeValue("agent");
		assert false : "Unknown variable '" + var + "'" ;
		return null;
	}
	public Set<String> getVariables()
	{
		Set<String> out = new HashSet<String> ();
		for(Element varElmt : this.root.getChild("variables").getChildren())
			if(! "random".equals(varElmt.getAttributeValue("type")))
				out.add(varElmt.getAttributeValue("name"));
		
		return out;
	}
	public Set<String> getVariables(String owner)
	{
		Set<String> out = new HashSet<String>();
		if(owner != null)
		{
			for (Element var : this.root.getChild("variables").getChildren())
				if(owner.equals(var.getAttributeValue("agent")))
					out.add(var.getAttributeValue("name"));
		}else
			for (Element var : this.root.getChild("variables").getChildren())
				if(var.getAttributeValue("agent") == null)
					out.add(var.getAttributeValue("name"));
		return out;
	}
	public HashSet<String> getNeighborVars(String var)
	{
		HashSet<String> out = new HashSet<String>();
		LinkedList<String> pending = new LinkedList<String>();
		pending.add(var);
		HashSet<String> done = new HashSet<String> ();
		do{
			String var2 = pending.poll();
			if (!done.add(var2))
				continue;
			for (Element constraint : this.root.getChild("constraints").getChildren()){
				String[] scope = constraint.getAttributeValue("scope").trim().split("\\s+");
				Arrays.sort(scope);
				if (Arrays.binarySearch(scope, var2) >=0 ){
					for (String neighbor : scope)
					{
						if (! this.isRandom(neighbor))
							out.add(neighbor);
					}
				}
			}
		}while(!pending.isEmpty());
		out.remove(var);
		return out;
	}
	public boolean isRandom (String var) {
		for (Element varElmt : this.root.getChild("variables").getChildren())
			if (var.equals(varElmt.getAttributeValue("name")))
				return new String ("random").equals(varElmt.getAttributeValue("type"));
		
		return false;
	}
}
