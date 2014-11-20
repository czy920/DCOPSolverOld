package com.cqu.settings;

public class Settings {
	
	public static Settings settings=new Settings();
	
	private int communicationTimeInDPOPs;
	private int communicationNCCCInAdopts;
	private int BNBmergeADOPTboundArg;
	private boolean displayGraphFrame;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		this.communicationTimeInDPOPs=0;
		this.communicationNCCCInAdopts=0;
		this.BNBmergeADOPTboundArg=2;
		this.displayGraphFrame=true;
	}
	
	public int getBNBmergeADOPTboundArg() {
		return BNBmergeADOPTboundArg;
	}

	public void setBNBmergeADOPTboundArg(int bNBmergeADOPTboundArg) {
		BNBmergeADOPTboundArg = bNBmergeADOPTboundArg;
	}

	public int getCommunicationTimeInDPOPs() {
		return communicationTimeInDPOPs;
	}

	public void setCommunicationTimeInDPOPs(int communicationTimeInDPOPs) {
		this.communicationTimeInDPOPs = communicationTimeInDPOPs;
	}

	public int getCommunicationNCCCInAdopts() {
		return communicationNCCCInAdopts;
	}

	public void setCommunicationNCCCInAdopts(int communicationNCCCInAdopts) {
		this.communicationNCCCInAdopts = communicationNCCCInAdopts;
	}

	public boolean isDisplayGraphFrame() {
		return displayGraphFrame;
	}

	public void setDisplayGraphFrame(boolean displayGraphFrame) {
		this.displayGraphFrame = displayGraphFrame;
	}
}
