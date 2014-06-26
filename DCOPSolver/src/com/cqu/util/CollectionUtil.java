package com.cqu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtil {
	
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
	
	public static int[] toInt(List<Integer> list)
	{
		Integer[] ret=new Integer[list.size()];
		return CollectionUtil.toInt(list.toArray(ret));
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
	
	public static int[][] toTwoDimension(int[] arr, int rows, int cols)
	{
		int[][] ret=new int[rows][cols];
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				ret[i][j]=arr[i*cols+j];
			}
		}
		return ret;
	}
	
	public static Map<Integer, int[]> transform(Map<Integer, List<Integer>> raw)
	{
		Map<Integer, int[]> ret=new HashMap<Integer, int[]>();
		for(Integer id : raw.keySet())
		{
			List<Integer> elementList=raw.get(id);
			Integer[] elements=new Integer[elementList.size()];
			ret.put(id, CollectionUtil.toInt(elementList.toArray(elements)));
		}
		return ret;
	}
	
	public static int exists(int[] arr, int value)
	{
		for(int i=0;i<arr.length;i++)
		{
			if(arr[i]==value)
			{
				return i;
			}
		}
		return -1;
	}
	
	public static int exists(List<Integer> list, Integer value)
	{
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i)==value)
			{
				return i;
			}
		}
		return -1;
	}
	
	public static String arrayToString(int[] arr)
	{
		if(arr==null||arr.length==0)
		{
			return "";
		}
		String str="[";
		for(int i=0;i<arr.length-1;i++)
		{
			str+=arr[i]+",";
		}
		str+=arr[arr.length-1]+"]";
		return str;
	}
}
