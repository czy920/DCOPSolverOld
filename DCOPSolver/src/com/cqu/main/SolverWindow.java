package com.cqu.main;

import java.awt.Dimension;
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

import com.cqu.algorithmconfiguration.LabelSpinnerParameter;
import com.cqu.core.AgentManager;
import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultAdopt;
import com.cqu.core.ResultDPOP;
import com.cqu.core.Solver;
import com.cqu.heuristics.DFSHeuristicsManager;
import com.cqu.problemgenerator.DialogGraphColoring;
import com.cqu.problemgenerator.DialogMeetingScheduling;
import com.cqu.problemgenerator.DialogRandomDCOP;
import com.cqu.problemgenerator.DialogSensorNetwork;
import com.cqu.settings.DialogSetSettings;
import com.cqu.settings.Settings;
import com.cqu.util.DateUtil;
import com.cqu.util.DialogUtil;
import com.cqu.util.FormatUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;

public class SolverWindow {
	
	private static final String INIT_PROBLEM_PATH="problems/";
	//private static final String INIT_PROBLEM_PATH="F:/hc/meeting-15-3-3/";

	private JFrame frmDcopsolver;
	
	private JMenuBar menuBar;
	
	private JTextField tfProblemPath;
	private JComboBox<String> combAlgorithmType;
	private JComboBox<String> combHeristicType;
	private JComboBox<String> combHeristicNextType;
	private JSpinner spinnerRepeatTimes;
	private JLabel labelRunProgress;
	private JLabel labelFlagRunning;
	private JButton btnOpen;
	
	private LabelSpinnerParameter lspSpinnerMessageTransmissionTime;
	private LabelSpinnerParameter lspSpinnerMessageTransmissionNCCC;
	private LabelSpinnerParameter lspSpinnerBnbLayer;
	private LabelSpinnerParameter lspSpinnerMaxDimensionsInMBDPOP;
	private LabelSpinnerParameter lspSpinnerADOPT_K;
	private LabelSpinnerParameter lspSpinnerCycleCountEnd;
	private LabelSpinnerParameter lspSpinnerSelectProbability;
	private LabelSpinnerParameter lspSpinnerSelectNewProbability;
	private LabelSpinnerParameter lspSpinnerSelectProbabilityA;
	private LabelSpinnerParameter lspSpinnerSelectProbabilityB;
	private LabelSpinnerParameter lspSpinnerSelectProbabilityC;
	private LabelSpinnerParameter lspSpinnerSelectProbabilityD;
	private LabelSpinnerParameter lspSpinnerSelectInterval;
	private LabelSpinnerParameter lspSpinnerSelectStepK1;
	private LabelSpinnerParameter lspSpinnerSelectStepK2;
	private LabelSpinnerParameter lspSpinnerSelectRound;
	
	//蚁群算法参数设置
	//private LabelSpinnerParameter lspSpinnerMaxCycle;
	private LabelSpinnerParameter lspSpinnercountAnt;
	private LabelSpinnerParameter lspSpinneralpha;
	private LabelSpinnerParameter lspSpinnerbeta;
	private LabelSpinnerParameter lspSpinnerrho;
	private LabelSpinnerParameter lspSpinnerMin_tau;
	private LabelSpinnerParameter lspSpinnerMax_tau;
	
	//BFSDPOP移簇方式选择
	private LabelSpinnerParameter lspSpinnerClusterRemovingChoice;
	
	private List<LabelSpinnerParameter> paramList;
	
	private JCheckBox cbGraphFrame;
	private JCheckBox cbDebug;
	private JCheckBox cbTreeFrame;
	private JEditorPane epResultDetails;
	private JPanel panelAlgorithmParamSetting;
	private JPanel panelGraphDisplaySetting;
	
	private JCheckBox cbTotalCost;
	private JCheckBox cbValues;
	private JCheckBox cbRunningTime;
	private JCheckBox cbMessageQuantity;
	private JCheckBox cbLostRatio;
	private JCheckBox cbNCCC;
	private JCheckBox cbMessageSize;
	private JCheckBox cbCycle;
	
	private JTextArea epConsoleLines;
	private ConsoleRedirectThread consoleRedirectThread;
	private String consoleOutput="";
	private int consoleOutputLineCount=0;
	private static final int MAX_CONSOLE_LINE_COUNT=100;
	private PrintStream printStream;
	

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
		
		consoleRedirectThread=new ConsoleRedirectThread(new ConsoleRedirectThread.NewLineListener() {
			
			@Override
			public void newLineAvailable(final String newLine) {
				// TODO Auto-generated method stub
				EventQueue.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						consoleOutputLineCount++;
						consoleOutput+=newLine;
						if(consoleOutputLineCount>MAX_CONSOLE_LINE_COUNT)
						{
							consoleOutputLineCount=MAX_CONSOLE_LINE_COUNT;
							consoleOutput=consoleOutput.substring(consoleOutput.indexOf('\n')+1, consoleOutput.length());
						}
						epConsoleLines.setText(consoleOutput);
					}
				});
			}
		});
		printStream=new PrintStream(consoleRedirectThread.getOut(), true);
		System.setOut(printStream);
		System.setErr(printStream);
		consoleRedirectThread.startProcess();
		System.out.println("DCOPSolver starts working...");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDcopsolver = new JFrame();
		frmDcopsolver.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				/*if(consoleRedirectThread!=null)
				{
					consoleRedirectThread.stopRunning();
				}*/
			}
		});
		frmDcopsolver.setResizable(false);
		frmDcopsolver.setTitle("DCOPSolver");
		frmDcopsolver.setBounds(100, 100, 737, 621);
		frmDcopsolver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		frmDcopsolver.setJMenuBar(menuBar);
		
		JMenu mnf = new JMenu("文件(F)");
		mnf.setMnemonic('F');
		menuBar.add(mnf);
		
		JMenuItem miOpen = new JMenuItem("打开");
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProblemFile();
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
				try {
					solve();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnr.add(miRun);
		
		JMenu mnp = new JMenu("问题(P)");
		mnp.setMnemonic('P');
		menuBar.add(mnp);
		
		JMenuItem miSensorNetwork = new JMenuItem("传感器网络");
		miSensorNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DialogSensorNetwork dlg=new DialogSensorNetwork();
				dlg.setVisible(true);
			}
		});
		mnp.add(miSensorNetwork);
		
		JMenuItem miMeetingScheduling = new JMenuItem("会议调度");
		miMeetingScheduling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DialogMeetingScheduling dlg=new DialogMeetingScheduling();
				dlg.setVisible(true);
			}
		});
		mnp.add(miMeetingScheduling);
		
		JMenuItem miGraphColoring = new JMenuItem("图着色");
		miGraphColoring.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				DialogGraphColoring dlg=new DialogGraphColoring();
				dlg.setVisible(true);
			}
		});
		mnp.add(miGraphColoring);
		
		JMenuItem miRandomDCOP = new JMenuItem("RandomDCOP");
		miRandomDCOP.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				DialogRandomDCOP dlg=new DialogRandomDCOP();
				dlg.setVisible(true);
			}
		});
		mnp.add(miRandomDCOP);
		
		JMenu mns = new JMenu("设置(S)");
		mns.setMnemonic('S');
		menuBar.add(mns);
		
		JMenuItem miSetSettings = new JMenuItem("设置");
		miSetSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DialogSetSettings dlg=new DialogSetSettings();
				dlg.setVisible(true);
			}
		});
		mns.add(miSetSettings);
		
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
		
		JPanel panelRunningSetting = new JPanel();
		panelRunningSetting.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelRunningSetting.setLayout(null);
		panelRunningSetting.setBounds(10, 35, 459, 132);
		frmDcopsolver.getContentPane().add(panelRunningSetting);
		
		JLabel label = new JLabel("问题：");
		label.setBounds(10, 13, 45, 15);
		panelRunningSetting.add(label);
		
		tfProblemPath = new JTextField();
		tfProblemPath.setColumns(10);
		tfProblemPath.setBounds(64, 10, 314, 21);
		panelRunningSetting.add(tfProblemPath);
		
		JLabel label_1 = new JLabel("算法：");
		label_1.setBounds(10, 47, 45, 15);
		panelRunningSetting.add(label_1);
		
		combAlgorithmType = new JComboBox<String>();
		combAlgorithmType.setBounds(64, 41, 385, 32);
		panelRunningSetting.add(combAlgorithmType);
		
		String[] agentTypes=AgentManager.AGENT_TYPES;
		for(int i=0;i<agentTypes.length;i++)
		{
			combAlgorithmType.addItem(agentTypes[i]);
		}
		combAlgorithmType.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				if(e.getStateChange()==ItemEvent.SELECTED)
				{
					algorithmTypeChanged((String) e.getItem());
				}
			}
		});
		
		spinnerRepeatTimes = new JSpinner();
		spinnerRepeatTimes.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				String oldText=labelRunProgress.getText();
				labelRunProgress.setText(spinnerRepeatTimes.getValue()+oldText.substring(oldText.indexOf('/')));
			}
		});
		spinnerRepeatTimes.setModel(new SpinnerNumberModel(10, 3, 100, 1));
		spinnerRepeatTimes.setBounds(64, 83, 65, 22);
		panelRunningSetting.add(spinnerRepeatTimes);
		
		labelRunProgress = new JLabel(spinnerRepeatTimes.getValue()+"/0/0");
		labelRunProgress.setBounds(207, 86, 118, 15);
		panelRunningSetting.add(labelRunProgress);
		
		JLabel label_3 = new JLabel("重复：");
		label_3.setBounds(10, 86, 45, 15);
		panelRunningSetting.add(label_3);
		
		labelFlagRunning = new JLabel("");
		labelFlagRunning.setIcon(new ImageIcon("resources/loading.gif"));
		labelFlagRunning.setHorizontalAlignment(SwingConstants.CENTER);
		labelFlagRunning.setBounds(409, 83, 40, 39);
		panelRunningSetting.add(labelFlagRunning);
		
		btnOpen = new JButton("打开");
		btnOpen.setBounds(388, 9, 61, 23);
		btnOpen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openProblemFile();
			}
		});
		panelRunningSetting.add(btnOpen);
		
		panelAlgorithmParamSetting = new JPanel();
		panelAlgorithmParamSetting.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		//panel_1.setBounds(10, 202, 459, 200);
		panelAlgorithmParamSetting.setBounds(10, 270, 459,88);
		frmDcopsolver.getContentPane().add(panelAlgorithmParamSetting);
		panelAlgorithmParamSetting.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		lspSpinnerMessageTransmissionTime=new LabelSpinnerParameter("通信时间：", new SpinnerNumberModel(0, 0, 1000, 10));
		lspSpinnerMessageTransmissionNCCC=new LabelSpinnerParameter("通信NCCC：", new SpinnerNumberModel(0, 0, 1000, 10));
		lspSpinnerBnbLayer=new LabelSpinnerParameter("BNB合并算法分层：", new SpinnerNumberModel(0.5, 0, 1, 0.1));
		lspSpinnerMaxDimensionsInMBDPOP=new LabelSpinnerParameter("维度限制：", new SpinnerNumberModel(8, 3, 20, 1));
		lspSpinnerADOPT_K=new LabelSpinnerParameter("K值：", new SpinnerNumberModel(0, 0, 10000, 100));
		
		//蚁群算法参数设置
		//lspSpinnerMaxCycle = new LabelSpinnerParameter("最大回合数：", new SpinnerNumberModel(100, 20, 300, 10));
		lspSpinnercountAnt = new LabelSpinnerParameter("蚂蚁数量：", new SpinnerNumberModel(10, 2, 50, 5));
		lspSpinneralpha = new LabelSpinnerParameter("alpha：", new SpinnerNumberModel(2, 1, 10, 1));
		lspSpinnerbeta = new LabelSpinnerParameter("beta：", new SpinnerNumberModel(8, 1, 10, 1));
		lspSpinnerrho = new LabelSpinnerParameter("rho：", new SpinnerNumberModel(0.02, 0, 1, 0.01));
		lspSpinnerMax_tau = new LabelSpinnerParameter("max_tau：", new SpinnerNumberModel(10, 10, 15, 0.5));
		lspSpinnerMin_tau = new LabelSpinnerParameter("min_tau：", new SpinnerNumberModel(0.1, 0, 5, 0.5));
		
		//BFSDPOP
		lspSpinnerClusterRemovingChoice=new LabelSpinnerParameter("移簇方式：", new SpinnerNumberModel(0, 0, 1, 1));
		
		lspSpinnerCycleCountEnd=new LabelSpinnerParameter("回合限制：", new SpinnerNumberModel(20, 0, 1000, 1));
		lspSpinnerSelectProbability=new LabelSpinnerParameter("选择概率p：", new SpinnerNumberModel(0.3, 0, 1, 0.1));
		lspSpinnerSelectNewProbability=new LabelSpinnerParameter("选择概率p*：", new SpinnerNumberModel(0.5, 0, 1, 0.1));
		lspSpinnerSelectProbabilityA=new LabelSpinnerParameter("选择概率A：", new SpinnerNumberModel(0.1, 0, 1, 0.1));
		lspSpinnerSelectProbabilityB=new LabelSpinnerParameter("选择概率B：", new SpinnerNumberModel(0.2, 0, 1, 0.1));
		lspSpinnerSelectProbabilityC=new LabelSpinnerParameter("选择概率C：", new SpinnerNumberModel(0.05, 0, 1, 0.1));
		lspSpinnerSelectProbabilityD=new LabelSpinnerParameter("选择概率D：", new SpinnerNumberModel(0.2, 0, 1, 0.1));
		lspSpinnerSelectInterval=new LabelSpinnerParameter("控制间隔：", new SpinnerNumberModel(15, 0, 500, 1));
		lspSpinnerSelectStepK1=new LabelSpinnerParameter("步长k：", new SpinnerNumberModel(5, 0, 50, 1));
		lspSpinnerSelectStepK2=new LabelSpinnerParameter("步长K*：", new SpinnerNumberModel(5, 0, 50, 1));
		lspSpinnerSelectRound=new LabelSpinnerParameter("重启轮数：", new SpinnerNumberModel(20, 0, 600, 1));
		
	/*	
		//选择根结点
		JLabel RootLabel = new JLabel("根结点选择：");
		RootLabel.setBounds(10, 145, 141, 15);
		panel_1.add(RootLabel);
		combHeristicType = new JComboBox<String>();
		combHeristicType.setBounds(10, 165, 165, 25);
		panel_1.add(combHeristicType);
		
		String[] HeuristicTypes=DFSHeuristicsManager.HEURISTICS_TYPES;
		for(int i=0;i<HeuristicTypes.length;i++)
		{
			combHeristicType.addItem(HeuristicTypes[i]);
		}
		
		//选择下一个结点
		JLabel NextLabel = new JLabel("Next结点选择：");
		NextLabel.setBounds(240, 145, 141, 15);
		panel_1.add(NextLabel);
		combHeristicNextType = new JComboBox<String>();
		combHeristicNextType.setBounds(240, 165, 165, 25);
		panel_1.add(combHeristicNextType);
		
		String[] HeuristicNextTypes=DFSHeuristicsManager.HEURISTICS_TYPES;
		for(int i=0;i<HeuristicNextTypes.length;i++)
		{
			combHeristicNextType.addItem(HeuristicNextTypes[i]);
		}*/
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 393, 459, 169);
		frmDcopsolver.getContentPane().add(scrollPane);
		
		epConsoleLines = new JTextArea();
		scrollPane.setViewportView(epConsoleLines);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(479, 202, 238, 360);
		frmDcopsolver.getContentPane().add(scrollPane_1);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		epResultDetails = new JEditorPane();
		scrollPane_1.setViewportView(epResultDetails);
		
		JLabel label_2 = new JLabel("控制台输出");
		label_2.setBounds(10, 368, 760, 15);
		frmDcopsolver.getContentPane().add(label_2);
		
		JLabel label_4 = new JLabel("图形显示设置");
		label_4.setBounds(10, 177, 459, 15);
		frmDcopsolver.getContentPane().add(label_4);
		
		JLabel label_8 = new JLabel("运行结果");
		label_8.setBounds(479, 177, 192, 15);
		frmDcopsolver.getContentPane().add(label_8);
		
		JLabel label_9 = new JLabel("运行设置");
		label_9.setBounds(10, 10, 459, 15);
		frmDcopsolver.getContentPane().add(label_9);
		
		cbDebug = new JCheckBox("输出Debug信息");
		cbDebug.setBounds(107, 364, 141, 23);
		frmDcopsolver.getContentPane().add(cbDebug);
		cbDebug.setEnabled(true);
		
		JLabel label_10 = new JLabel("算法参数设置");
		label_10.setBounds(10, 245, 459, 15);
		frmDcopsolver.getContentPane().add(label_10);
		
		panelGraphDisplaySetting = new JPanel();
		panelGraphDisplaySetting.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout fl_panelGraphDisplaySetting = (FlowLayout) panelGraphDisplaySetting.getLayout();
		fl_panelGraphDisplaySetting.setAlignment(FlowLayout.LEFT);
		panelGraphDisplaySetting.setBounds(10, 202, 459, 33);
		frmDcopsolver.getContentPane().add(panelGraphDisplaySetting);
		
		cbGraphFrame = new JCheckBox("GraphFrame");
		panelGraphDisplaySetting.add(cbGraphFrame);
		cbGraphFrame.setSelected(true);
		
		cbTreeFrame = new JCheckBox("Tree Frame");
		panelGraphDisplaySetting.add(cbTreeFrame);
		cbTreeFrame.setEnabled(true);
		
		JLabel label_5 = new JLabel("输出项");
		label_5.setBounds(479, 10, 192, 15);
		frmDcopsolver.getContentPane().add(label_5);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setBounds(479, 35, 238, 132);
		frmDcopsolver.getContentPane().add(panel);
		
		cbTotalCost = new JCheckBox("TotalCost");
		cbTotalCost.setSelected(true);
		panel.add(cbTotalCost);
		
		cbValues = new JCheckBox("Values");
		cbValues.setSelected(true);
		panel.add(cbValues);
		
		cbRunningTime = new JCheckBox("RunningTime");
		cbRunningTime.setSelected(true);
		panel.add(cbRunningTime);
		
		cbMessageQuantity = new JCheckBox("MessageQuantity");
		cbMessageQuantity.setSelected(true);
		panel.add(cbMessageQuantity);
		
		cbMessageSize = new JCheckBox("MessageSize");
		panel.add(cbMessageSize);
		
		cbLostRatio = new JCheckBox("LostRatio");
		cbLostRatio.setSelected(true);
		panel.add(cbLostRatio);
		
		cbNCCC = new JCheckBox("NCCC");
		cbNCCC.setSelected(true);
		panel.add(cbNCCC);
		
		cbCycle = new JCheckBox("Cycle");
		panel.add(cbCycle);
		
		initStatus();
		setSettingValues();
	}
	
	private void initStatus()
	{
		tfProblemPath.setText(new File(INIT_PROBLEM_PATH).listFiles()[0].getPath());
		setBatch(isBatch());
		labelFlagRunning.setVisible(false);
		
		this.algorithmTypeChanged((String) this.combAlgorithmType.getSelectedItem());
	}
	
	private void algorithmTypeChanged(String newType)
	{
		this.panelAlgorithmParamSetting.removeAll();
		this.panelAlgorithmParamSetting.repaint();
		this.paramList=getCurrentAlgorithmParams(newType);
		for(LabelSpinnerParameter param : this.paramList)
		{
			this.panelAlgorithmParamSetting.add(param);
		}
		this.panelAlgorithmParamSetting.paintAll(this.panelAlgorithmParamSetting.getGraphics());
		this.panelAlgorithmParamSetting.invalidate();
	}
	
	private List<LabelSpinnerParameter> getCurrentAlgorithmParams(String algorithmType)
	{
		List<LabelSpinnerParameter> paramList=new ArrayList<LabelSpinnerParameter>();
		if(algorithmType.equals("DSA_A")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("DSA_B")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("DSA_C")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("DSA_D")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("DSA_E")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("MGM")){
			paramList.add(lspSpinnerCycleCountEnd);
		}
		else if(algorithmType.equals("MGM2")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALSDSA")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALSMGM")){
			paramList.add(lspSpinnerCycleCountEnd);
		}
		else if(algorithmType.equals("ALSMGM2")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
			paramList.add(lspSpinnerSelectRound);
		}
		else if(algorithmType.equals("ALS_DSA")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALS_H1_DSA")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbability);
			paramList.add(lspSpinnerSelectNewProbability);
			paramList.add(lspSpinnerSelectStepK1);
			paramList.add(lspSpinnerSelectStepK2);
			paramList.add(lspSpinnerSelectRound);
		}
		else if(algorithmType.equals("ALS_H2_DSA")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectProbabilityA);
			paramList.add(lspSpinnerSelectProbabilityB);
			paramList.add(lspSpinnerSelectProbabilityC);
			paramList.add(lspSpinnerSelectProbabilityD);
			paramList.add(lspSpinnerSelectRound);
		}
		else if(algorithmType.equals("ALSLMUSDSA4")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectInterval);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALSMLUDSA")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectInterval);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALSDSAMGM")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectInterval);
			paramList.add(lspSpinnerSelectProbability);
		}
		else if(algorithmType.equals("ALSDSAMGMEVO")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectInterval);
			paramList.add(lspSpinnerSelectProbability);
			paramList.add(lspSpinnerSelectNewProbability);
		}
		else if(algorithmType.equals("ALSDSALUC")){
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnerSelectInterval);
			paramList.add(lspSpinnerSelectProbability);
			paramList.add(lspSpinnerSelectNewProbability);
		}
		else if(algorithmType.equals("DPOP"))
		{
			paramList.add(lspSpinnerMessageTransmissionTime);
		}else if(algorithmType.equals("BFSDPOP"))
		{
			paramList.add(lspSpinnerMessageTransmissionTime);
			paramList.add(lspSpinnerClusterRemovingChoice);
		}else if(algorithmType.equals("HybridDPOP"))
		{
			paramList.add(lspSpinnerMessageTransmissionTime);
		}else if(algorithmType.equals("HybridMBDPOP"))
		{
			paramList.add(lspSpinnerMessageTransmissionTime);
			paramList.add(lspSpinnerMaxDimensionsInMBDPOP);
		}else if(algorithmType.equals("AgileDPOP"))
		{
			paramList.add(lspSpinnerMessageTransmissionTime);
			paramList.add(lspSpinnerMaxDimensionsInMBDPOP);
		}else if(algorithmType.equals("ADOPT"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
		}else if(algorithmType.equals("BNBADOPT"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
		}else if(algorithmType.equals("ADOPT_K"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
			paramList.add(lspSpinnerADOPT_K);
		}else if(algorithmType.equals("BDADOPT"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
			paramList.add(lspSpinnerBnbLayer);
		}else if(algorithmType.equals("SynAdopt"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
		}else if(algorithmType.equals("SynAdopt2"))
		{
			paramList.add(lspSpinnerMessageTransmissionNCCC);
		}else if(algorithmType.equals("ACO")||algorithmType.equals("ACO_tree")||algorithmType.equals("ACO_bf")||algorithmType.equals("ACO_phase")||
				algorithmType.equals("ACO_line")||algorithmType.equals("ACO_final")){
			//paramList.add(lspSpinnerMaxCycle);
			paramList.add(lspSpinnerCycleCountEnd);
			paramList.add(lspSpinnercountAnt);
			paramList.add(lspSpinneralpha);
			paramList.add(lspSpinnerbeta);
			paramList.add(lspSpinnerrho);
			paramList.add(lspSpinnerMin_tau);
			paramList.add(lspSpinnerMax_tau);
		}
		return paramList;
	}
	
	private void openProblemFile()
	{
		boolean isBatchOld=isBatch();
		String defaultDir=tfProblemPath.getText().trim();
		if(defaultDir.isEmpty()==true)
		{
			defaultDir=INIT_PROBLEM_PATH;
		}else if(isBatchOld==false)
		{
			defaultDir=defaultDir.substring(0, defaultDir.lastIndexOf('\\'));
			//defaultDir=defaultDir.substring(0, defaultDir.lastIndexOf('/'));
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
	
	private boolean isBatch()
	{
		return new File(tfProblemPath.getText().trim()).isDirectory();
	}
	
	private void setBatch(boolean batch)
	{
		cbDebug.setEnabled(!batch);
		setStateGraphDisplaySettingPanel(batch);
		spinnerRepeatTimes.setEnabled(batch);
		labelRunProgress.setEnabled(batch);
	}
	
	private void setStateRunningPanel(boolean running)
	{
		tfProblemPath.setEnabled(!running);
		combAlgorithmType.setEnabled(!running);
		spinnerRepeatTimes.setEnabled(!running);
		labelRunProgress.setEnabled(!running);
		labelFlagRunning.setVisible(running);
		btnOpen.setEnabled(!running);
		
		cbDebug.setEnabled(!running);
	}
	
	private void setStateAlgorithmParamSettingPanel(boolean running)
	{
		for(LabelSpinnerParameter param : this.paramList)
		{
			param.getSpinner().setEnabled(!running);
		}
	}
	
	private void setStateGraphDisplaySettingPanel(boolean running)
	{
		cbGraphFrame.setEnabled(!running);
		cbTreeFrame.setEnabled(!running);
	}
	
	private void setUIState(boolean running)
	{
		boolean batch=isBatch();
		if(running==true)
		{
			setStateRunningPanel(running);
			setStateGraphDisplaySettingPanel(running);
			setStateAlgorithmParamSettingPanel(running);
			
			if(batch==true)
			{
				spinnerRepeatTimes.setEnabled(!running);
				labelRunProgress.setEnabled(running);
			}
		}else
		{
			setStateRunningPanel(running);
			setStateGraphDisplaySettingPanel(running);
			setStateAlgorithmParamSettingPanel(running);
			
			setBatch(batch);
		}
		
	}
	
	private void solve()
	{
		this.setUIState(true);
		this.setSettingValues();
		this.epResultDetails.setText("");
		this.labelFlagRunning.setVisible(true);
		
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
					String detailedResult=DateUtil.currentTime()+"\n";
					if(cbTotalCost.isSelected()==true)
					{
						detailedResult+="TotalCost: "+ret.totalCost+"\n";
					}
					if(cbRunningTime.isSelected()==true)
					{
						detailedResult+="RunningTime: "+ret.totalTime+"ms\n";
					}
					if(cbMessageQuantity.isSelected()==true)
					{
						detailedResult+="MessageQuantity: "+ret.messageQuantity+"\n";
					}
					if(cbLostRatio.isSelected()==true)
					{
						detailedResult+="LostRatio: "+ret.lostRatio+"%"+"\n";
					}
					
					
					if(ret instanceof ResultAdopt)
					{
						if(cbNCCC.isSelected()==true)
						{
							detailedResult+="NCCC: "+((ResultAdopt)ret).nccc+"\n";
						}
					}else if(ret instanceof ResultDPOP)
					{
						if(cbMessageSize.isSelected()==true)
						{
							detailedResult+="UtilMsgSizeMin: "+FormatUtil.formatSize((int)Math.round(((ResultDPOP)ret).utilMsgSizeMin))+"\n";
							detailedResult+="UtilMsgSizeMax: "+FormatUtil.formatSize((int)Math.round(((ResultDPOP)ret).utilMsgSizeMax))+"\n";
							detailedResult+="UtilMsgSizeAvg: "+FormatUtil.formatSize((int)Math.round(((ResultDPOP)ret).utilMsgSizeAvg))+"\n";
						}
					}
					if(cbCycle.isSelected()==true)
					{
						for(String key : ret.otherResults.keySet())
						{
							detailedResult+=key+": "+ret.otherResults.get(key)+"\n";
						}
					}
					if(cbValues.isSelected()==true)
					{
						for(Integer key : ret.agentValues.keySet())
						{
							detailedResult+="Agent "+key+": "+ret.agentValues.get(key)+"\n";
						}
					}
					detailedResult=detailedResult.substring(0, detailedResult.length()-1);
					
					final String resultToShow=detailedResult;
					EventQueue.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							epResultDetails.setText(resultToShow);
						}
					});
				}
				setUIState(false);
			}
		};

		String problemPath=tfProblemPath.getText().trim();
		if(problemPath.isEmpty()==true)
		{
			return;
		}
		if(this.isBatch()==false)
		{
			//solver.solve(problemPath, (String) combAlgorithmType.getSelectedItem(), 
			//		cbTreeFrame.isSelected(), cbDebug.isSelected(), el, (String) combHeristicType.getSelectedItem(), (String) combHeristicNextType.getSelectedItem());
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
		Settings.settings.setCommunicationTimeInDPOPs((Integer)lspSpinnerMessageTransmissionTime.getSpinner().getValue());
		Settings.settings.setCommunicationNCCCInAdopts((Integer)lspSpinnerMessageTransmissionNCCC.getSpinner().getValue());
		Settings.settings.setBNBmergeADOPTboundArg((Double)lspSpinnerBnbLayer.getSpinner().getValue());
		Settings.settings.setDisplayGraphFrame(cbGraphFrame.isSelected());
		Settings.settings.setMaxDimensionsInMBDPOP((Integer)lspSpinnerMaxDimensionsInMBDPOP.getSpinner().getValue());
		Settings.settings.setADOPT_K((Integer)lspSpinnerADOPT_K.getSpinner().getValue());
		
		//蚁群算法参数
		//Settings.settings.setMaxCycle((Integer)lspSpinnerMaxCycle.getSpinner().getValue());
		Settings.settings.setCountAnt((Integer)lspSpinnercountAnt.getSpinner().getValue());
		Settings.settings.setAlpha((Integer)lspSpinneralpha.getSpinner().getValue());
		Settings.settings.setBeta((Integer)lspSpinnerbeta.getSpinner().getValue());
		Settings.settings.setRho((Double)lspSpinnerrho.getSpinner().getValue());
		Settings.settings.setMin_tau((Double)lspSpinnerMin_tau.getSpinner().getValue());
		Settings.settings.setMax_tau((Double)lspSpinnerMax_tau.getSpinner().getValue());
		
		
		//BFSDPOP
		Settings.settings.setClusterRemovingChoice((Integer)lspSpinnerClusterRemovingChoice.getSpinner().getValue());
		
		Settings.settings.setCycleCount((Integer)lspSpinnerCycleCountEnd.getSpinner().getValue());
		Settings.settings.setSelectProbability((Double)lspSpinnerSelectProbability.getSpinner().getValue());
		Settings.settings.setSelectProbabilityA((Double)lspSpinnerSelectProbabilityA.getSpinner().getValue());
		Settings.settings.setSelectProbabilityB((Double)lspSpinnerSelectProbabilityB.getSpinner().getValue());
		Settings.settings.setSelectProbabilityC((Double)lspSpinnerSelectProbabilityC.getSpinner().getValue());
		Settings.settings.setSelectProbabilityD((Double)lspSpinnerSelectProbabilityD.getSpinner().getValue());
		Settings.settings.setSelectNewProbability((Double)lspSpinnerSelectNewProbability.getSpinner().getValue());
		Settings.settings.setSelectInterval((Integer)lspSpinnerSelectInterval.getSpinner().getValue());
		Settings.settings.setSelectStepK1((Integer)lspSpinnerSelectStepK1.getSpinner().getValue());
		Settings.settings.setSelectStepK2((Integer)lspSpinnerSelectStepK2.getSpinner().getValue());
		Settings.settings.setSelectRound((Integer)lspSpinnerSelectRound.getSpinner().getValue());
	}
}
