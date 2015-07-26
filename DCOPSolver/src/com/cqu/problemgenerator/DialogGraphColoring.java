package com.cqu.problemgenerator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import benchmark.graphcoloring.URGCGenerator;

import com.cqu.util.DialogUtil;

public class DialogGraphColoring extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5759469815301862016L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField tfSavePath;
	
	private JSpinner spinnerInstanceNum;
	private JSpinner spinnerColorNumFrom;
	private JSpinner spinnerColorNumStep;
	private JSpinner spinnerColorNumTo;
	private JSpinner spinnerGraphDensityFrom;
	private JSpinner spinnerGraphDensityStep;
	private JSpinner spinnerGraphDensityTo;
	private JSpinner spinnerAgentNumFrom;
	private JSpinner spinnerAgentNumStep;
	private JSpinner spinnerAgentNumTo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DialogGraphColoring dialog = new DialogGraphColoring();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DialogGraphColoring() {
		setTitle("图着色");
		setBounds(100, 100, 467, 347);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblNumberOfInstances = new JLabel("Number Of Instances:");
			lblNumberOfInstances.setBounds(10, 10, 146, 15);
			contentPanel.add(lblNumberOfInstances);
		}
		{
			spinnerInstanceNum = new JSpinner(new SpinnerNumberModel(25, 1, 500, 1));
			spinnerInstanceNum.setBounds(166, 7, 275, 22);
			contentPanel.add(spinnerInstanceNum);
		}
		{
			JLabel lblNewLabel = new JLabel("");
			lblNewLabel.setBounds(10, 48, 146, 15);
			contentPanel.add(lblNewLabel);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("From             Step              To");
			lblNewLabel_1.setBounds(166, 39, 275, 15);
			contentPanel.add(lblNewLabel_1);
		}
		{
			JLabel lblNumberOfColors = new JLabel("Number Of Colors:");
			lblNumberOfColors.setBounds(10, 73, 146, 15);
			contentPanel.add(lblNumberOfColors);
		}
		{
			spinnerColorNumFrom = new JSpinner(new SpinnerNumberModel(3, 2, 50, 1));
			spinnerColorNumFrom.setBounds(166, 70, 63, 22);
			contentPanel.add(spinnerColorNumFrom);
		}
		{
			spinnerColorNumStep = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
			spinnerColorNumStep.setBounds(268, 70, 63, 22);
			contentPanel.add(spinnerColorNumStep);
		}
		{
			spinnerColorNumTo = new JSpinner(new SpinnerNumberModel(3, 2, 50, 1));
			spinnerColorNumTo.setBounds(378, 70, 63, 22);
			contentPanel.add(spinnerColorNumTo);
		}
		{
			JLabel lblGraphDensity = new JLabel("Graph Density(%):");
			lblGraphDensity.setBounds(10, 117, 146, 15);
			contentPanel.add(lblGraphDensity);
		}
		{
			spinnerGraphDensityFrom = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
			spinnerGraphDensityFrom.setBounds(166, 114, 63, 22);
			contentPanel.add(spinnerGraphDensityFrom);
		}
		{
			spinnerGraphDensityStep = new JSpinner(new SpinnerNumberModel(5, 1, 80, 1));
			spinnerGraphDensityStep.setBounds(268, 114, 63, 22);
			contentPanel.add(spinnerGraphDensityStep);
		}
		{
			spinnerGraphDensityTo = new JSpinner(new SpinnerNumberModel(65, 1, 100, 1));
			spinnerGraphDensityTo.setBounds(378, 114, 63, 22);
			contentPanel.add(spinnerGraphDensityTo);
		}
		{
			JLabel lblNumberOfAgents = new JLabel("Number Of Agents:");
			lblNumberOfAgents.setBounds(10, 164, 146, 15);
			contentPanel.add(lblNumberOfAgents);
		}
		{
			spinnerAgentNumFrom = new JSpinner(new SpinnerNumberModel(10, 5, 20, 1));
			spinnerAgentNumFrom.setBounds(166, 161, 63, 22);
			contentPanel.add(spinnerAgentNumFrom);
		}
		{
			spinnerAgentNumStep = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
			spinnerAgentNumStep.setBounds(268, 161, 63, 22);
			contentPanel.add(spinnerAgentNumStep);
		}
		{
			spinnerAgentNumTo = new JSpinner(new SpinnerNumberModel(30, 5, 1000, 1));
			spinnerAgentNumTo.setBounds(378, 161, 63, 22);
			contentPanel.add(spinnerAgentNumTo);
		}
		{
			JLabel lblPath = new JLabel("Path:");
			lblPath.setBounds(10, 218, 41, 15);
			contentPanel.add(lblPath);
		}
		{
			tfSavePath = new JTextField();
			tfSavePath.setBounds(166, 215, 275, 21);
			contentPanel.add(tfSavePath);
			tfSavePath.setColumns(10);
		}
		{
			JButton btnGenerate = new JButton("Generate");
			btnGenerate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					generateProblems();
				}
			});
			btnGenerate.setBounds(161, 251, 109, 36);
			contentPanel.add(btnGenerate);
		}
		{
			JButton btnBrowse = new JButton("Browse...");
			btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String defaultDir=tfSavePath.getText().trim();
					
					File f=DialogUtil.dialogOpenDir("Select Direcory", defaultDir);
					if(f!=null&&f.isDirectory()==true)
					{
						tfSavePath.setText(f.getPath());
					}
				}
			});
			btnBrowse.setBounds(61, 214, 95, 23);
			contentPanel.add(btnBrowse);
		}
	}
	
	private void generateProblems()
	{
		String saveDir=tfSavePath.getText().trim();
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
		int colorNumFrom=(Integer) spinnerColorNumFrom.getValue();
		int colorNumStep=(Integer) spinnerColorNumStep.getValue();
		int colorNumTo=(Integer) spinnerColorNumTo.getValue();
		int graphDensityFrom=(Integer) spinnerGraphDensityFrom.getValue();
		int graphDensityStep=(Integer) spinnerGraphDensityStep.getValue();
		int graphDensityTo=(Integer) spinnerGraphDensityTo.getValue();
		int agentNumFrom=(Integer) spinnerAgentNumFrom.getValue();
		int agentNumStep=(Integer) spinnerAgentNumStep.getValue();
		int agentNumTo=(Integer) spinnerAgentNumTo.getValue();
		
		if(colorNumFrom>colorNumTo)
		{
			DialogUtil.dialogError("colorNumFrom>colorNumTo");
			return;
		}
		if(graphDensityFrom>graphDensityTo)
		{
			DialogUtil.dialogError("graphDensityFrom>graphDensityTo");
			return;
		}
		if(agentNumFrom>agentNumTo)
		{
			DialogUtil.dialogError("agentNumFrom>agentNumTo");
			return;
		}
		
		URGCGenerator gc = new URGCGenerator(new File(saveDir), instanceNum, "Simple", 
				agentNumFrom, agentNumStep, agentNumTo, 
				graphDensityFrom, graphDensityStep, graphDensityTo,
				colorNumFrom, colorNumStep, colorNumTo);
		gc.generate();
		
		DialogUtil.dialogInformation("finished!");
	}

}
