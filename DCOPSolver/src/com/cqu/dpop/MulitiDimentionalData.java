package com.cqu.dpop;

import java.util.HashMap;
import java.util.Map;

public class MulitiDimentionalData {
	
	public static final int REDUCT_DIMENTION_WITH_MAX=0;
	public static final int REDUCT_DIMENTION_WITH_MIN=1;
	
	private int[] data;
	private int[] dimentionLengths;
	private Map<String, Integer> dimentionNames;
	
	public MulitiDimentionalData(int[] data, int[] dimentionLengths, Map<String, Integer> dimentionNames) {
		// TODO Auto-generated constructor stub
		this.data=data;
		this.dimentionLengths=dimentionLengths;
		this.dimentionNames=dimentionNames;
	}
	
	public MulitiDimentionalData reductionDimention(String dimentionName, int reductDimentionMethod)
	{
		int dimentionIndex=dimentionNames.get(dimentionName);
		int dimentionToReductLength=dimentionLengths[dimentionIndex];
		
		Map<String, Integer> dimentionNamesNew=new HashMap<String, Integer>();
		for(String name : dimentionNames.keySet())
		{
			if(name.equals(dimentionName)==false)
			{
				dimentionNamesNew.put(name, dimentionNames.get(name));
			}
		}
		
		int[] dataNew=new int[data.length/dimentionToReductLength];
		int[] dimentionLengthsNew=new int[dimentionLengths.length-1];
		for(int i=0;i<dimentionIndex;i++)
		{
			dimentionLengthsNew[i]=i;
		}
		for(int i=dimentionIndex+1;i<dimentionLengths.length;i++)
		{
			dimentionLengthsNew[i-1]=i;
		}
		
		//降低指定维度
		
		return new MulitiDimentionalData(dataNew, dimentionLengthsNew, dimentionNamesNew);
	}
}
