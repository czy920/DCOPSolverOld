package com.cqu.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

public class ParserMeetingScheduling extends ContentParser{

	public ParserMeetingScheduling(Element root, String problemType) {
		super(root, problemType);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Problem parseContent(Problem problem) {
		// TODO Auto-generated method stub
		
		if(parseDomains(root.getChild(DOMAINS), problem)==false)
		{
			this.printMessage("parseDomains() fails!");
			return null;
		}
		
		List<Variable> variables=parseVariables(root.getChild(VARIABLES));
		if(variables==null)
		{
			this.printMessage("parseVariables() fails!");
			return null;
		}
		
		Map<Integer, List<String>> attendees=new HashMap<Integer, List<String>>();
		for(Variable var : variables)
		{
			String name=var.getName().substring(0, var.getName().indexOf(var.getAgentName()));
			Integer id=Integer.parseInt(name.substring(1));
			problem.agentNames.put(id, name);
			problem.agentDomains.put(id, var.getDomainName());
			if(attendees.containsKey(id)==false)
			{
				attendees.put(id, new ArrayList<String>());
			}
			attendees.get(id).add(var.getAgentName());
		}
		
		Integer[] agentIds=new Integer[attendees.size()];
		attendees.keySet().toArray(agentIds);
		int constraintCount=0;
		int domainSize=0;
		for(int[] domain : problem.domains.values())
		{
			domainSize=domain.length;
			break;
		}
		Map<Integer, List<String>> agentConstraints=new HashMap<Integer, List<String>>();
		Map<Integer, List<Integer>> agentNeighbors=new HashMap<Integer, List<Integer>>();
		for(int i=0;i<agentIds.length;i++)
		{
			for(int j=i+1;j<agentIds.length;j++)
			{
				int sameAttendeeCount=sameCount(attendees.get(agentIds[i]), attendees.get(agentIds[j]));
				if(sameAttendeeCount>0)
				{
					String name="R"+constraintCount;
					int[] costs=calConstraints(sameAttendeeCount, domainSize);
					
					int min=costs[0];
					for(int k=1;k<costs.length;k++){
						if(min>costs[k])min=costs[k];
					}
					problem.relationCost.put(name, min);
					
					problem.costs.put(name, costs);
					if(agentConstraints.containsKey(agentIds[i])==false)
					{
						agentConstraints.put(agentIds[i], new ArrayList<String>());
						agentNeighbors.put(agentIds[i], new ArrayList<Integer>());
					}
					if(agentConstraints.containsKey(agentIds[j])==false)
					{
						agentConstraints.put(agentIds[j], new ArrayList<String>());
						agentNeighbors.put(agentIds[j], new ArrayList<Integer>());
					}
					agentConstraints.get(agentIds[i]).add(name);
					agentConstraints.get(agentIds[j]).add(name);
					agentNeighbors.get(agentIds[i]).add(agentIds[j]);
					agentNeighbors.get(agentIds[j]).add(agentIds[i]);
					constraintCount++;
				}
			}
		}
		
		for(Integer agentId : agentConstraints.keySet())
		{
			List<String> constraintList=agentConstraints.get(agentId);
			List<Integer> neighborList=agentNeighbors.get(agentId);
			String[] constraints=new String[constraintList.size()];
			int[] neighbors=new int[neighborList.size()];
			for(int i=0;i<constraintList.size();i++)
			{
				constraints[i]=constraintList.get(i);
				neighbors[i]=neighborList.get(i);
			}
			problem.agentConstraintCosts.put(agentId, constraints);
			problem.neighbourAgents.put(agentId, neighbors);
		}
		
		return problem;
	}
	
	private int[] calConstraints(int sameAttendeeCount, int domainSize)
	{
		int[] costs=new int[domainSize*domainSize];
		for(int i=0;i<domainSize;i++)
		{
			for(int j=0;j<domainSize;j++)
			{
				if(i==j)
				{
					costs[i*domainSize+j]=100*sameAttendeeCount;
				}else
				{
					costs[i*domainSize+j]=0;
				}
			}
		}
		return costs;
	}
	
	private int sameCount(List<String> attendeesA, List<String> attendeesB)
	{
		List<String> sameAttendees=new ArrayList<String>();
		for(int i=0;i<attendeesA.size();i++)
		{
			String attendee=attendeesA.get(i);
			for(int j=0;j<attendeesB.size();j++)
			{
				if(attendeesB.get(j).equals(attendee)==true)
				{
					sameAttendees.add(attendee);
					break;
				}
			}
		}
		return sameAttendees.size();
	}
	
	private boolean parseDomains(Element element, Problem problem)
	{
		if(element==null)
		{
			return false;
		}
		int nbDomains=-1;
		try {
			nbDomains=Integer.parseInt(element.getAttributeValue(NBDOMAINS));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbDomains!=elementList.size())
		{
			printMessage("nbDomains!=elementList.size()");
			return false;
		}
		
		for(int i=0;i<nbDomains;i++)
		{
			int[] domain=parseFromTo(elementList.get(i).getValue());
			int nbValues=Integer.parseInt(elementList.get(i).getAttributeValue(NBVALUES));
			if(nbValues!=domain.length)
			{
				printMessage("nbValues!=domain.length");
				return false;
			}
			problem.domains.put(elementList.get(i).getAttributeValue(NAME), domain);
		}
		
		return true;
	}
	
	private int[] parseFromTo(String fromToStr)
	{
		int from=-1;
		int to=-1;
		String separator="..";
		
		fromToStr=fromToStr.trim();
		int sepIndex=fromToStr.indexOf(separator);
		from=Integer.parseInt(fromToStr.substring(0, sepIndex));
		to=Integer.parseInt(fromToStr.substring(sepIndex+separator.length()));
		
		int[] ret=new int[to-from+1];
		for(int i=0;i<ret.length;i++)
		{
			ret[i]=i+from;
		}
		
		return ret;
	}
	
	private List<Variable> parseVariables(Element element)
	{
		if(element==null)
		{
			return null;
		}
		int nbVariables=-1;
		try {
			nbVariables=Integer.parseInt(element.getAttributeValue(NBVARIABLES));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		if(nbVariables!=elementList.size())
		{
			printMessage("nbVariables!=elementList.size()");
			return null;
		}
		List<Variable> variables=new ArrayList<Variable>();
		for(int i=0;i<nbVariables;i++)
		{
			String name=elementList.get(i).getAttributeValue(NAME);
			String domainName=elementList.get(i).getAttributeValue(DOMAIN);
			String agentName=elementList.get(i).getAttributeValue(AGENT);
			variables.add(new Variable(name, domainName, agentName));
		}
		return variables;
	}
	
	final class Variable
	{
		private String name;
		private String domainName;
		private String agentName;
		
		public Variable(String name, String domainName, String agentName) {
			super();
			this.name = name;
			this.domainName = domainName;
			this.agentName = agentName;
		}
		
		public String getName() {
			return name;
		}
		
		public String getDomainName() {
			return domainName;
		}
		
		public String getAgentName() {
			return agentName;
		}
	}

}
