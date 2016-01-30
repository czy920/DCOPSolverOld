package com.cqu.aco;

public class ValueMsgContext {
	private int ant;
	private int currentCost;
	private Context context;
	
	public ValueMsgContext(int ant, int currentCost, Context context) {
		super();
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
	
	
	

}
