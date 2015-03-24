package org.cqu.problem.parser;

import org.cqu.utility.XmlUtil;
import org.jdom2.Document;
import org.jdom2.Element;



public class PresentationParser {

	private String xmlPath;
	private Element root;
	private ProblemPresentation presentation;
	
	public PresentationParser(String xmlPath) {
		this.xmlPath=xmlPath;
	}
	
	public Element getRoot() {
		return root;
	}

	private void openProblem()
	{
		Document doc = XmlUtil.openXmlDocument(xmlPath);
		root = doc.getRootElement();
	}
	
	public ProblemPresentation parse()
	{
		openProblem();
		if(parsePresentation(root.getChild(NodeNames.PRESENTATION))==false)
		{
			System.out.println("parsePresentation()=false");
		}
		return presentation;
	}
	
	private boolean parsePresentation(Element element)
	{
		String format=element.getAttributeValue(NodeNames.FORMAT);
		String type=element.getAttributeValue(NodeNames.TYPE);
		String benchmark = null;
		if(format.equals(ProblemPresentation.FORMAT_XDISCSP))
		{
			benchmark=element.getAttributeValue(NodeNames.BENCHMARK);
		}
		presentation=new ProblemPresentation(format, type, benchmark);
		return true;
	}
}
