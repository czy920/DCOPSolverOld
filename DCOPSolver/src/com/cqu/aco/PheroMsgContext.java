package com.cqu.aco;

import java.util.HashMap;

public class PheroMsgContext {
	int ant;
	double delta;
	HashMap<Integer, Double> betterDelta;
	int bestCost;
	String endBestAnt;
	int cycle;

	public PheroMsgContext(int cycle, int ant, double delta, int bestCost, String endBestAnt) {
		super();
		this.cycle = cycle;
		this.ant = ant;
		this.delta = delta;
		this.bestCost = bestCost;
		this.endBestAnt = endBestAnt;
	}
	
	public PheroMsgContext(int cycle, int ant, HashMap<Integer, Double> betterDelta, int bestCost, String endBestAnt) {
		super();
		this.cycle = cycle;
		this.ant = ant;
		this.betterDelta = new HashMap<Integer, Double>();
		this.betterDelta.putAll(betterDelta);
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

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}	
	
	

}
