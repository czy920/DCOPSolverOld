package com.cqu.maxsum;

import java.util.List;
import java.util.Map;

public class MessageContent {

	private int currentValueIndex = -1;
	private HyperCube hyperCube;
	private LargerHyperCube largerHyperCube;
	private List<Map<Integer, Integer>> optimalList;
	private int degree;
	private int random;
	
	public MessageContent(LargerHyperCube largerHyperCube) {
		super();
		this.largerHyperCube = largerHyperCube;
	}
	
	public MessageContent(LargerHyperCube largerHyperCube, List<Map<Integer, Integer>> optimalList){
		this(largerHyperCube);
		this.optimalList = optimalList;
	}
	
	public MessageContent(int degree, int radom){
		this.degree = degree;
		this.random = radom;
	}
	
	public MessageContent(){
		
	}	
	
	public MessageContent(HyperCube hyperCube){
		this.hyperCube=hyperCube;
		currentValueIndex=-1;
	}
	
	public MessageContent(HyperCube hyperCube,int currentValueIndex){
		this.currentValueIndex=currentValueIndex;
		this.hyperCube=hyperCube;
	}
	
	public int getCurrentValueIndex() {
		return currentValueIndex;
	}
	
	public HyperCube getHyperCube() {
		return hyperCube;
	}
	
	public void setCurrentValueIndex(int currentValueIndex) {
		this.currentValueIndex = currentValueIndex;
	}
	
	public void setHyperCube(HyperCube hyperCube) {
		this.hyperCube = hyperCube;
	}
	
	public LargerHyperCube getLargerHyperCube() {
		return largerHyperCube;
	}
	
	public void setLargerHyperCube(LargerHyperCube largerHyperCube) {
		this.largerHyperCube = largerHyperCube;
	}
	
	public List<Map<Integer, Integer>> getOptimalList() {
		return optimalList;
	}
	public int getDegree() {
		return degree;
	}
	public int getRandom() {
		return random;
	}
	
}
