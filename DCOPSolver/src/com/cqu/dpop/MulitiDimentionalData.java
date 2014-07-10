package com.cqu.dpop;

import java.util.HashMap;
import java.util.Map;

import com.cqu.util.CollectionUtil;

public class MulitiDimentionalData {
	
	public static final int REDUCT_DIMENTION_WITH_MAX=0;
	public static final int REDUCT_DIMENTION_WITH_MIN=1;
	
	private int[] data;
	private int[] dimentionLengths;
	private Map<String, Integer> dimentionNames;
	private int[] reductDimentionResultIndexes;
	
	public MulitiDimentionalData(int[] data, int[] dimentionLengths, Map<String, Integer> dimentionNames, int[] reductDimentionResultIndexes) {
		// TODO Auto-generated constructor stub
		this.data=data;
		this.dimentionLengths=dimentionLengths;
		this.dimentionNames=dimentionNames;
		this.reductDimentionResultIndexes=reductDimentionResultIndexes;
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
				int index=dimentionNames.get(name);
				if(index<dimentionIndex)
				{
					dimentionNamesNew.put(name, index);
				}else
				{
					dimentionNamesNew.put(name, index-1);
				}
			}
		}
		
		int[] dataNew=new int[data.length/dimentionToReductLength];
		int[] dimentionLengthsNew=new int[dimentionLengths.length-1];
		for(int i=0;i<dimentionIndex;i++)
		{
			dimentionLengthsNew[i]=dimentionLengths[i];
		}
		for(int i=dimentionIndex+1;i<dimentionLengths.length;i++)
		{
			dimentionLengthsNew[i-1]=dimentionLengths[i];
		}
		
		//降低指定维度
		int outerPeriod=1;
		for(int i=dimentionIndex;i<dimentionLengths.length;i++)
		{
			outerPeriod=outerPeriod*dimentionLengths[i];
		}
		int innerPeriod=outerPeriod/dimentionLengths[dimentionIndex];
		//投影或聚集
		int[] resultIndexes=new int[dataNew.length];
		int indexTemp=0;
		if(reductDimentionMethod==REDUCT_DIMENTION_WITH_MIN)
		{
			
			for(int i=0;i<data.length;i+=outerPeriod)
			{
				for(int j=0;j<innerPeriod;j++)
				{
					indexTemp=i*innerPeriod/outerPeriod+j;
					int valueChoosen=Integer.MAX_VALUE;
					for(int k=0;k<outerPeriod;k+=innerPeriod)
					{
						if(valueChoosen>data[i+j+k])
						{
							valueChoosen=data[i+j+k];
							resultIndexes[indexTemp]=k/innerPeriod;
						}
					}
					dataNew[indexTemp]=valueChoosen;
				}
			}
		}else
		{
			
			for(int i=0;i<data.length;i+=outerPeriod)
			{
				for(int j=0;j<innerPeriod;j++)
				{
					indexTemp=i*innerPeriod/outerPeriod+j;
					int valueChoosen=Integer.MIN_VALUE;
					for(int k=0;k<outerPeriod;k+=innerPeriod)
					{
						if(valueChoosen<data[i+j+k])
						{
							valueChoosen=data[i+j+k];
							resultIndexes[indexTemp]=k/innerPeriod;
						}
					}
					dataNew[indexTemp]=valueChoosen;
				}
			}
		}
		
		return new MulitiDimentionalData(dataNew, dimentionLengthsNew, dimentionNamesNew, resultIndexes);
	}
	
	public int[] getReductDimentionResultIndexes()
	{
		return this.reductDimentionResultIndexes;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return CollectionUtil.arrayToString(data);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Map<String, Integer> names=new HashMap<String, Integer>();
		names.put("A1", 0);
		names.put("A2", 1);
		names.put("A3", 2);
		MulitiDimentionalData data=new MulitiDimentionalData(new int[]{1, 3, 2, 4, 2, 8, 3, 1}, new int[]{2, 2, 2}, names , null);
		System.out.println(data.reductionDimention("A1", REDUCT_DIMENTION_WITH_MIN).toString());
		System.out.println(data.reductionDimention("A2", REDUCT_DIMENTION_WITH_MIN).toString());
		System.out.println(data.reductionDimention("A3", REDUCT_DIMENTION_WITH_MIN).toString());
		
		System.out.println(data.reductionDimention("A1", REDUCT_DIMENTION_WITH_MIN).reductionDimention("A2", REDUCT_DIMENTION_WITH_MIN).reductionDimention("A3", REDUCT_DIMENTION_WITH_MIN));
	}
}
