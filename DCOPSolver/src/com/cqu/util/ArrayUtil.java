package com.cqu.util;

public class ArrayUtil {
	
	public static <T> String arrayToString(int[] localCosts_)
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
}
