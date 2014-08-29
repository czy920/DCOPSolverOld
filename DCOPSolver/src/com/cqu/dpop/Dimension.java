package com.cqu.dpop;

public class Dimension implements Comparable<Dimension>{
	
	private String name;
	private int size;
	private Integer priority;
	
	private int constraintCountTotal;
	private int constraintCount;
	
	public Dimension(String name, int size, Integer priority) {
		super();
		this.name = name;
		this.size = size;
		this.priority = priority;
		
		this.constraintCountTotal=Integer.MAX_VALUE;
		this.constraintCount=0;
	}
	
	public Dimension(String name, int size, Integer priority, int constraintCountTotal, int constraintCount) {
		super();
		this.name = name;
		this.size = size;
		this.priority = priority;
		
		this.constraintCountTotal=constraintCountTotal;
		this.constraintCount=constraintCount;
	}
	
	public Dimension(Dimension dimen)
	{
		this.name=dimen.name;
		this.size=dimen.size;
		this.priority=dimen.priority;
		
		this.constraintCountTotal=dimen.constraintCountTotal;
		this.constraintCount=dimen.constraintCount;
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

	public void mergeConstraintCount(Dimension dimen) {
		this.constraintCount +=dimen.constraintCount;
		this.constraintCountTotal=Math.min(this.constraintCountTotal, dimen.constraintCountTotal);
	}
	
	public void setConstraintCountTotal(int constraintCountTotal) {
		this.constraintCountTotal = constraintCountTotal;
	}

	public boolean isReductable()
	{
		return this.constraintCount>=this.constraintCountTotal;
	}

	@Override
	public int compareTo(Dimension o) {
		// TODO Auto-generated method stub
		return this.priority.compareTo(o.priority);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+name+" "+size+" "+priority+" "+constraintCountTotal+" "+constraintCount+"]";
	}
}
