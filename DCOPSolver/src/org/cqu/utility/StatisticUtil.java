package org.cqu.utility;

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
	 * 
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
