package com.cqu.aco;

public class ValueMsgContext {
	private int ant;
	private int currentCost;
	private Context context;
	private int cycle;   //非流水时取值为0
	
	public ValueMsgContext(int cycle, int ant, int currentCost, Context context) {
		super();
		this.cycle =cycle;
		this.ant = ant;
		this.currentCost = currentCost;
		this.context = context;
	}

	public int getAnt() {
		return ant;
	}

	public void setAnt(int ant) {
		this.ant = ant;
	}

	public int getCurrentCost() {
		return currentCost;
	}

	public void setCurrentCost(int currentCost) {
		this.currentCost = currentCost;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}	

}
