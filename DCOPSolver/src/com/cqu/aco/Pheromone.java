package com.cqu.aco;

import java.util.Arrays;

public class Pheromone {
	
	private double[][] tau;
	
	public Pheromone(int myDomain, int neighborDomain){
		tau = new double[myDomain][neighborDomain];
	}
	
	
	public void updatePheromone(int myValue, int neighborValue, double new_tau){
		tau[myValue][neighborValue] = new_tau;
	}
	
	public void initValue(double value){
		for(int i =0 ;i < tau.length; i++){
			for(int j =0; j < tau[i].length; j++){
				tau[i][j] = value;
			}
		}
	}


	public double[][] getTau() {
		return tau;
	}


	public void setTau(double[][] tau) {
		this.tau = tau;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "[";
		for(int i = 0; i< tau.length; i++){
			str += Arrays.toString(tau[i]);
			if(i != tau.length-1)
				str += ",";
		}
		str += "]";
		return str;
	}
	
	
	

}
