package com.cqu.bnbadopt;

import java.util.HashMap;
import java.util.Map;

public class Context implements Cloneable{
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Context newContext=null;
		newContext= (Context) super.clone();
		Map<Integer, Map<Integer, Integer>> newcontext=new HashMap<Integer, Map<Integer, Integer>>();
		newcontext.putAll(this.context);
		newContext.setContext(newcontext);
		return newContext;
	}

	private Map<Integer, Map<Integer,Integer>> context;
	
	public Map<Integer, Map<Integer,Integer>> getContext() {
		return context;
	}

	public void setContext(Map<Integer, Map<Integer,Integer>> context) {
		this.context = context;
	}

	
	
	public Context() {
		// TODO Auto-generated constructor stub
		context=new HashMap<Integer, Map<Integer,Integer>>();
	}
	
	public Context(Context c)
	{
		context=new HashMap<Integer, Map<Integer,Integer>>();
		context.putAll(c.getContext());
	}
	
	public void reset()
	{
		context=new HashMap<Integer, Map<Integer,Integer>>();
	}
	
	public void addOrUpdate(Integer key, Integer value,Integer ID)
	{
	    HashMap<Integer,Integer> val= new HashMap<Integer,Integer>();
		val.put(value,ID);
		if(!context.containsKey(key)||(context.containsKey(key)&&(this.get(key)!=value||this.getID(key)<ID)))
			context.put(key,val);   //增加更新的内容，会不会旧的反而覆盖新的？
	}
	
	public int get(int key)
	{
		int value=-1;
		if(context.containsKey(key)==true)
		{
			value=context.get(key).keySet().iterator().next();
		}
		return value;
	}
	
	public int getID(int key)
	{
		int ID=-1;
		if(context.containsKey(key)==true)
		{
			ID=context.get(key).values().iterator().next();
		}
		return ID;
	}
	
	public int Remove(int key)
	{
		HashMap<Integer,Integer> val=new HashMap<Integer,Integer>();
		int value=-1;
		if(context.containsKey(key)==true)
		{
			 val=(HashMap<Integer, Integer>) context.remove(key);
			 value=val.keySet().iterator().next();

		}
		
		return value;
	}
	
	public void remove(int[] keys)
	{
		for(int key : keys)
		{
			Remove(key);
		
		}
	}
	
	public void union(Context c)
	{
		for(Integer key : c.context.keySet())
		{
			if(this.context.containsKey(key)&&this.getID(key)>c.getID(key))continue;
			HashMap<Integer,Integer> val=new HashMap<Integer,Integer>();
			val.put(c.get(key), c.getID(key));
			this.context.put(key, val);
		}
	}
	
	/**
	 * to check this context weather it is compatible with other context(child context,parent value)
	 * @param c
	 * @return
	 */
	
	public boolean compatible(Context c)    
	{
		for(Integer key : c.context.keySet())
		{
			if(this.context.containsKey(key)==true&&(this.get(key)!=c.get(key)||this.getID(key) != c.getID(key)))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return context.toString();
	}
}

