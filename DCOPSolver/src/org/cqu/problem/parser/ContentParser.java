package org.cqu.problem.parser;

import org.jdom2.Element;

public abstract class ContentParser {
	
	protected Problem problem;
	protected Element root;
	protected ProblemPresentation presentation;
	
	public ContentParser(Problem problem, Element root, ProblemPresentation presentation) {
		// TODO Auto-generated constructor stub
		this.problem=problem;
		this.root=root;
		this.presentation=presentation;
	}
	
	public abstract void parseContent();
}
