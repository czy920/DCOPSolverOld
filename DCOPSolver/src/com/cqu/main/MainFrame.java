package com.cqu.main;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.SwingConstants;

import com.cqu.core.AgentManager;
import com.cqu.core.EventListener;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;
import com.cqu.util.DialogUtil;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8603036412745950029L;
	private JPanel contentPane;
	
	private JComboBox combobProblem;
	private JComboBox combobAgentType;
	private JLabel lbRunningFlag;
	private JCheckBox cbDebug;
	private JCheckBox cbTreeFrame;
	private JButton btnSolve;
	private JTextField tfDirPath;
	private JCheckBox cbBatch;
	private JLabel labelBatCounter;
	private JSpinner spinnerRepeatTimes;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setResizable(false);
		setTitle("DCOPSolver");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 299);
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(this, popupMenu);
		
		JMenuItem miSetSettings = new JMenuItem("设置");
		miSetSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.showSettingsDialog();
			}
		});
		popupMenu.add(miSetSettings);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		combobAgentType = new JComboBox();
		combobAgentType.setBounds(73, 52, 311, 32);
		contentPane.add(combobAgentType);
		
		JLabel lblAgenttype = new JLabel("AgentType");
		lblAgenttype.setBounds(10, 61, 65, 15);
		contentPane.add(lblAgenttype);
		
		cbDebug = new JCheckBox("Debug");
		cbDebug.setBounds(73, 99, 64, 23);
		contentPane.add(cbDebug);
		
		cbTreeFrame = new JCheckBox("Tree Frame");
		cbTreeFrame.setBounds(134, 99, 90, 23);
		contentPane.add(cbTreeFrame);
		
		JLabel lblSwitch = new JLabel("Switch");
		lblSwitch.setBounds(10, 103, 65, 15);
		contentPane.add(lblSwitch);
		
		combobProblem = new JComboBox();
		combobProblem.setBounds(73, 10, 311, 32);
		contentPane.add(combobProblem);
		
		JLabel lblProblem = new JLabel("Problem");
		lblProblem.setBounds(10, 19, 65, 15);
		contentPane.add(lblProblem);
		
		btnSolve = new JButton("Solve");
		btnSolve.setBounds(10, 199, 374, 62);
		contentPane.add(btnSolve);
		
		lbRunningFlag = new JLabel("New label");
		lbRunningFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lbRunningFlag.setBounds(276, 82, 54, 56);
		contentPane.add(lbRunningFlag);
		
		//init
		File[] files=new File("problems/").listFiles();
		for(int i=0;i<files.length;i++)
		{
			combobProblem.addItem(files[i].getName());
		}
		combobProblem.addItem("browse...");
		
		combobProblem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String selectedProblem=combobProblem.getSelectedItem()+"";
				if(selectedProblem.equals("browse...")==true)
				{
					String lastItem=combobProblem.getItemAt(combobProblem.getItemCount()-1)+"";
					String defaultDir="";
					if(lastItem.equals("browse...")==false)
					{
						defaultDir=lastItem.substring(0, lastItem.lastIndexOf('\\'));
					}
					File f=DialogUtil.dialogOpenFile(new String[]{".xml"}, "Select A Problem", defaultDir);
					if(f!=null)
					{
						if(lastItem.equals("browse...")==false)
						{
							combobProblem.removeItemAt(combobProblem.getItemCount()-1);
						}
						combobProblem.addItem(f.getPath());
						combobProblem.setSelectedIndex(combobProblem.getItemCount()-1);
					}else
					{
						combobProblem.setSelectedIndex(0);
					}
				}
			}
		});
		
		String[] agentTypes=AgentManager.AGENT_TYPES;
		for(int i=0;i<agentTypes.length;i++)
		{
			combobAgentType.addItem(agentTypes[i]);
		}
		
		btnSolve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					solve();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		lbRunningFlag.setText("");
		lbRunningFlag.setIcon(new ImageIcon("resources/loading.gif"));
		
		cbBatch = new JCheckBox("Batch");
		cbBatch.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(cbBatch.isSelected()==true)
				{
					File f=DialogUtil.dialogOpenDir("Select Direcory", tfDirPath.getText().isEmpty()?"E:/":tfDirPath.getText());
					if(f!=null&&f.isDirectory()==true)
					{
						enableBatch(true);
						tfDirPath.setText(f.getPath());
					}else
					{
						cbBatch.setSelected(false);
					}
				}else
				{
					enableBatch(false);
				}
			}
		});
		cbBatch.setBounds(322, 135, 62, 23);
		contentPane.add(cbBatch);
		
		tfDirPath = new JTextField();
		tfDirPath.setBounds(10, 168, 374, 21);
		contentPane.add(tfDirPath);
		tfDirPath.setColumns(10);
		
		labelBatCounter = new JLabel("0/0/0");
		labelBatCounter.setBounds(194, 139, 95, 15);
		contentPane.add(labelBatCounter);
		
		spinnerRepeatTimes = new JSpinner();
		spinnerRepeatTimes.setModel(new SpinnerNumberModel(5, 3, 10, 1));
		spinnerRepeatTimes.setBounds(73, 136, 65, 22);
		contentPane.add(spinnerRepeatTimes);
		
		JLabel lblTimes = new JLabel("times");
		lblTimes.setBounds(10, 139, 54, 15);
		contentPane.add(lblTimes);
		lbRunningFlag.setVisible(false);
		
		enableBatch(false);
	}
	
	private void enableBatch(boolean enable)
	{
		tfDirPath.setEditable(enable);
		
		combobProblem.setEnabled(!enable);
		if(enable==true)
		{
			cbDebug.setSelected(false);
			cbTreeFrame.setSelected(false);
		}
		cbDebug.setEnabled(!enable);
		cbTreeFrame.setEnabled(!enable);
		labelBatCounter.setEnabled(enable);
		spinnerRepeatTimes.setEnabled(enable);
	}
	
	private void enableUI(boolean enable)
	{
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
	
	private void solve() throws Exception
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
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
