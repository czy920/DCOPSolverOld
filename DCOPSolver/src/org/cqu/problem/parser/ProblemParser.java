package org.cqu.problem.parser;

public class ProblemParser {
	
	private PresentationParser presentationParser;
	private ContentParser contentParser;
	
	public ProblemParser(String xmlPath) {
		// TODO Auto-generated constructor stub
		presentationParser=new PresentationParser(xmlPath);
	}
	
	public void parse(Problem problem)
	{
		ProblemPresentation presentation=presentationParser.parse();
		String format=presentation.getFormat();
		String type=presentation.getType();
		String benchmark=presentation.getBenchmark();
		if(format.equals(ProblemPresentation.FORMAT_XDISCSP))
		{
			if(type.equals(ProblemPresentation.TYPE_DCOP))
			{
				if(benchmark.equals(ProblemPresentation.BENCHMARK_RANDOMDCOP))
				{
					contentParser=new RandomDCOPParser(problem, presentationParser.getRoot(), presentation);
				}
			}else if(type.equals(ProblemPresentation.TYPE_DISCSP))
			{
				if(benchmark.equals(ProblemPresentation.BENCHMARK_GRAPHCOLORING))
				{
					contentParser=new GraphColoringParser(problem, presentationParser.getRoot(), presentation);
				}
			}
		}else if(format.equals(ProblemPresentation.FORMAT_XCSP_FRODO))
		{
			contentParser=new MeetingSchedulingParser(problem, presentationParser.getRoot(), presentation);
		}
		if(contentParser!=null)
		{
			contentParser.parseContent();
		}
	}
}
