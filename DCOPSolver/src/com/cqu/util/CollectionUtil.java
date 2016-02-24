package com.cqu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtil {
	
	/**
	 * 返回arrA-arrB
	 * @param arrA
	 * @param arrB
	 * @return
	 */
	public static int[] except(int[] arrA, int[] arrB)
	{
		if(arrA==null||arrB==null)
		{
			return null;
		}
		
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
	
	/**
	 * Integer[]转换为int[]
	 * @param arr
	 * @return
	 */
	public static int[] toInt(Integer[] arr)
	{
		if(arr==null)
		{
			return null;
		}
		
		int[] ret=new int[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			ret[i]=arr[i];
		}
		return ret;
	}
	
	/**
	 * List<Integer>转换为int[]
	 * @param list
	 * @return
	 */
	public static int[] toInt(List<Integer> list)
	{
		if(list==null)
		{
			return null;
		}
		
		int[] ret=new int[list.size()];
		for(int i=0;i<list.size();i++)
		{
			ret[i]=list.get(i);
		}
		
		return ret;
	}
	
	/**
	 * int[]转换为Integer[]
	 * @param arr
	 * @return
	 */
	public static Integer[] toInteger(int[] arr)
	{
		Integer[] ret=new Integer[arr.length];
		for(int i=0;i<arr.length;i++)
		{
			ret[i]=arr[i];
		}
		return ret;
	}
	
	/**
	 * 返回元素e在arr中的首次序号
	 * @param arr
	 * @param e
	 * @return
	 */
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
	
	/**
	 * 返回元素e在arr中的首次序号
	 * @param arr
	 * @param e
	 * @return
	 */
	public static int indexOf(String[] arr, String e)
	{
		for(int i=0;i<arr.length;i++)
		{
			if(arr[i].equals(e))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 返回元素value在list中的首次序号
	 * @param list
	 * @param value
	 * @return
	 */
	public static int indexOf(List<Integer> list, Integer value)
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
	
	/**
	 * 一维数组转换为二维数组
	 * @param arr
	 * @param rows
	 * @param cols
	 * @return
	 */
	public static int[][] toTwoDimension(int[] arr, int rows, int cols)
	{
		if(arr==null||rows*cols!=arr.length)
		{
			return null;
		}
		
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
	
	/**
	 * Map<Integer, List<Integer>>转换为Map<Integer, int[]>
	 * @param raw
	 * @return
	 */
	public static Map<Integer, int[]> transform(Map<Integer, List<Integer>> raw)
	{
		if(raw==null)
		{
			return null;
		}
		
		Map<Integer, int[]> ret=new HashMap<Integer, int[]>();
		for(Integer id : raw.keySet())
		{
			ret.put(id, CollectionUtil.toInt(raw.get(id)));
		}
		return ret;
	}
	
	/**
	 * int[]转换为打印字符串
	 * @param arr
	 * @return
	 */
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
