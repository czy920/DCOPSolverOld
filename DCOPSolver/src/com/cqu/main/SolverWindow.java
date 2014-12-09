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
import com.cqu.util.DateUtil;
import com.cqu.util.DialogUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.HashMap;
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

public class SolverWindow {
	
	private static final String INIT_PROBLEM_PATH="problems/";

	private JFrame frmDcopsolver;
	
	private JMenuBar menuBar;
	
	private JTextField tfProblemPath;
	private JComboBox combAlgorithmType;
	private JSpinner spinnerRepeatTimes;
	private JLabel labelRunProgress;
	private JLabel labelFlagRunning;
	private JButton btnOpen;
	
	private JSpinner spinnerMessageTransmissionTime;
	private JSpinner spinnerMessageTransmissionNCCC;
	private JSpinner spinnerADOPT_K;
	private JSpinner spinnerBnbLayer;
	private JCheckBox cbGraphFrame;
	private JCheckBox cbDebug;
	private JCheckBox cbTreeFrame;
	private JEditorPane epResultDetails;
	private JSpinner spinnerMaxDimensionsInAgileDPOP;
	
	private JTextArea epConsoleLines;
	private ConsoleRedirectThread consoleRedirectThread;
	private String consoleOutput="";
	private int consoleOutputLineCount=0;
	private static final int MAX_CONSOLE_LINE_COUNT=100;
	private PrintStream printStream;
	
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
		consoleRedirectThread.start();
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
		panel.setBounds(10, 35, 459, 132);
		frmDcopsolver.getContentPane().add(panel);
		
		JLabel label = new JLabel("路径：");
		label.setBounds(10, 13, 45, 15);
		panel.add(label);
		
		tfProblemPath = new JTextField();
		tfProblemPath.setColumns(10);
		tfProblemPath.setBounds(64, 10, 314, 21);
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
		labelFlagRunning.setIcon(new ImageIcon("resources/loading.gif"));
		labelFlagRunning.setHorizontalAlignment(SwingConstants.CENTER);
		labelFlagRunning.setBounds(409, 83, 40, 39);
		panel.add(labelFlagRunning);
		
		btnOpen = new JButton("打开");
		btnOpen.setBounds(388, 9, 61, 23);
		btnOpen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openProblemFile();
			}
		});
		panel.add(btnOpen);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(10, 152, 459, 130);

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
		label_6.setBounds(241, 13, 130, 15);
		panel_1.add(label_6);
		
		spinnerMessageTransmissionNCCC = new JSpinner();
		spinnerMessageTransmissionNCCC.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spinnerMessageTransmissionNCCC.setBounds(381, 10, 70, 22);
		panel_1.add(spinnerMessageTransmissionNCCC);
		
		JLabel label_7 = new JLabel("BNB合并算法分层：");
		label_7.setBounds(10, 45, 141, 15);
		panel_1.add(label_7);
		
		spinnerBnbLayer = new JSpinner();
		spinnerBnbLayer.setModel(new SpinnerNumberModel(0.5, 0, 1, 0.1));
		spinnerBnbLayer.setBounds(161, 42, 70, 22);
		panel_1.add(spinnerBnbLayer);
		
		JLabel adoptk = new JLabel("ADOPT_K算法的K：");
		adoptk.setBounds(241, 45, 130, 15);
		panel_1.add(adoptk);
		
		spinnerADOPT_K = new JSpinner();
		spinnerADOPT_K.setModel(new SpinnerNumberModel(0, 0, 5000, 1));
		spinnerADOPT_K.setBounds(381, 42, 70, 22);
		panel_1.add(spinnerADOPT_K);
		
		cbGraphFrame = new JCheckBox("每次显示GraphFrame");
		cbGraphFrame.setSelected(true);
		cbGraphFrame.setBounds(10, 75, 230, 15);
		panel_1.add(cbGraphFrame);
		
		cbTreeFrame = new JCheckBox("每次显示Tree Frame");
		cbTreeFrame.setEnabled(true);
		cbTreeFrame.setBounds(241, 75, 230, 15);
		panel_1.add(cbTreeFrame);
		
		cbDebug = new JCheckBox("输出Debug信息");
		cbDebug.setEnabled(true);
		cbDebug.setBounds(10, 105, 225, 15);
		panel_1.add(cbDebug);
		
		
		JLabel lblNewLabel = new JLabel("AgileDPOP维度限制：");
		lblNewLabel.setBounds(10, 116, 141, 15);
		panel_1.add(lblNewLabel);
		
		spinnerMaxDimensionsInAgileDPOP = new JSpinner();
		spinnerMaxDimensionsInAgileDPOP.setModel(new SpinnerNumberModel(3, 2, 10, 1));
		spinnerMaxDimensionsInAgileDPOP.setBounds(161, 113, 70, 22);
		panel_1.add(spinnerMaxDimensionsInAgileDPOP);
		
		JLabel lblAdoptkk = new JLabel("ADOPT_k中k值：");
		lblAdoptkk.setBounds(241, 116, 141, 15);
		panel_1.add(lblAdoptkk);
		
		spinnerADOPT_K = new JSpinner();
		spinnerADOPT_K.setBounds(381, 113, 70, 22);
		panel_1.add(spinnerADOPT_K);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 283, 459, 142);
		frmDcopsolver.getContentPane().add(scrollPane);
		
		epConsoleLines = new JTextArea();
		scrollPane.setViewportView(epConsoleLines);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(479, 35, 238, 527);
		frmDcopsolver.getContentPane().add(scrollPane_1);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		epResultDetails = new JEditorPane();
		scrollPane_1.setViewportView(epResultDetails);
		
		JLabel label_2 = new JLabel("控制台输出");
		label_2.setBounds(10, 368, 760, 15);
		frmDcopsolver.getContentPane().add(label_2);
		
		JLabel label_4 = new JLabel("算法参数设置");
		label_4.setBounds(10, 177, 459, 15);
		frmDcopsolver.getContentPane().add(label_4);
		
		JLabel label_8 = new JLabel("运行结果");
		label_8.setBounds(479, 10, 192, 15);
		frmDcopsolver.getContentPane().add(label_8);
		
		JLabel label_9 = new JLabel("运行设置");
		label_9.setBounds(10, 10, 459, 15);
		frmDcopsolver.getContentPane().add(label_9);
		
		initStatus();
		setSettingValues();
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
	
	private void initStatus()
	{
		componentStatus=new HashMap<String, Boolean>();
		
		componentStatus.put("tfProblemPath", tfProblemPath.isEnabled());
		componentStatus.put("combAlgorithmType", combAlgorithmType.isEnabled());
		componentStatus.put("spinnerRepeatTimes", spinnerRepeatTimes.isEnabled());
		componentStatus.put("labelRunProgress", labelRunProgress.isEnabled());
		componentStatus.put("btnOpen", btnOpen.isEnabled());
		
		componentStatus.put("spinnerMessageTransmissionTime", spinnerMessageTransmissionTime.isEnabled());
		componentStatus.put("spinnerMessageTransmissionNCCC", spinnerMessageTransmissionNCCC.isEnabled());
		componentStatus.put("spinnerBnbLayer", spinnerBnbLayer.isEnabled());
		componentStatus.put("spinnerADOPT_K",spinnerADOPT_K.isEnabled());
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
		btnOpen.setEnabled(enable);
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
		labelFlagRunning.setVisible(false);
		btnOpen.setEnabled(componentStatus.get("btnOpen"));
	}
	
	private void enableSettingPanel(boolean enable)
	{
		spinnerMessageTransmissionTime.setEnabled(enable);
		spinnerMessageTransmissionNCCC.setEnabled(enable);
		spinnerBnbLayer.setEnabled(enable);
		spinnerADOPT_K.setEnabled(enable);
		cbGraphFrame.setEnabled(enable);
		cbDebug.setEnabled(enable);
		cbTreeFrame.setEnabled(enable);
		spinnerMaxDimensionsInAgileDPOP.setEnabled(enable);
		spinnerADOPT_K.setEnabled(enable);
	}
	
	private void resumeSettingPanel()
	{
		spinnerMessageTransmissionTime.setEnabled(componentStatus.get("spinnerMessageTransmissionTime"));
		spinnerMessageTransmissionNCCC.setEnabled(componentStatus.get("spinnerMessageTransmissionNCCC"));
		spinnerBnbLayer.setEnabled(componentStatus.get("spinnerBnbLayer"));
		spinnerADOPT_K.setEnabled(componentStatus.get("spinnerADOPT_K"));
		cbGraphFrame.setEnabled(componentStatus.get("cbGraphFrame"));
		cbDebug.setEnabled(componentStatus.get("cbDebug"));
		cbTreeFrame.setEnabled(componentStatus.get("cbTreeFrame"));
		spinnerMaxDimensionsInAgileDPOP.setEnabled(true);
		spinnerADOPT_K.setEnabled(true);
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
					detailedResult+="totalCost: "+ret.totalCost+"\n";
					detailedResult+="totalTime: "+ret.totalTime+"ms\n";
					detailedResult+="messageQuantity: "+ret.messageQuantity+"\n";
					detailedResult+="lostRatio: "+ret.lostRatio+"%"+"\n";
					if(ret instanceof ResultAdopt)
					{
						detailedResult+="NCCC: "+((ResultAdopt)ret).nccc+"\n";
					}else if(ret instanceof ResultDPOP)
					{
						detailedResult+="utilMsgSizeMin: "+((ResultDPOP)ret).utilMsgSizeMin+"\n";
						detailedResult+="utilMsgSizeMax: "+((ResultDPOP)ret).utilMsgSizeMax+"\n";
						detailedResult+="utilMsgSizeAvg: "+((ResultDPOP)ret).utilMsgSizeAvg+"\n";
					}
					for(Integer key : ret.agentValues.keySet())
					{
						detailedResult+="agent "+key+": "+ret.agentValues.get(key)+"\n";
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
		Settings.settings.setBNBmergeADOPTboundArg((Double)spinnerBnbLayer.getValue());
		Settings.settings.setADOPT_K((Integer)spinnerADOPT_K.getValue());
		Settings.settings.setDisplayGraphFrame(cbGraphFrame.isSelected());
		Settings.settings.setMaxDimensionsInAgileDPOP((Integer)spinnerMaxDimensionsInAgileDPOP.getValue());
		Settings.settings.setADOPT_K((Integer) spinnerADOPT_K.getValue());
	}
}
