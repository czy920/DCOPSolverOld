package com.cqu.adopt;

import java.util.HashMap;
import java.util.Map;

public class Context{
	
	private Map<Integer, Integer> context;
	
	public Context() {
		// TODO Auto-generated constructor stub
		context=new HashMap<Integer, Integer>();
	}
	
	public Context(Context c)
	{
		context=new HashMap<Integer, Integer>();
		for(Integer key : c.context.keySet())
		{
			context.put(key, c.context.get(key));
		}
	}
	
	public void reset()
	{
		context=new HashMap<Integer, Integer>();
	}
	
	public void addOrUpdate(Integer key, Integer value)
	{
		context.put(key, value);
	}
	
	public int get(int key)
	{
		int value=-1;
		if(context.containsKey(key)==true)
		{
			value=context.get(key);
		}
		return value;
	}
	
	public int remove(int key)
	{
		int value=-1;
		if(context.containsKey(key)==true)
		{
			value=context.remove(key);
		}
		return value;
	}
	
	public void remove(int[] keys)
	{
		for(int key : keys)
		{
			context.remove(key);
		}
	}
	
	public void union(Context c)
	{
		for(Integer key : c.context.keySet())
		{
			context.put(key, c.context.get(key));
		}
	}
	
	public boolean compatible(Context c)
	{
		for(Integer key : c.context.keySet())
		{
			if(context.containsKey(key)==true&&context.get(key).equals(c.context.get(key))==false)
			{
				return false;
			}
		}
		return true;
	}
}
