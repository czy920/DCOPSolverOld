package com.cqu.util;

public class StatisticUtil {
	
	/**
	 * 
	 * @param arr
	 * @return maxIndex
	 */
	public static int max(int[] arr)
	{
		int maxIndex=0;
		int maxValue=arr[0];
		for(int i=1;i<arr.length;i++)
		{
			if(maxValue<arr[i])
			{
				maxIndex=i;
				maxValue=arr[i];
			}
		}
		return maxIndex;
	}
}
