package com.cqu.core;

public class ResultAdopt extends Result{
	
	public int nccc;
	
	public ResultAdopt() {
		// TODO Auto-generated constructor stub
	}
	
	public ResultAdopt(Result rs) {
		super(rs);
		// TODO Auto-generated constructor stub
		this.nccc=((ResultAdopt)rs).nccc;
	}
	
	@Override
	public void min(Result rs) {
		// TODO Auto-generated method stub
		super.min(rs);
		this.nccc=Math.min(this.nccc, ((ResultAdopt)rs).nccc);
	}
	
	@Override
	public void max(Result rs) {
		// TODO Auto-generated method stub
		super.max(rs);
		this.nccc=Math.max(this.nccc, ((ResultAdopt)rs).nccc);
	}
	
	
	
	@Override
	public void add(Result rs, int validCount) {
		// TODO Auto-generated method stub
		super.add(rs, validCount);
		this.nccc+=(int)(1.0*((ResultAdopt)rs).nccc/validCount);
	}
	
	@Override
	public void minus(Result rs, int validCount) {
		// TODO Auto-generated method stub
		super.minus(rs, validCount);
		this.nccc-=(int)(1.0*((ResultAdopt)rs).nccc/validCount);
	}
}
