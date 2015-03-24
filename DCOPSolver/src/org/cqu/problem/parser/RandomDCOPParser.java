package org.cqu.problem.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

public class RandomDCOPParser extends XDisCSPParser{

	public RandomDCOPParser(Problem problem, Element root,
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
			constraintValues=parseConstraintValues(elementList.get(i).getValue(), problem, elementList.get(i).getAttributeValue(NodeNames.NAME));
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
	
	private int[] parseConstraintValues(String costStr, Problem problem, String relation)
	{
		String[] items=costStr.split("\\|");
		String[] costParts=new String[items.length];
		Map<String, Integer> valuePairParts=new HashMap<String, Integer>();
		int index=0;
		for(int i=0;i<items.length;i++)
		{
			index=items[i].indexOf(':');
			costParts[i]=items[i].substring(0, index);
			valuePairParts.put(items[i].substring(index+1), i);
		}
		Object[] valuePairPartsKeyArray=valuePairParts.keySet().toArray();
		Arrays.sort(valuePairPartsKeyArray);
		int[] costs=new int[items.length];
		for(int i=0;i<items.length;i++)
		{
			costs[i]=Integer.parseInt(costParts[valuePairParts.get(valuePairPartsKeyArray[i])]);	
		}
		return costs;
	}
}
