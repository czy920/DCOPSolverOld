package com.cqu.parser;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.core.Problem;
import com.cqu.util.XmlUtil;

public class DCOPParser {
	
	public static final String PRESENTATION="presentation";
	public static final String FORMAT="format";
	public static final String TYPE="type";
	
	public static final String FORMAT_DISCHOCO="XDisCSP 1.0";
	public static final String FORMAT_FRODO="XCSP 2.1_FRODO";
	
	public static final String TYPE_DCOP="DCOP";
	public static final String TYPE_GRAPH_COLORING="DisCSP";
	
	private String xmlPath;
	private String problemFormat;
	private String problemType;
	
	public DCOPParser(String path) {
		// TODO Auto-generated constructor stub
		this.xmlPath=path;
	}

	public Problem parse(String treeGeneratorType)
	{
		Problem problem = new Problem();
		
		Document doc=XmlUtil.openXmlDocument(this.xmlPath);
		
		String docURL=doc.getBaseURI();
		String[] docName=docURL.split("/");
		System.out.println(docName[docName.length-1]);
		
		Element root=doc.getRootElement();
		
		if(parsePresentation(root.getChild(PRESENTATION))==false)
		{
			this.printMessage("parsePresentation()=false");
			return null;
		}
		
		if(problemFormat.equals(FORMAT_DISCHOCO))
		{
			ProblemParserGeneral parser=new ProblemParserGeneral(root, treeGeneratorType, problemType);
			return parser.parse(problem);
		}else if(problemFormat.equals(FORMAT_FRODO))
		{
			ProblemParserMeetingScheduling parser=new ProblemParserMeetingScheduling(root, treeGeneratorType, docURL);
			return parser.parse(problem);
		}else
		{
			return null;
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
	
	private void printMessage(String msg)
	{
		System.out.println(msg);
	}
}
