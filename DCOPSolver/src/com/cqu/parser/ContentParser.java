package com.cqu.parser;

import org.jdom2.Element;


public abstract class ContentParser {
	
	protected static final String ID="id";
	protected static final String NAME="name";
	protected static final String ARITY="arity";
	
	protected static final String AGENTS="agents";
	protected static final String NBAGENTS="nbAgents";
	protected static final String AGENT="agent";
	
	protected static final String DOMAINS="domains";
	protected static final String DOMAIN="domain";
	protected static final String NBDOMAINS="nbDomains";
	protected static final String NBVALUES="nbValues";
	
	protected static final String VARIABLES="variables";
	protected static final String NBVARIABLES="nbVariables";
	
	protected static final String CONSTRAINTS="constraints";
	protected static final String NBCONSTRAINTS="nbConstraints";
	protected static final String SCOPE="scope";
	protected static final String REFERENCE="reference";
	
	protected static final String RELATIONS="relations";
	protected static final String NBRELATIONS="nbRelations";
	protected static final String NBTUPLES="nbTuples";
	
	protected static final String TYPE_DCOP="DCOP";
	protected static final String TYPE_GRAPH_COLORING="DisCSP";
	
	protected Element root;
	protected String problemType;
	
	public ContentParser(Element root, String problemType) {
		super();
		this.root = root;
		this.problemType = problemType;
	}
	
	protected abstract Problem parseContent(Problem problem);
	
	protected void printMessage(String msg)
	{
		System.out.println(msg);
	}
}
