package com.cqu.bnbmergeadopt;

import java.util.HashMap;
import java.util.Map;

public class CostMsg {
	private Map<String, Object> cost;
	private String typeMethod;
	CostMsg(){
		cost=new HashMap<String,Object>();
		typeMethod=new String();
	}
	
	CostMsg(Map<String, Object> cost,String typeMethod){
		this.cost=cost;
		this.typeMethod=typeMethod;
	}

	public Map<String, Object> getCost() {
		return cost;
	}

	public void setCost(Map<String, Object> cost) {
		this.cost = cost;
	}

	public String gettypeMethod() {
		return typeMethod;
	}

	public void settypeMethod(String typeMethod) {
		this.typeMethod = typeMethod;
	}
	
	

}
