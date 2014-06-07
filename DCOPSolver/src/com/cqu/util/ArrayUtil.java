package com.cqu.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {
	
	public static String arrayToString(int[] localCosts_)
	{
		String str="[";
		for(int e : localCosts_)
		{
			str+=e+",";
		}
		str=str.substring(0, str.length()-1);
		str+="]";
		return str;
	}
	
	public static int[] except(int[] arrA, int[] arrB)
	{
		List<Integer> exceptList=new ArrayList<Integer>();
		for(int i=0;i<arrA.length;i++)
		{
			if(indexOf(arrB, arrA[i])==-1)
			{
				exceptList.add(arrA[i]);
			}
		}
		if(exceptList.size()==0)
		{
			return null;
		}else
		{
			Integer[] ret=new Integer[exceptList.size()];
			return toInt(exceptList.toArray(ret));
		}
	}
	
	public static int[] toInt(Integer[] arr)
	{
		int[] ret=new int[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			ret[i]=arr[i];
		}
		return ret;
	}
	
	public static int indexOf(int[] arr, int e)
	{
		for(int i=0;i<arr.length;i++)
		{
			if(arr[i]==e)
			{
				return i;
			}
		}
		return -1;
	}
}
