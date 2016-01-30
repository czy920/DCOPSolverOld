package com.cqu.aco;

public class PheroMsgContext {
	int ant;
	double delta;
	public PheroMsgContext(int ant, double delta) {
		super();
		this.ant = ant;
		this.delta = delta;
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
	
	

}
