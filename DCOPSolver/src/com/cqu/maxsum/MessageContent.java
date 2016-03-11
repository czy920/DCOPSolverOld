package com.cqu.maxsum;

public class MessageContent {

	private int currentValueIndex = -1;
	private HyperCube hyperCube;
	public MessageContent(LargerHyperCube largerHyperCube) {
		super();
		this.largerHyperCube = largerHyperCube;
	}
	public MessageContent(){
		
	}

	private LargerHyperCube largerHyperCube;
	
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
	
}
