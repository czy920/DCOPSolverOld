package com.cqu.aco;

import com.cqu.core.Infinity;

public class PheroMsgContext {
	int ant;
	double delta;
	int bestCost;
	String endBestAnt;
	public PheroMsgContext(int ant, double delta, int bestCost, String endBestAnt) {
		super();
		this.ant = ant;
		this.delta = delta;
		this.bestCost = bestCost;
		this.endBestAnt = endBestAnt;
	}
	public int getAnt() {
		return ant;
	}
	public void setAnt(int ant) {
		this.ant = ant;
	}
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		this.delta = delta;
	}
	public int getBestCost() {
		return bestCost;
	}
	public void setBestCost(int bestCost) {
		this.bestCost = bestCost;
	}
	public String getEndBestAnt() {
		return endBestAnt;
	}
	public void setEndBestAnt(String endBestAnt) {
		this.endBestAnt = endBestAnt;
	}	

}
