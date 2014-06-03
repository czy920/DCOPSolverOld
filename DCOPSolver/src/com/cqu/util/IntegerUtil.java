package com.cqu.util;

public class IntegerUtil {
	
	public static String getEasyString(int value)
	{
		if(value==Integer.MAX_VALUE)
		{
			return "Inf";
		}else
		{
			return value+"";
		}
	}
}
