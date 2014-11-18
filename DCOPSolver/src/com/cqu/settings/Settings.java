package com.cqu.settings;

import javax.swing.JDialog;

public class Settings {
	
	public static Settings settings=new Settings();
	
	private int communicationTimeInDPOPs;
	private int communicationNCCCInAdopts;
	private double BNBmergeADOPTboundArg;
	private int ADOPT_K;

	private boolean displayGraphFrame;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		this.communicationTimeInDPOPs=0;
		this.communicationNCCCInAdopts=0;
		this.BNBmergeADOPTboundArg=0.5;
		this.ADOPT_K=2;
		this.displayGraphFrame=true;
	}
	
	public double getBNBmergeADOPTboundArg() {
		return BNBmergeADOPTboundArg;
	}

	public void setBNBmergeADOPTboundArg(double bNBmergeADOPTboundArg) {
		BNBmergeADOPTboundArg = bNBmergeADOPTboundArg;
	}
	
	public int getADOPT_K() {
		return ADOPT_K;
	}

	public void setADOPT_K(int aDOPT_K) {
		ADOPT_K = aDOPT_K;
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

	public static Settings showSettingsDialog()
	{
		DialogSettings dialog = new DialogSettings(settings);
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setModal(true);
		dialog.setVisible(true);
		return settings;
	}
}
