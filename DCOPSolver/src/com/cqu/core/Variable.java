package com.cqu.core;

public class Variable {
	
	private int id;
	private String name;
	private int[] domain;
	private int index;
	
	public Variable(int id, String name, int[] domain, int index) {
		super();
		this.id = id;
		this.name = name;
		this.domain = domain;
		this.index = index;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int[] getDomain() {
		return domain;
	}
	
	public void setDomain(int[] domain) {
		this.domain = domain;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
}
