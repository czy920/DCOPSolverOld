package com.cqu.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Debugger {
	
	public static boolean debugOn=true;
	public static Map<String, List<Integer>> valueChanges;
	
	public static void init(Map<Integer, String> agentNames)
	{
		valueChanges=new HashMap<String, List<Integer>>();
		for(Integer agentId : agentNames.keySet())
		{
			valueChanges.put(agentNames.get(agentId), new ArrayList<Integer>());
		}
	}
	
	public static void printValueChanges()
	{
		for(String agentName : valueChanges.keySet())
		{
			System.out.println(agentName+": "+valueChanges.get(agentName).toString());
		}
	}
}
