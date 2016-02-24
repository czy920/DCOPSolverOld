package com.cqu.util;

public class StatisticUtil {
	
	/**
	 * 返回arr[]中的最大元素序号
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
	
	/**
	 * 返回arr[]中的最小元素序号
	 * @param arr
	 * @return
	 */
	public static int min(int[] arr)
	{
		int minIndex=0;
		int minValue=arr[0];
		for(int i=1;i<arr.length;i++)
		{
			if(minValue > arr[i])
			{
				minIndex=i;
				minValue=arr[i];
			}
		}
		return minIndex;
	}
	/**
	 * 返回arr[]中的最小元素，序号，最大元素，序号，平均值
	 * @param arr
	 * @return {minValue, minIndex, maxValue, maxIndex, avgValue}
	 */
	public static int[] minMaxAvg(int[] arr)
	{
		int minIndex=0;
		int minValue=arr[0];
		int maxIndex=0;
		int maxValue=arr[0];
		int total=arr[0];
		for(int i=1;i<arr.length;i++)
		{
			if(minValue>arr[i])
			{
				minIndex=i;
				minValue=arr[i];
			}
			if(maxValue<arr[i])
			{
				maxIndex=i;
				maxValue=arr[i];
			}
			total+=arr[i];
		}
		return new int[]{minValue, minIndex, maxValue, maxIndex, total/arr.length};
	}
}
