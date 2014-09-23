package com.cqu.test;

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
import com.cqu.core.DFSTree;
import com.cqu.core.EventListener;
import com.cqu.core.MessageMailer;
import com.cqu.core.Problem;
import com.cqu.core.ProblemParser;
import com.cqu.core.TreeGenerator;
import com.cqu.synchronousqueue.AgentManagerSynchronous;
import com.cqu.synchronousqueue.MessageMailerSynchronous;
import com.cqu.visualtree.TreeFrame;

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
		setTitle("DPOPSolver");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 365, 290);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		combobAgentType = new JComboBox();
		combobAgentType.setBounds(100, 74, 236, 32);
		contentPane.add(combobAgentType);
		
		JLabel lblAgenttype = new JLabel("AgentType");
		lblAgenttype.setBounds(25, 83, 65, 15);
		contentPane.add(lblAgenttype);
		
		cbDebug = new JCheckBox("Debug");
		cbDebug.setBounds(100, 135, 64, 23);
		contentPane.add(cbDebug);
		
		cbTreeFrame = new JCheckBox("Tree Frame");
		cbTreeFrame.setSelected(true);
		cbTreeFrame.setBounds(166, 135, 98, 23);
		contentPane.add(cbTreeFrame);
		
		JLabel lblSwitch = new JLabel("Switch");
		lblSwitch.setBounds(25, 139, 65, 15);
		contentPane.add(lblSwitch);
		
		combobProblem = new JComboBox();
		combobProblem.setBounds(100, 17, 236, 32);
		contentPane.add(combobProblem);
		
		JLabel lblProblem = new JLabel("Problem");
		lblProblem.setBounds(25, 26, 65, 15);
		contentPane.add(lblProblem);
		
		btnSolve = new JButton("Solve");
		btnSolve.setBounds(25, 182, 311, 55);
		contentPane.add(btnSolve);
		
		lbRunningFlag = new JLabel("New label");
		lbRunningFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lbRunningFlag.setBounds(282, 116, 54, 56);
		contentPane.add(lbRunningFlag);
		
		//init
		File[] files=new File("problems/").listFiles();
		for(int i=0;i<files.length;i++)
		{
			combobProblem.addItem(files[i].getName());
		}
		
		String[] agentTypes=AgentManager.AGENT_TYPES;
		for(int i=0;i<agentTypes.length;i++)
		{
			combobAgentType.addItem(agentTypes[i]);
		}
		
		btnSolve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				solve();
			}
		});
		
		lbRunningFlag.setText("");
		lbRunningFlag.setIcon(new ImageIcon("resources/loading.gif"));
		lbRunningFlag.setVisible(false);
	}
	
	private void enableUI(boolean enable)
	{
		combobProblem.setEnabled(enable);
		combobAgentType.setEnabled(enable);
		cbDebug.setEnabled(enable);
		cbTreeFrame.setEnabled(enable);
		btnSolve.setEnabled(enable);
		
		lbRunningFlag.setVisible(!enable);
	}
	
	private void solve()
	{
		this.enableUI(false);
		
		String instance="problems/"+combobProblem.getSelectedItem();
		//parse problem xml
		ProblemParser parser=new ProblemParser(instance);
		
		Problem problem=null;
		String agentType=(String) combobAgentType.getSelectedItem();
		if(agentType.equals("BFSDPOP"))
		{
			problem=parser.parse(TreeGenerator.TREE_GENERATOR_TYPE_BFS);
		}else
		{
			problem=parser.parse(TreeGenerator.TREE_GENERATOR_TYPE_DFS);
		}
		if(problem==null)
		{
			return;
		}
		
		//display DFS tree，back edges not included
		if(cbTreeFrame.isSelected()==true)
		{
			TreeFrame treeFrame=new TreeFrame(DFSTree.toTreeString(problem.agentNames, problem.parentAgents, problem.childAgents));
			treeFrame.showTreeFrame();
		}
		
		//set whether to print running data records
		Debugger.init(problem.agentNames);
		Debugger.debugOn=cbDebug.isSelected();
		
		//start agents and MessageMailer
		EventListener el=new EventListener() {
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				enableUI(true);
			}
		};
		if(agentType.equals("ADOPT")||agentType.equals("BNBADOPT"))
		{
			//construct agents
			AgentManagerSynchronous agentManager=new AgentManagerSynchronous(problem, agentType);
			MessageMailerSynchronous msgMailer=new MessageMailerSynchronous(agentManager);
			msgMailer.addEventListener(el);
			msgMailer.execute();
		}else
		{
			//construct agents
			AgentManager agentManager=new AgentManager(problem, agentType);
			MessageMailer msgMailer=new MessageMailer(agentManager);
			msgMailer.addEventListener(el);
			agentManager.startAgents(msgMailer);
			msgMailer.start();
		}
	}
}
