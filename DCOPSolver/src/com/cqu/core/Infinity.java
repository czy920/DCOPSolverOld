package com.cqu.core;

public class Infinity {
	
	public final static int INFINITY=Integer.MAX_VALUE;
	
	public static int add(int a, int b)
	{
		if(a==Infinity.INFINITY||b==Infinity.INFINITY)
		{
			return Infinity.INFINITY;
		}else
		{
			return a+b;
		}
	}
	
	public static int minus(int a, int b)
	{
		if(a==Infinity.INFINITY)
		{
			return Infinity.INFINITY;
		}else
		{
			return a-b;
		}
	}
	
	public static String infinityEasy(int value)
	{
		if(value==Integer.MAX_VALUE)
		{
			return "Inf";
		}else if(value==-Integer.MAX_VALUE)
		{
			return "-Inf";
		}else
		{
			return value+"";
		}
	}
}
