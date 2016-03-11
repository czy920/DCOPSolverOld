package com.cqu.settings;

import javax.swing.JDialog;

public class Settings {
	
	public static Settings settings=new Settings();
	
	private int communicationTimeInDPOPs;
	private int communicationNCCCInAdopts;
	private double BNBmergeADOPTboundArg;
	private int ADOPT_K;
	private int maxDimensionInMBDPOP;
	
	//蚁群算法参数设置
	private long MaxCycle;
	private long countAnt;
	private int alpha;
	private int beta;
	private double rho;
	private double Min_tau;
	private double Max_tau;
	
	//BFSDPOP
	private int clusterRemovingChoice;
	
	private int cycleCountEnd;
	private double selectProbability;
	private double selectNewProbability;
	private double selectProbabilityA;
	private double selectProbabilityB;
	private double selectProbabilityC;
	private double selectProbabilityD;
	private int selectInterval;
	private int selectStepK1;
	private int selectStepK2;
	private int selectRound;
	
	
	private boolean displayGraphFrame;
	
	public Settings() {
		// TODO Auto-generated constructor stub
		this.communicationTimeInDPOPs=0;
		this.communicationNCCCInAdopts=0;
		this.BNBmergeADOPTboundArg=0.5;
		this.ADOPT_K=2;
		this.displayGraphFrame=true;
		this.maxDimensionInMBDPOP=3;
		
		//蚁群算法参数设置
		this.MaxCycle = 100;
		this.countAnt = 2;
		this.alpha = 2;
		this.beta = 8;
		this.rho = 0.02;
		this.Min_tau = 0.1;
		this.Max_tau = 10;
		
		//BFSDPOP
		this.clusterRemovingChoice=0;
		
		this.cycleCountEnd = 20;
		this.selectProbability = 0.3;
		this.selectNewProbability = 0.5;
		this.selectInterval = 20;
		this.selectStepK1 = 20;
		this.selectStepK2 = 20;
		this.selectRound = 60;
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
	
	

	public long getMaxCycle() {
		return MaxCycle;
	}

	public void setMaxCycle(long maxCycle) {
		MaxCycle = maxCycle;
	}

	public long getCountAnt() {
		return countAnt;
	}

	public void setCountAnt(long countAnt) {
		this.countAnt = countAnt;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	public double getMin_tau() {
		return Min_tau;
	}

	public void setMin_tau(double min_tau) {
		Min_tau = min_tau;
	}

	public double getMax_tau() {
		return Max_tau;
	}

	public void setMax_tau(double max_tau) {
		Max_tau = max_tau;
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
	
	public int getMaxDimensionsInMBDPOP() {
		return maxDimensionInMBDPOP;
	}

	public void setMaxDimensionsInMBDPOP(int maxDimensionsInAgileDPOP) {
		this.maxDimensionInMBDPOP = maxDimensionsInAgileDPOP;
	}
	
	public int getClusterRemovingChoice() {
		return clusterRemovingChoice;
	}

	public void setClusterRemovingChoice(int clusterRemovingChoice) {
		this.clusterRemovingChoice = clusterRemovingChoice;
	}

	public int getCycleCountEnd() {
		return cycleCountEnd;
	}

	public void setCycleCount(int myCycleCountEnd) {
		cycleCountEnd = myCycleCountEnd;
	}
	
	public double getSelectProbability() {
		return selectProbability;
	}

	public void setSelectProbability(double mySelectProbability) {
		selectProbability = mySelectProbability;
	}
	
	public double getSelectNewProbability() {
		return selectNewProbability;
	}

	public void setSelectNewProbability(double mySelectNewProbability) {
		selectNewProbability = mySelectNewProbability;
	}

	public double getSelectProbabilityA() {
		return selectProbabilityA;
	}

	public void setSelectProbabilityA(double mySelectProbabilityA) {
		selectProbabilityA = mySelectProbabilityA;
	}

	public double getSelectProbabilityB() {
		return selectProbabilityB;
	}

	public void setSelectProbabilityB(double mySelectProbabilityB) {
		selectProbabilityB = mySelectProbabilityB;
	}

	public double getSelectProbabilityC() {
		return selectProbabilityC;
	}

	public void setSelectProbabilityC(double mySelectProbabilityC) {
		selectProbabilityC = mySelectProbabilityC;
	}

	public double getSelectProbabilityD() {
		return selectProbabilityD;
	}

	public void setSelectProbabilityD(double mySelectProbabilityD) {
		selectProbabilityD = mySelectProbabilityD;
	}
	
	public int getSelectInterval() {
		return selectInterval;
	}

	public void setSelectInterval(int mySelectInterval) {
		selectInterval = mySelectInterval;
	}
	
	public int getSelectStepK1() {
		return selectStepK1;
	}

	public void setSelectStepK1(int mySelectStepK1) {
		selectStepK1 = mySelectStepK1;
	}

	public int getSelectStepK2() {
		return selectStepK2;
	}

	public void setSelectStepK2(int mySelectStepK2) {
		selectStepK2 = mySelectStepK2;
	}

	public int getSelectRound() {
		return selectRound;
	}

	public void setSelectRound(int mySelectRound) {
		selectRound = mySelectRound;
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
