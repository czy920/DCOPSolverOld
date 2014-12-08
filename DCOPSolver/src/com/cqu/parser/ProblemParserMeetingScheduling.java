package com.cqu.parser;

import org.jdom2.Element;

import com.cqu.core.Problem;

public class ProblemParserMeetingScheduling {
	
	private Element root;
	private String treeGeneratorType;
	private String problemType;
	
	public ProblemParserMeetingScheduling(Element root, String treeGeneratorType, String problemType) {
		// TODO Auto-generated constructor stub
		this.root=root;
		this.treeGeneratorType=treeGeneratorType;
		this.problemType=problemType;
	}
	
	public Problem parse(Problem problem)
	{
		return null;
	}
}
