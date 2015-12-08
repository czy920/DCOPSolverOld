package com.cqu.core;

public class ResultDPOP extends Result{
	
	public double utilMsgCount;
	public double utilMsgSizeMin;
	public double utilMsgSizeMax;
	public double utilMsgSizeAvg;
	
	public ResultDPOP() {
		// TODO Auto-generated constructor stub
	}
	
	public ResultDPOP(Result rs) {
		super(rs);
		// TODO Auto-generated constructor stub
		this.utilMsgCount=((ResultDPOP)rs).utilMsgCount;
		this.utilMsgSizeMin=((ResultDPOP)rs).utilMsgSizeMin;
		this.utilMsgSizeMax=((ResultDPOP)rs).utilMsgSizeMax;
		this.utilMsgSizeAvg=((ResultDPOP)rs).utilMsgSizeAvg;
	}
	
	@Override
	public void min(Result rs) {
		// TODO Auto-generated method stub
		super.min(rs);
		this.utilMsgCount=Math.min(this.utilMsgCount, ((ResultDPOP)rs).utilMsgCount);
		this.utilMsgSizeMin=Math.min(this.utilMsgSizeMin, ((ResultDPOP)rs).utilMsgSizeMin);
		this.utilMsgSizeMax=Math.min(this.utilMsgSizeMax, ((ResultDPOP)rs).utilMsgSizeMax);
		this.utilMsgSizeAvg=Math.min(this.utilMsgSizeAvg, ((ResultDPOP)rs).utilMsgSizeAvg);
	}
	
	@Override
	public void max(Result rs) {
		// TODO Auto-generated method stub
		super.max(rs);
		this.utilMsgCount=Math.max(this.utilMsgCount, ((ResultDPOP)rs).utilMsgCount);
		this.utilMsgSizeMin=Math.max(this.utilMsgSizeMin, ((ResultDPOP)rs).utilMsgSizeMin);
		this.utilMsgSizeMax=Math.max(this.utilMsgSizeMax, ((ResultDPOP)rs).utilMsgSizeMax);
		this.utilMsgSizeAvg=Math.max(this.utilMsgSizeAvg, ((ResultDPOP)rs).utilMsgSizeAvg);
	}
	
	
	
	@Override
	public void add(Result rs, int validCount) {
		// TODO Auto-generated method stub
		super.add(rs, validCount);
		this.utilMsgCount+=((ResultDPOP)rs).utilMsgCount/validCount;
		this.utilMsgSizeMin+=((ResultDPOP)rs).utilMsgSizeMin/validCount;
		this.utilMsgSizeMax+=((ResultDPOP)rs).utilMsgSizeMax/validCount;
		this.utilMsgSizeAvg+=((ResultDPOP)rs).utilMsgSizeAvg/validCount;
	}
	
	@Override
	public void minus(Result rs, int validCount) {
		// TODO Auto-generated method stub
		super.minus(rs, validCount);
		this.utilMsgCount-=((ResultDPOP)rs).utilMsgCount/validCount;
		this.utilMsgSizeMin-=((ResultDPOP)rs).utilMsgSizeMin/validCount;
		this.utilMsgSizeMax-=((ResultDPOP)rs).utilMsgSizeMax/validCount;
		this.utilMsgSizeAvg-=((ResultDPOP)rs).utilMsgSizeAvg/validCount;
	}
}
