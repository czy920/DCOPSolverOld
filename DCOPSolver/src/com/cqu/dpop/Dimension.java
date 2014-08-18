package com.cqu.dpop;

public class Dimension implements Comparable<Dimension>{
	
	private String name;
	private int size;
	private Integer priority;
	
	public Dimension(String name, int size, Integer priority) {
		super();
		this.name = name;
		this.size = size;
		this.priority = priority;
	}
	
	public Dimension(Dimension dimen)
	{
		this.name=dimen.name;
		this.size=dimen.size;
		this.priority=dimen.priority;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public Integer getPriority() {
		return priority;
	}

	@Override
	public int compareTo(Dimension o) {
		// TODO Auto-generated method stub
		return this.priority.compareTo(o.priority);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+name+" "+size+" "+priority+"]";
	}
}
