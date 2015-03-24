package org.cqu.problem.generator;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.SpinnerNumberModel;

import org.cqu.utility.DialogUtil;


import frodo2.benchmarks.meetings.MeetingScheduling;

public class DialogMeetingScheduling extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6199380878302755895L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField tfPath;
	private JSpinner spinnerAgentNumFrom;
	private JSpinner spinnerAgentNumStep;
	private JSpinner spinnerAgentNumTo;
	private JSpinner spinnerMeetingNumFrom;
	private JSpinner spinnerMeetingNumStep;
	private JSpinner spinnerMeetingNumTo;
	private JSpinner spinnerAgentNumPerMeeting;
	private JSpinner spinnerInstanceNum;
	private JSpinner spinnerSlots;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DialogMeetingScheduling dialog = new DialogMeetingScheduling();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DialogMeetingScheduling() {
		setModal(true);
		
		setTitle("MeetingScheduling Problem Generator");
		setBounds(100, 100, 555, 355);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblPath = new JLabel("Path：");
		lblPath.setBounds(10, 10, 113, 15);
		contentPanel.add(lblPath);
		
		tfPath = new JTextField();
		tfPath.setBounds(133, 7, 311, 21);
		contentPanel.add(tfPath);
		tfPath.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String defaultDir=tfPath.getText().trim();
				
				File f=DialogUtil.dialogOpenDir("Select Direcory", defaultDir);
				if(f!=null&&f.isDirectory()==true)
				{
					tfPath.setText(f.getPath());
				}
			}
		});
		btnBrowse.setBounds(454, 6, 75, 23);
		contentPanel.add(btnBrowse);
		
		JLabel lblInstances = new JLabel("Instances：");
		lblInstances.setBounds(10, 39, 111, 15);
		contentPanel.add(lblInstances);
		
		JLabel lblAgents = new JLabel("Agents:");
		lblAgents.setBounds(10, 75, 111, 15);
		contentPanel.add(lblAgents);
		
		JLabel lblMeetings = new JLabel("Meetings：");
		lblMeetings.setBounds(10, 114, 111, 15);
		contentPanel.add(lblMeetings);
		
		JLabel lblAgentspermeeting = new JLabel("AgentsPerMeeting：");
		lblAgentspermeeting.setBounds(10, 153, 111, 15);
		contentPanel.add(lblAgentspermeeting);
		
		JButton btnGenerate = new JButton("Generate");
		btnGenerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateProblems();
			}
		});
		btnGenerate.setBounds(10, 225, 519, 82);
		contentPanel.add(btnGenerate);
		
		spinnerAgentNumFrom = new JSpinner();
		spinnerAgentNumFrom.setModel(new SpinnerNumberModel(10, 5, 100, 1));
		spinnerAgentNumFrom.setBounds(133, 72, 75, 22);
		contentPanel.add(spinnerAgentNumFrom);
		
		spinnerAgentNumStep = new JSpinner();
		spinnerAgentNumStep.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinnerAgentNumStep.setBounds(297, 72, 80, 22);
		contentPanel.add(spinnerAgentNumStep);
		
		spinnerAgentNumTo = new JSpinner();
		spinnerAgentNumTo.setModel(new SpinnerNumberModel(12, 5, 100, 1));
		spinnerAgentNumTo.setBounds(454, 72, 75, 22);
		contentPanel.add(spinnerAgentNumTo);
		
		spinnerMeetingNumFrom = new JSpinner();
		spinnerMeetingNumFrom.setModel(new SpinnerNumberModel(10, 2, 100, 1));
		spinnerMeetingNumFrom.setBounds(133, 111, 75, 22);
		contentPanel.add(spinnerMeetingNumFrom);
		
		spinnerMeetingNumStep = new JSpinner();
		spinnerMeetingNumStep.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinnerMeetingNumStep.setBounds(297, 111, 81, 22);
		contentPanel.add(spinnerMeetingNumStep);
		
		spinnerMeetingNumTo = new JSpinner();
		spinnerMeetingNumTo.setModel(new SpinnerNumberModel(10, 2, 100, 1));
		spinnerMeetingNumTo.setBounds(454, 111, 75, 22);
		contentPanel.add(spinnerMeetingNumTo);
		
		spinnerAgentNumPerMeeting = new JSpinner();
		spinnerAgentNumPerMeeting.setModel(new SpinnerNumberModel(2, 2, 100, 1));
		spinnerAgentNumPerMeeting.setBounds(133, 150, 396, 22);
		contentPanel.add(spinnerAgentNumPerMeeting);
		
		spinnerInstanceNum = new JSpinner();
		spinnerInstanceNum.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		spinnerInstanceNum.setBounds(133, 38, 396, 22);
		contentPanel.add(spinnerInstanceNum);
		
		JLabel lblSlots = new JLabel("Slots：");
		lblSlots.setBounds(10, 196, 54, 15);
		contentPanel.add(lblSlots);
		
		spinnerSlots = new JSpinner();
		spinnerSlots.setModel(new SpinnerNumberModel(3, 2, 20, 1));
		spinnerSlots.setBounds(133, 193, 396, 22);
		contentPanel.add(spinnerSlots);
	}
	
	private void generateProblems()
	{
		String saveDir=tfPath.getText().trim();
		if(saveDir.isEmpty())
		{
			DialogUtil.dialogError("save dir is empty");
			return;
		}
		File f=new File(saveDir);
		if(f.exists()==false)
		{
			f.mkdirs();
		}
			
		int instanceNum=(Integer) spinnerInstanceNum.getValue();
		int agentNumFrom=(Integer) spinnerAgentNumFrom.getValue();
		int agentNumStep=(Integer) spinnerAgentNumStep.getValue();
		int agentNumTo=(Integer) spinnerAgentNumTo.getValue();
		int meetingNumFrom=(Integer) spinnerMeetingNumFrom.getValue();
		int meetingNumStep=(Integer) spinnerMeetingNumStep.getValue();
		int meetingNumTo=(Integer) spinnerMeetingNumTo.getValue();
		int slots=(Integer) spinnerSlots.getValue();
		int agentNumPerMeeting=(Integer) spinnerAgentNumPerMeeting.getValue();
		
		if(agentNumFrom>agentNumTo)
		{
			DialogUtil.dialogError("agentNumFrom>agentNumTo");
			return;
		}
		if(meetingNumFrom>meetingNumTo)
		{
			DialogUtil.dialogError("meetingNumFrom>meetingNumTo");
			return;
		}
		if(agentNumPerMeeting>agentNumFrom)
		{
			DialogUtil.dialogError("agentNumPerMeeting>agentNumFrom");
			return;
		}
		
		for(int i=agentNumFrom;i<=agentNumTo;i+=agentNumStep)
		{
			int agentNum=i;
			for(int j=meetingNumFrom;j<=meetingNumTo;j+=meetingNumStep)
			{
				int meetingNum=j;
				for(int k=0;k<instanceNum;k++)
				{
					MeetingScheduling.generateOneProblem(saveDir+"\\MS_"+agentNum+"_"+meetingNum+"_"+agentNumPerMeeting+"_"+(k+1)+".xml", 
							agentNum, meetingNum, agentNumPerMeeting, slots);
				}
			}
		}
		DialogUtil.dialogInformation("finished!");
	}
}
