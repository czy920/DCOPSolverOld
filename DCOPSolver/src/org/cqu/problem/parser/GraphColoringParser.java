package org.cqu.problem.parser;

import java.util.List;

import org.cqu.utility.CollectionUtil;
import org.jdom2.Element;


public class GraphColoringParser extends XDisCSPParser{

	public GraphColoringParser(Problem problem, Element root,
			ProblemPresentation presentation) {
		super(problem, root, presentation);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean parseRelations(Element element, Problem problem) {
		// TODO Auto-generated method stub
		if(element==null)
		{
			return false;
		}
		int nbRelations=-1;
		try {
			nbRelations=Integer.parseInt(element.getAttributeValue(NodeNames.NBRELATIONS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbRelations!=elementList.size())
		{
			System.out.println("nbRelations!=elementList.size()");
			return false;
		}
		
		for(int i=0;i<nbRelations;i++)
		{
			int arity=Integer.parseInt(elementList.get(i).getAttributeValue(NodeNames.ARITY));
			if(arity!=2)
			{
				System.out.println("arity!=2");
				return false;
			}
			int[] constraintValues=null;
			constraintValues=parseConstraintValues(problem.getNodeDomain(problem.getNodeIds().get(0)), elementList.get(i).getValue());
			int nbTuples=Integer.parseInt(elementList.get(i).getAttributeValue(NodeNames.NBTUPLES));
			if(nbTuples!=constraintValues.length)
			{
				System.out.println("nbValues!=cost length");
				return false;
			}
			problem.putConstraintValues(elementList.get(i).getAttributeValue(NodeNames.NAME), constraintValues);
		}
		return true;
	}
	
	private int[] parseConstraintValues(int[] domain, String costStr)
	{
		String[] items=costStr.split("\\|");
		int[] costs=new int[domain.length*domain.length];
		for(int i=0;i<domain.length;i++)
		{
			for(int j=0;j<domain.length;j++)
			{
				if(CollectionUtil.indexOf(items, domain[i]+" "+domain[j])!=-1)
				{
					costs[i*domain.length+j]=1;
				}else
				{
					costs[i*domain.length+j]=0;
				}
			}
		}
		return costs;
	}

}
