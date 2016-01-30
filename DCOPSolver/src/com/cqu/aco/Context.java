package com.cqu.aco;

import java.util.HashMap;
import java.util.Map;

public class Context implements Cloneable{
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		Context newContext=null;
		newContext= (Context) super.clone();
		Map<Integer, Integer> newcontext=new HashMap<Integer, Integer>();
		newcontext.putAll(this.context);
		return newContext;
	}

	private Map<Integer, Integer> context;
	
	public Map<Integer, Integer> getContext() {
		return context;
	}

	public void setContext(Map<Integer, Integer> context) {
		this.context = context;
	}
	
	public Context() {
		// TODO Auto-generated constructor stub
		context=new HashMap<Integer ,Integer>();
	}
	
	public Context(Context c)
	{
		context=new HashMap<Integer, Integer>();
		context.putAll(c.getContext());
	}
	
	public void reset()
	{
		context=new HashMap<Integer, Integer>();
	}
		
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return context.toString();
	}
	
	public void merge(Context con){
		for(int i : con.getContext().keySet()){
			this.context.put(i, con.getContext().get(i).intValue());
		}
	}
}

