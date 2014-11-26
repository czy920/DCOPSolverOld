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

import com.cqu.core.AgentManager;
import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultAdopt;
import com.cqu.core.ResultDPOP;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;
import com.cqu.util.DialogUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SolverWindow {
	
	private static final String INIT_PROBLEM_PATH="problems/";

	private JFrame frmDcopsolver;
	
	private JMenuBar menuBar;
	
	private JTextField tfProblemPath;
	private JComboBox combAlgorithmType;
	private JSpinner spinnerRepeatTimes;
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
	
	private Map<String, Boolean> componentStatus;
	

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
				boolean isBatchOld=isBatch();
				String defaultDir=tfProblemPath.getText().trim();
				if(defaultDir.isEmpty()==true)
				{
					defaultDir=INIT_PROBLEM_PATH;
				}else if(isBatchOld==false)
				{
					defaultDir=defaultDir.substring(0, defaultDir.lastIndexOf('\\'));
				}
				
				File f=DialogUtil.dialogOpenFile(new String[]{".xml"}, "Select A Problem", defaultDir);
				if(f!=null)
				{
					tfProblemPath.setText(f.getPath());
					if(isBatchOld==true)
					{
						setBatch(false);
					}
				}
			}
		});
		mnf.add(miOpen);
		
		JMenuItem miOpenDir = new JMenuItem("打开目录");
		miOpenDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isBatchOld=isBatch();
				String defaultDir=tfProblemPath.getText().trim();
				if(defaultDir.isEmpty()==true)
				{
					defaultDir=INIT_PROBLEM_PATH;
				}else if(isBatchOld==false)
				{
					defaultDir=defaultDir.substring(0, defaultDir.lastIndexOf('\\'));
				}
				
				File f=DialogUtil.dialogOpenDir("Select Direcory", defaultDir);
				if(f!=null&&f.isDirectory()==true)
				{
					tfProblemPath.setText(f.getPath());
					if(isBatchOld==false)
					{
						setBatch(true);
					}
					labelRunProgress.setText(((Integer)spinnerRepeatTimes.getValue())+"/0/"+f.list(new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String name) {
							// TODO Auto-generated method stub
							return name.endsWith(".xml");
						}
					}).length);
				}
			}
		});
		mnf.add(miOpenDir);
		
		JMenu mnr = new JMenu("运行(R)");
		mnr.setMnemonic('R');
		menuBar.add(mnr);
		
		JMenuItem miRun = new JMenuItem("运行");
		miRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				solve();
			}
		});
		mnr.add(miRun);
		
		JMenu mnh = new JMenu("帮助(H)");
		mnh.setMnemonic('H');
		menuBar.add(mnh);
		
		JMenuItem miAbout = new JMenuItem("关于");
		miAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new DialogAbout().setVisible(true);
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
		
		String[] agentTypes=AgentManager.AGENT_TYPES;
		for(int i=0;i<agentTypes.length;i++)
		{
			combAlgorithmType.addItem(agentTypes[i]);
		}
		
		spinnerRepeatTimes = new JSpinner();
		spinnerRepeatTimes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				String oldText=labelRunProgress.getText();
				labelRunProgress.setText(spinnerRepeatTimes.getValue()+oldText.substring(oldText.indexOf('/')));
			}
		});
		spinnerRepeatTimes.setModel(new SpinnerNumberModel(5, 3, 10, 1));
		spinnerRepeatTimes.setBounds(64, 83, 65, 22);
		panel.add(spinnerRepeatTimes);
		
		labelRunProgress = new JLabel(spinnerRepeatTimes.getValue()+"/0/0");
		labelRunProgress.setBounds(207, 86, 118, 15);
		panel.add(labelRunProgress);
		
		JLabel label_3 = new JLabel("遍数：");
		label_3.setBounds(10, 86, 45, 15);
		panel.add(label_3);
		
		labelFlagRunning = new JLabel("");
		labelFlagRunning.setIcon(new ImageIcon("E:\\hz\\java\\workspace\\DCOPSolver\\DCOPSolver\\resources\\loading.gif"));
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
		spinnerBnbLayer.setModel(new SpinnerNumberModel(2, 2, 10, 1));
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
		
		initStatus();
		setSettingValues();
	}
	
	private void initStatus()
	{
		componentStatus=new HashMap<String, Boolean>();
		
		componentStatus.put("tfProblemPath", tfProblemPath.isEnabled());
		componentStatus.put("combAlgorithmType", combAlgorithmType.isEnabled());
		componentStatus.put("spinnerRepeatTimes", spinnerRepeatTimes.isEnabled());
		componentStatus.put("labelRunProgress", labelRunProgress.isEnabled());
		componentStatus.put("labelFlagRunning", labelFlagRunning.isEnabled());
		
		componentStatus.put("spinnerMessageTransmissionTime", spinnerMessageTransmissionTime.isEnabled());
		componentStatus.put("spinnerMessageTransmissionNCCC", spinnerMessageTransmissionNCCC.isEnabled());
		componentStatus.put("spinnerBnbLayer", spinnerBnbLayer.isEnabled());
		componentStatus.put("cbGraphFrame", cbGraphFrame.isEnabled());
		componentStatus.put("cbDebug", cbDebug.isEnabled());
		componentStatus.put("cbTreeFrame", cbTreeFrame.isEnabled());
		
		tfProblemPath.setText(new File(INIT_PROBLEM_PATH).listFiles()[0].getPath());
		setBatch(isBatch());
		labelFlagRunning.setVisible(false);
	}
	
	private boolean isBatch()
	{
		return new File(tfProblemPath.getText().trim()).isDirectory();
	}
	
	private void setBatch(boolean batch)
	{
		cbDebug.setEnabled(!batch);
		cbTreeFrame.setEnabled(!batch);
		cbGraphFrame.setEnabled(!batch);
		spinnerRepeatTimes.setEnabled(batch);
		labelRunProgress.setEnabled(batch);
		
		componentStatus.put("cbDebug", cbDebug.isEnabled());
		componentStatus.put("cbTreeFrame", cbTreeFrame.isEnabled());
		componentStatus.put("cbGraphFrame", cbGraphFrame.isEnabled());
		componentStatus.put("spinnerRepeatTimes", spinnerRepeatTimes.isEnabled());
		componentStatus.put("labelRunProgress", labelRunProgress.isEnabled());
	}
	
	private void enableMainPanel(boolean enable)
	{
		tfProblemPath.setEnabled(enable);
		combAlgorithmType.setEnabled(enable);
		spinnerRepeatTimes.setEnabled(enable);
		if(isBatch()==true)
		{
			labelRunProgress.setEnabled(!enable);
		}else
		{
			labelRunProgress.setEnabled(enable);
		}
		labelFlagRunning.setVisible(!enable);
	}
	
	private void resumeMainPanel()
	{
		tfProblemPath.setEnabled(componentStatus.get("tfProblemPath"));
		combAlgorithmType.setEnabled(componentStatus.get("combAlgorithmType"));
		spinnerRepeatTimes.setEnabled(componentStatus.get("spinnerRepeatTimes"));
		labelRunProgress.setEnabled(componentStatus.get("labelRunProgress"));
		labelFlagRunning.setVisible(!componentStatus.get("labelFlagRunning"));
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
	
	private void resumeSettingPanel()
	{
		spinnerMessageTransmissionTime.setEnabled(componentStatus.get("spinnerMessageTransmissionTime"));
		spinnerMessageTransmissionNCCC.setEnabled(componentStatus.get("spinnerMessageTransmissionNCCC"));
		spinnerBnbLayer.setEnabled(componentStatus.get("spinnerBnbLayer"));
		cbGraphFrame.setEnabled(componentStatus.get("cbGraphFrame"));
		cbDebug.setEnabled(componentStatus.get("cbDebug"));
		cbTreeFrame.setEnabled(componentStatus.get("cbTreeFrame"));
	}
	
	private void enableUI(boolean enable)
	{
		menuBar.setEnabled(enable);
		if(enable==false)
		{
			enableMainPanel(false);
			enableSettingPanel(false);
		}else
		{
			resumeMainPanel();
			resumeSettingPanel();
		}
	}
	
	private void solve()
	{
		this.enableUI(false);
		this.setSettingValues();
		
		Solver solver=new Solver();
		EventListener el=new EventListener() {
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinished(Object result) {
				// TODO Auto-generated method stub
				Result ret=(Result) result;
				if(ret!=null)
				{
					tfTotalCost.setText(ret.totalCost+"");
					tfTotalTime.setText(ret.totalTime+"");
					
					String detailedResult="messageQuantity: "+ret.messageQuantity+"\n";
					detailedResult+="lostRatio: "+ret.lostRatio+"%"+"\n";
					if(ret instanceof ResultAdopt)
					{
						detailedResult+="NCCC: "+((ResultAdopt)ret).nccc;
					}else if(ret instanceof ResultDPOP)
					{
						detailedResult+="utilMsgSizeMin: "+((ResultDPOP)ret).utilMsgSizeMin+"\n";
						detailedResult+="utilMsgSizeMax: "+((ResultDPOP)ret).utilMsgSizeMax+"\n";
						detailedResult+="utilMsgSizeAvg: "+((ResultDPOP)ret).utilMsgSizeAvg;
					}
					epResultDetails.setText(detailedResult);
				}
				enableUI(true);
			}
		};

		String problemPath=tfProblemPath.getText().trim();
		if(problemPath.isEmpty()==true)
		{
			return;
		}
		if(this.isBatch()==false)
		{
			solver.solve(problemPath, (String) combAlgorithmType.getSelectedItem(), 
					cbTreeFrame.isSelected(), cbDebug.isSelected(), el); 
		}else
		{
			solver.batSolve(problemPath, (String) combAlgorithmType.getSelectedItem(), (Integer)spinnerRepeatTimes.getValue(), el, new Solver.BatSolveListener(){

						@Override
						public void progressChanged(int problemTotalCount, int problemIndex, int timeIndex) {
							// TODO Auto-generated method stub
							labelRunProgress.setText((timeIndex+1)+"/"+(problemIndex+1)+"/"+problemTotalCount);
						}
			});
		}
	}
	
	private void setSettingValues()
	{
		Settings.settings.setCommunicationTimeInDPOPs((Integer)spinnerMessageTransmissionTime.getValue());
		Settings.settings.setCommunicationNCCCInAdopts((Integer)spinnerMessageTransmissionNCCC.getValue());
		Settings.settings.setBNBmergeADOPTboundArg((Integer)spinnerBnbLayer.getValue());
		Settings.settings.setDisplayGraphFrame(cbGraphFrame.isSelected());
	}
}
