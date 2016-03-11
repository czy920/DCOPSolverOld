package com.cqu.maxsum;

public class NodeInfo {

	private int domainSize;
	private int agentId;
	private boolean previousThanCurrent;
	
	public NodeInfo() {
		// TODO Auto-generated constructor stub
	}
	public NodeInfo(int domainSize,int agentId){
		this.domainSize=domainSize;
		this.agentId=agentId;
	}
	public NodeInfo(int domainSize,int agentId,boolean previousThanCurrent){
		this.domainSize=domainSize;
		this.agentId=agentId;
		this.previousThanCurrent=previousThanCurrent;
	}
	public void setAgentId(int agentId) {
		this.agentId = agentId;
	}
	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}
	public int getAgentId() {
		return agentId;
	}
	public int getDomainSize() {
		return domainSize;
	}
	public void setPreviousThanCurrent(boolean previousThanCurrent) {
		this.previousThanCurrent = previousThanCurrent;
	}
	public boolean getPreviousThanCurrent(){
		return previousThanCurrent;
	}

}
