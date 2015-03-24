package org.cqu.problem.parser;

public final class ProblemPresentation {
	
	public static final String FORMAT_XDISCSP="XDisCSP 1.0";
	public static final String FORMAT_XCSP_FRODO="XCSP 2.1_FRODO";
	
	public static final String TYPE_DCOP="DCOP";
	public static final String TYPE_DISCSP="DisCSP";
	
	public static final String BENCHMARK_RANDOMDCOP="RandomDCOP";
	public static final String BENCHMARK_GRAPHCOLORING="Graph Coloring";
	
	private String format;
	private String type;
	private String benchmark;
	
	public ProblemPresentation(String format, String type, String benchmark) {
		super();
		this.format = format;
		this.type = type;
		this.benchmark = benchmark;
	}

	public String getFormat() {
		return format;
	}
	
	public String getType() {
		return type;
	}
	
	public String getBenchmark() {
		return benchmark;
	}
}
