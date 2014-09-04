package com.cqu.bnbmergeadopt;

public class ValueMsg {
	int[] val;
	String typeMethod;
	ValueMsg(){
		val=new int[3];
		typeMethod=new String();
	}
	ValueMsg(int[] value,String typeMethod){
		this.val=value;
		this.typeMethod=typeMethod;
	}
	public int[] getVal() {
		return val;
	}
	public void setVal(int[] val) {
		this.val = val;
	}
	public String gettypeMethod() {
		return typeMethod;
	}
	public void settypeMethod(String typeMethod) {
		this.typeMethod = typeMethod;
	}
	

}
