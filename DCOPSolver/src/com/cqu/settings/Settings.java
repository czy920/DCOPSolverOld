package com.cqu.settings;

import javax.swing.JDialog;

public class Settings {
	
	public static Settings settings=new Settings();
	
	private int communicationTimeInDPOPs;
	private int communicationNCCCInAdopts;
	private boolean displayGraphFrame;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		this.communicationTimeInDPOPs=0;
		this.communicationNCCCInAdopts=0;
		this.displayGraphFrame=true;
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
