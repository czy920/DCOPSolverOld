package com.cqu.main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.JEditorPane;
import javax.swing.border.EtchedBorder;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class SolverWindow {

	private JFrame frmDcopsolver;
	
	private JMenuBar menuBar;
	
	private JTextField tfProblemPath;
	private JComboBox combAlgorithmType;
	private JSpinner spinnerRunTimes;
	private JLabel labelRunProgress;
	private JLabel labelFlagRunning;
	
	private JSpinner spinnerMessageTransmissionTime;
	private JSpinner spinnerMessageTransmissionNCCC;
	private JSpinner spinnerBnbLayer;
	private JCheckBox cbGraphFrame;
	private JCheckBox cbDebug;
	private JCheckBox cbTreeFrame;
	
	private JTextField tfTotalTime;
	private JTextField tfTotalCost;
	private JEditorPane epResultDetails;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SolverWindow window = new SolverWindow();
					window.frmDcopsolver.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SolverWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDcopsolver = new JFrame();
		frmDcopsolver.setResizable(false);
		frmDcopsolver.setTitle("DCOPSolver");
		frmDcopsolver.setBounds(100, 100, 687, 443);
		frmDcopsolver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		frmDcopsolver.setJMenuBar(menuBar);
		
		JMenu mnf = new JMenu("文件(F)");
		mnf.setMnemonic('F');
		menuBar.add(mnf);
		
		JMenuItem miOpen = new JMenuItem("打开");
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		mnf.add(miOpen);
		
		JMenuItem miOpenDir = new JMenuItem("打开目录");
		miOpenDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		mnf.add(miOpenDir);
		
		JMenu mnr = new JMenu("运行(R)");
		mnr.setMnemonic('R');
		menuBar.add(mnr);
		
		JMenuItem miRun = new JMenuItem("运行");
		miRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		mnr.add(miRun);
		
		JMenu mnh = new JMenu("帮助(H)");
		mnh.setMnemonic('H');
		menuBar.add(mnh);
		
		JMenuItem miAbout = new JMenuItem("关于");
		miAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		mnh.add(miAbout);
		frmDcopsolver.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setLayout(null);
		panel.setBounds(10, 10, 459, 259);
		frmDcopsolver.getContentPane().add(panel);
		
		JLabel label = new JLabel("路径：");
		label.setBounds(10, 13, 45, 15);
		panel.add(label);
		
		tfProblemPath = new JTextField();
		tfProblemPath.setColumns(10);
		tfProblemPath.setBounds(64, 10, 385, 21);
		panel.add(tfProblemPath);
		
		JLabel label_1 = new JLabel("算法：");
		label_1.setBounds(10, 47, 45, 15);
		panel.add(label_1);
		
		combAlgorithmType = new JComboBox();
		combAlgorithmType.setBounds(64, 41, 385, 32);
		panel.add(combAlgorithmType);
		
		spinnerRunTimes = new JSpinner();
		spinnerRunTimes.setBounds(64, 83, 65, 22);
		panel.add(spinnerRunTimes);
		
		labelRunProgress = new JLabel("0/0/0");
		labelRunProgress.setBounds(207, 86, 118, 15);
		panel.add(labelRunProgress);
		
		JLabel label_3 = new JLabel("遍数：");
		label_3.setBounds(10, 86, 45, 15);
		panel.add(label_3);
		
		labelFlagRunning = new JLabel("");
		labelFlagRunning.setHorizontalAlignment(SwingConstants.CENTER);
		labelFlagRunning.setBounds(196, 150, 54, 56);
		panel.add(labelFlagRunning);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(10, 278, 459, 106);
		frmDcopsolver.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel label_5 = new JLabel("DPOP类算法通信时间：");
		label_5.setBounds(10, 13, 141, 15);
		panel_1.add(label_5);
		
		spinnerMessageTransmissionTime = new JSpinner();
		spinnerMessageTransmissionTime.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spinnerMessageTransmissionTime.setBounds(161, 10, 70, 22);
		panel_1.add(spinnerMessageTransmissionTime);
		
		JLabel label_6 = new JLabel("每次消息通信NCCC：");
		label_6.setBounds(241, 16, 130, 15);
		panel_1.add(label_6);
		
		spinnerMessageTransmissionNCCC = new JSpinner();
		spinnerMessageTransmissionNCCC.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spinnerMessageTransmissionNCCC.setBounds(381, 13, 70, 22);
		panel_1.add(spinnerMessageTransmissionNCCC);
		
		JLabel label_7 = new JLabel("BNB合并算法分层：");
		label_7.setBounds(10, 45, 141, 15);
		panel_1.add(label_7);
		
		spinnerBnbLayer = new JSpinner();
		spinnerBnbLayer.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinnerBnbLayer.setBounds(161, 42, 70, 22);
		panel_1.add(spinnerBnbLayer);
		
		cbGraphFrame = new JCheckBox("每次显示GraphFrame");
		cbGraphFrame.setSelected(true);
		cbGraphFrame.setBounds(241, 41, 210, 23);
		panel_1.add(cbGraphFrame);
		
		cbDebug = new JCheckBox("输出Debug信息");
		cbDebug.setEnabled(true);
		cbDebug.setBounds(6, 76, 225, 23);
		panel_1.add(cbDebug);
		
		cbTreeFrame = new JCheckBox("每次显示Tree Frame");
		cbTreeFrame.setEnabled(true);
		cbTreeFrame.setBounds(241, 76, 210, 23);
		panel_1.add(cbTreeFrame);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.setLayout(null);
		panel_2.setBounds(478, 10, 193, 374);
		frmDcopsolver.getContentPane().add(panel_2);
		
		JLabel label_8 = new JLabel("Total Time：");
		label_8.setBounds(10, 10, 80, 15);
		panel_2.add(label_8);
		
		tfTotalTime = new JTextField();
		tfTotalTime.setColumns(10);
		tfTotalTime.setBounds(100, 7, 83, 21);
		panel_2.add(tfTotalTime);
		
		JLabel label_9 = new JLabel("Total Cost：");
		label_9.setBounds(10, 47, 80, 15);
		panel_2.add(label_9);
		
		tfTotalCost = new JTextField();
		tfTotalCost.setColumns(10);
		tfTotalCost.setBounds(100, 44, 83, 21);
		panel_2.add(tfTotalCost);
		
		epResultDetails = new JEditorPane();
		epResultDetails.setBounds(10, 72, 173, 292);
		panel_2.add(epResultDetails);
	}
	
	private boolean isBatch()
	{
		return new File(tfProblemPath.getText().trim()).isDirectory();
	}
	
	private void enableBatch(boolean enable)
	{
		cbDebug.setEnabled(!enable);
		cbTreeFrame.setEnabled(!enable);
		labelRunProgress.setEnabled(enable);
		spinnerRunTimes.setEnabled(enable);
	}
	
	private void enableMainPanel(boolean enable)
	{
		tfProblemPath.setEnabled(enable);
		combAlgorithmType.setEnabled(enable);
		spinnerRunTimes.setEnabled(enable);
		labelRunProgress.setEnabled(enable);
	}
	
	private void enableSettingPanel(boolean enable)
	{
		spinnerMessageTransmissionTime.setEnabled(enable);
		spinnerMessageTransmissionNCCC.setEnabled(enable);
		spinnerBnbLayer.setEnabled(enable);
		cbGraphFrame.setEnabled(enable);
		cbDebug.setEnabled(enable);
		cbTreeFrame.setEnabled(enable);
	}
	
	private void enableStatisticsPanel(boolean enable)
	{
		tfTotalTime.setEnabled(enable);
		tfTotalCost.setEnabled(enable);
		epResultDetails.setEnabled(enable);
	}
	
	/*private void enableUI(boolean enable, boolean batch)
	{
		menuBar.setEnabled(enable);
		
		tfProblemPath.setEnabled(enable);
		combAlgorithmType.setEnabled(enable);
		
		combobProblem.setEnabled(enable);
		combobAgentType.setEnabled(enable);
		cbDebug.setEnabled(enable);
		cbTreeFrame.setEnabled(enable);
		btnSolve.setEnabled(enable);
		cbBatch.setEnabled(enable);
		tfDirPath.setEditable(enable);
		labelBatCounter.setEnabled(enable);
		spinnerRepeatTimes.setEnabled(enable);
		if(enable==true)
		{
			enableBatch(cbBatch.isSelected());
		}else
		{
			if(cbBatch.isSelected()==true)
			{
				labelBatCounter.setEnabled(true);
			}
		}
		
		lbRunningFlag.setVisible(!enable);
	}
	
	private void solve()
	{
		this.enableUI(false);
		
		Solver solver=new Solver();
		EventListener el=new EventListener() {
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinished(Object result) {
				// TODO Auto-generated method stub
				enableUI(true);
			}
		};

		if(this.cbBatch.isSelected()==false)
		{
			String selectedProblem=combobProblem.getSelectedItem()+"";
			if(combobProblem.getSelectedIndex()<(combobProblem.getItemCount()-1))
			{
				selectedProblem="problems/"+selectedProblem;
			}
			solver.solve(selectedProblem, (String) combobAgentType.getSelectedItem(), 
					cbTreeFrame.isSelected(), cbDebug.isSelected(), el); 
		}else
		{
			String problemDir=tfDirPath.getText();
			if(problemDir.isEmpty()==true)
			{
				return;
			}
			solver.batSolve(problemDir, (String) combobAgentType.getSelectedItem(), (Integer)spinnerRepeatTimes.getValue(), el, new Solver.BatSolveListener(){

						@Override
						public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
							// TODO Auto-generated method stub
							labelBatCounter.setText((timeIndex+1)+"/"+(problemIndex+1)+"/"+problemTotalCount);
						}
			});
		}
	}*/
}
