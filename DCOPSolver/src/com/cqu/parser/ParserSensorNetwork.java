package com.cqu.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.jdom2.Element;

import util.HashList;

public class ParserSensorNetwork extends ContentParser{

	public ParserSensorNetwork(Element root, String problemType) {
		super(root, problemType);
		// TODO Auto-generated constructor stub
	}
	
	protected Problem parseContent(Problem problem){
		
		if(parseDomains(root.getChild(DOMAINS), problem)==false){
			this.printMessage("parseDomains() fails!");
			return null;
		}
		
		List<Variable> variables = parseVariables(root.getChild(VARIABLES));
		
		if(variables == null){
			this.printMessage("parseVariables() fails!");
			return null;
		}
		
		Map<Integer, Map<Integer, List<Integer>>> relations = parseRelations(root.getChild(RELATIONS));//compatibility，<id,<id,List<id>>>
		
		Map<Integer, List<Integer>> detect = new HashMap<Integer, List<Integer>>();//Agent's id and the it's domain
		
		for(Variable var : variables){	
			String name = var.getName();//S1.0;S1.1;S1.2;S2.0;S2.1;S2.2;S3.0;S3.1;S3.2
			Integer id = Integer.parseInt(name.substring(1,name.indexOf(".")))*10+Integer.parseInt(name.substring(name.indexOf(".")+1));//10;11;12;20;21;22;30;31;32
			String domainName = var.getDomainName();
			problem.agentNames.put(id, name);
			problem.agentDomains.put(id, var.getDomainName());
			
			if(detect.containsKey(id) == false){
				detect.put(id, new ArrayList<Integer>());
			}
			
			int[] domain = problem.domains.get(domainName);
			
			for(int i= 0; i<domain.length; i++){
				detect.get(id).add(domain[i]);
			}
		}

		Map<Integer, List<String>> agentConstraints = new HashMap<Integer, List<String>>();
		Map<Integer, List<Integer>> agentNeighbors = new HashMap<Integer, List<Integer>>();
		Integer[] agentIds = new Integer[detect.size()];//save the agent's ID
		detect.keySet().toArray(agentIds);
		Arrays.sort(agentIds);
        int constraintCount = 0;
        
        for(int i=0; i<agentIds.length; i++){        //changed
        	for(int j=i+1; j<agentIds.length; j++){
        		if(nameToagentName(problem.agentNames.get(agentIds[i]),variables).equals(nameToagentName(problem.agentNames.get(agentIds[j]),variables))){
        			//if two agent's original agent is common, they must have constraints; else, if two agent's domains have same parts, they have constraints
        			String name = "R" + constraintCount;
        			int[] costs = intraCosts(detect.get(agentIds[i]), detect.get(agentIds[j]), relations.get(Integer.parseInt(nameToagentName(problem.agentNames.get(agentIds[i]),variables).substring(1))-1));   //maybe have mistake
	
        			int min = costs[0];
					for(int k=1; k<costs.length; k++){
						if(min > costs[k])
							min = costs[k];
					}
					
					problem.relationCost.put(name,min);
					problem.costs.put(name,costs);//约束代价不是n*n的会不会影响结果？
					
					if(agentConstraints.containsKey(agentIds[i]) == false){
						agentConstraints.put(agentIds[i], new ArrayList<String>());
						agentNeighbors.put(agentIds[i], new ArrayList<Integer>());
					}
					
					if(agentConstraints.containsKey(agentIds[j]) == false){
						agentConstraints.put(agentIds[j], new ArrayList<String>());
						agentNeighbors.put(agentIds[j], new ArrayList<Integer>());
					}
					
					agentConstraints.get(agentIds[i]).add(name);
					agentConstraints.get(agentIds[j]).add(name);
					agentNeighbors.get(agentIds[i]).add(agentIds[j]);
					agentNeighbors.get(agentIds[j]).add(agentIds[i]);
					constraintCount++;    			

        		}else if(sameNum(detect.get(agentIds[i]),detect.get(agentIds[j])) > 0){
        			String name = "R" + constraintCount;
        			int[] costs = interCosts(detect.get(agentIds[i]),detect.get(agentIds[j]));
        			
        			int min = costs[0];
					for(int k=1; k<costs.length; k++){
						if(min>costs[k])
							min = costs[k];
					}
					
					problem.relationCost.put(name, min);
					problem.costs.put(name,costs);
					
					if(agentConstraints.containsKey(agentIds[i])==false){
						agentConstraints.put(agentIds[i], new ArrayList<String>());
						agentNeighbors.put(agentIds[i], new ArrayList<Integer>());
					}
					
					if(agentConstraints.containsKey(agentIds[j])==false){
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
		
        for(Integer agentId : agentConstraints.keySet()){
			List<String> constraintList = agentConstraints.get(agentId);
			List<Integer> neighborList = agentNeighbors.get(agentId);
			String[] constraints = new String[constraintList.size()];
			int[] neighbors = new int[neighborList.size()];
			for(int i=0; i<constraintList.size(); i++){
				constraints[i] = constraintList.get(i);
				neighbors[i] = neighborList.get(i);
			}
			
			problem.agentConstraintCosts.put(agentId, constraints);
			problem.neighbourAgents.put(agentId, neighbors);
		}
        
        return problem;
	}
	
	private int[] intraCosts(List<Integer> f, List<Integer> s, Map<Integer, List<Integer>> relation){
		int[] costs = new int[f.size()*s.size()];
		
		for(int i=0; i<f.size(); i++){//is compatibility?
			for(int j=0; j<s.size(); j++){
				if(relation.containsKey(f.get(i)) == false){
					costs[i*s.size()+j] = 1000;
				}else if(relation.get(f.get(i)).contains(s.get(j)) == false){
					costs[i*s.size()+j] = 1000;
				}else{
					costs[i*s.size()+j] = 0;
				}
			}
		}
		
		for(int i=0; i<f.size(); i++){
			for(int j=0; j<s.size(); j++){
				if(i == j){
					costs[i*s.size()+j] = 1000;
				}else{
					costs[i*s.size()+j] = 0;
				}
				
			}
		}
	    return costs;
	    
	}
	
	private int[] interCosts(List<Integer> f,List<Integer> s){
		int[] costs = new int[f.size()*s.size()];
		for(int i=0; i<f.size(); i++){
			for(int j=0; j<s.size(); j++){
				if(f.get(i) == s.get(j)){
					costs[i*s.size()+j] = 1000;
				}else{
					costs[i*s.size()+j] = 0;
				}
			}
		}
		return costs;
	}
	
	private int sameNum(List<Integer> f, List<Integer> s){
		int count = 0;
		for(int i=0; i<f.size(); i++){
			if(s.contains(f.get(i)))
				count++;
		}
		return count;
	}
	
	private String nameToagentName(String name, List<Variable> variable){
		for(int i=0; i<variable.size(); i++){
			if(variable.get(i).getName().equals(name)){//error
				return variable.get(i).getAgentName();
			}
		}
		return null;
	}
	
	private Map<Integer, Map<Integer, List<Integer>>> parseRelations(Element element){
		List<Element>elementList = element.getChildren();
		Map<Integer, Map<Integer, List<Integer>>> compatibility = new HashMap<Integer, Map<Integer, List<Integer>>>();
		for(int i=0; i<elementList.size(); i++) {
			
			String text = elementList.get(i).getText();
			List<String> list = new HashList<String>(); 
			String str = text;
			
			while(str.indexOf("|")>0){
				list.add(str.substring(0,str.indexOf("|")));
				str = str.substring(str.indexOf("|")+1);
			}
			list.add(str.substring(str.indexOf("|")+1));

			Integer id = Integer.parseInt(elementList.get(i).getAttributeValue(NAME).substring(1));
			if(compatibility.containsKey(id) == false){
				compatibility.put(id, new HashMap<Integer, List<Integer>>());
			}
			
			for(int j=0; j<list.size(); j++){//whether to add judge if exists?
				String listStr = list.get(j);
				if(compatibility.get(id).containsKey(Integer.parseInt(listStr.substring(0,listStr.indexOf(" ")))) == false){
					compatibility.get(id).put(Integer.parseInt(listStr.substring(0,listStr.indexOf(" "))), new ArrayList<Integer>());
				}
				if(compatibility.get(id).containsKey(Integer.parseInt(listStr.substring(listStr.indexOf(" ")+1))) == false){
					compatibility.get(id).put(Integer.parseInt(listStr.substring(listStr.indexOf(" ")+1)), new ArrayList<Integer>());
				}
				compatibility.get(id).get(Integer.parseInt(listStr.substring(0,listStr.indexOf(" ")))).add(Integer.parseInt(listStr.substring(listStr.indexOf(" ")+1)));
				compatibility.get(id).get(Integer.parseInt(listStr.substring(listStr.indexOf(" ")+1))).add(Integer.parseInt(listStr.substring(0,listStr.indexOf(" "))));
			}
		}
		return compatibility;
	}
	
	private boolean parseDomains(Element element, Problem problem){
		
		if(element==null){
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
		
		if(nbDomains!=elementList.size()){
			printMessage("nbDomains!=elementList.size()");
			return false;
		}
		
		for(int i = 0; i<nbDomains; i++){	
			String[] domain = elementList.get(i).getText().split(" ");
			int[] Domain = new int[domain.length];
			for(int j=0; j<domain.length; j++){
				Domain[j] = Integer.parseInt(domain[j]);
			}
			
			int nbValues = Integer.parseInt(elementList.get(i).getAttributeValue(NBVALUES));
			
			if(nbValues!=Domain.length){
				printMessage("nbValues!=domain.length");
				return false;
			}
			
			problem.domains.put(elementList.get(i).getAttributeValue(NAME),Domain);
		}
		
		return true;
	}
	
	private List<Variable> parseVariables(Element element){

		if(element==null){
			return null;
		}
		
		int nbVariables=-1;
		
		try{
			nbVariables=Integer.parseInt(element.getAttributeValue(NBVARIABLES));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		List<Element> elementList=element.getChildren();
		
		if(nbVariables!=elementList.size()){
			printMessage("nbVariables!=elementList.size()");
			return null;
		}
		
		List<Variable> variables = new ArrayList<Variable>();
		
		for(int i = 0; i<nbVariables; i++){
			String name = elementList.get(i).getAttributeValue(NAME);
			String domainName = elementList.get(i).getAttributeValue(DOMAIN);
			String agentName = elementList.get(i).getAttributeValue(AGENT);
			variables.add(new Variable(name, domainName, agentName));
		}
		
		return variables;
	}
	
	final class Variable{
		
		private String name;
		private String domainName;
		private String agentName;	
		
		public Variable(String name, String domainName, String agentName) {
			super();
			this.name = name;
			this.domainName = domainName;
			this.agentName = agentName;
		}
		
		public String getName(){
			return name;
		}	
		
		public String getDomainName(){
			return domainName;
		}	
		
		public String getAgentName(){
			return agentName;
		}
	}
}
