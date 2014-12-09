package com.cqu.parser;

import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.bfsdpop.CrossEdgeAllocator;
import com.cqu.core.BFSTree;
import com.cqu.core.DFSTree;
import com.cqu.core.TreeGenerator;
import com.cqu.util.XmlUtil;

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
	
	public ProblemParser(String xmlPath, String treeGeneratorType) {
		// TODO Auto-generated constructor stub
		this.xmlPath=xmlPath;
		this.treeGeneratorType=treeGeneratorType;
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
			this.generateCommunicationStructure(problem);
			return problem;
		}else
		{
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateCommunicationStructure(Problem problem)
	{
		TreeGenerator treeGenerator;
		if(treeGeneratorType.equals(TreeGenerator.TREE_GENERATOR_TYPE_DFS))
		{
			treeGenerator=new DFSTree(problem.neighbourAgents);
		}else
		{
			treeGenerator=new BFSTree(problem.neighbourAgents);
		}
		treeGenerator.generate();
		
		problem.agentLevels=treeGenerator.getNodeLevels();
		for(Integer level:problem.agentLevels.values())
			if(problem.treeDepth<(level+1))problem.treeDepth=level+1;
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
}
