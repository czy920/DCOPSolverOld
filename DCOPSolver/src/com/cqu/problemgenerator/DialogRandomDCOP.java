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

import benchmark.random.DCOPGenerator;

import com.cqu.util.DialogUtil;

public class DialogRandomDCOP extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1426751816008838268L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JSpinner spinnerGraphDensityFrom;
	private JSpinner spinnerGraphDensityStep;
	private JSpinner spinnerGraphDensityTo;
	private JSpinner spinnerMinimumCost;
	private JSpinner spinnerMaximumCost;
	private JTextField tfSavePath;
	private JSpinner spinnerInstanceNum;
	private JSpinner spinnerAgentNum;
	private JSpinner spinnerDomainSize;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DialogRandomDCOP dialog = new DialogRandomDCOP();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DialogRandomDCOP() {
		setTitle("RandomDCOP");
		setBounds(100, 100, 437, 353);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblNumberOfInstances = new JLabel("Number Of Instances:");
		lblNumberOfInstances.setBounds(10, 10, 157, 15);
		contentPanel.add(lblNumberOfInstances);
		
		JLabel lblNewLabel = new JLabel("Number Of Agents:");
		lblNewLabel.setBounds(10, 35, 157, 15);
		contentPanel.add(lblNewLabel);
		
		JLabel lblNumberOf = new JLabel("Domain Size:");
		lblNumberOf.setBounds(10, 60, 157, 15);
		contentPanel.add(lblNumberOf);
		
		JLabel lblNewLabel_1 = new JLabel("Graph Density(%):");
		lblNewLabel_1.setBounds(10, 110, 157, 15);
		contentPanel.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setBounds(10, 85, 157, 15);
		contentPanel.add(lblNewLabel_2);
		
		spinnerGraphDensityFrom = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
		spinnerGraphDensityFrom.setBounds(177, 107, 55, 22);
		contentPanel.add(spinnerGraphDensityFrom);
		
		spinnerGraphDensityStep = new JSpinner(new SpinnerNumberModel(30, 1, 100, 1));
		spinnerGraphDensityStep.setBounds(269, 107, 55, 22);
		contentPanel.add(spinnerGraphDensityStep);
		
		spinnerGraphDensityTo = new JSpinner(new SpinnerNumberModel(80, 1, 100, 1));
		spinnerGraphDensityTo.setBounds(358, 107, 55, 22);
		contentPanel.add(spinnerGraphDensityTo);
		
		JLabel lblNewLabel_3 = new JLabel("From           Step           To");
		lblNewLabel_3.setBounds(177, 85, 236, 15);
		contentPanel.add(lblNewLabel_3);
		
		JLabel lblMinimumCost = new JLabel("Minimum Cost:");
		lblMinimumCost.setBounds(10, 150, 157, 15);
		contentPanel.add(lblMinimumCost);
		
		spinnerMinimumCost = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
		spinnerMinimumCost.setBounds(177, 147, 236, 22);
		contentPanel.add(spinnerMinimumCost);
		
		JLabel lblNewLabel_4 = new JLabel("Maximum Cost:");
		lblNewLabel_4.setBounds(10, 198, 157, 15);
		contentPanel.add(lblNewLabel_4);
		
		spinnerMaximumCost = new JSpinner(new SpinnerNumberModel(100, 0, 10000, 10));
		spinnerMaximumCost.setBounds(177, 195, 236, 22);
		contentPanel.add(spinnerMaximumCost);
		
		JButton btnGenerate = new JButton("Generate");
		btnGenerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateProblems();
			}
		});
		btnGenerate.setBounds(141, 268, 116, 33);
		contentPanel.add(btnGenerate);
		
		JLabel lblPath = new JLabel("Path:");
		lblPath.setBounds(10, 240, 55, 15);
		contentPanel.add(lblPath);
		
		tfSavePath = new JTextField();
		tfSavePath.setBounds(177, 237, 236, 21);
		contentPanel.add(tfSavePath);
		tfSavePath.setColumns(10);
		
		spinnerInstanceNum = new JSpinner(new SpinnerNumberModel(25, 1, 200, 1));
		spinnerInstanceNum.setBounds(177, 7, 234, 22);
		contentPanel.add(spinnerInstanceNum);
		
		spinnerAgentNum = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
		spinnerAgentNum.setBounds(177, 32, 234, 22);
		contentPanel.add(spinnerAgentNum);
		
		spinnerDomainSize = new JSpinner(new SpinnerNumberModel(10, 2, 30, 1));
		spinnerDomainSize.setBounds(177, 57, 236, 22);
		contentPanel.add(spinnerDomainSize);
		
		JButton btnBrowseDir = new JButton("Browse...");
		btnBrowseDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String defaultDir=tfSavePath.getText().trim();
				
				File f=DialogUtil.dialogOpenDir("Select Direcory", defaultDir);
				if(f!=null&&f.isDirectory()==true)
				{
					tfSavePath.setText(f.getPath());
				}
			}
		});
		btnBrowseDir.setBounds(75, 236, 92, 23);
		contentPanel.add(btnBrowseDir);
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
		int agentNum=(Integer) spinnerAgentNum.getValue();
		int domainSize=(Integer) spinnerDomainSize.getValue();
		int graphDensityFrom=(Integer) spinnerGraphDensityFrom.getValue();
		int graphDensityStep=(Integer) spinnerGraphDensityStep.getValue();
		int graphDensityTo=(Integer) spinnerGraphDensityTo.getValue();
		int minimumCost=(Integer) spinnerMinimumCost.getValue();
		int maximumCost=(Integer) spinnerMaximumCost.getValue();
		
		if(graphDensityFrom>graphDensityTo)
		{
			DialogUtil.dialogError("graphDensityFrom>graphDensityTo");
			return;
		}
		
		DCOPGenerator g = new DCOPGenerator(new File(saveDir), "TKC", 
				instanceNum, agentNum, domainSize, 
				graphDensityFrom, graphDensityTo, graphDensityStep, 
				minimumCost, maximumCost);
		g.generate();
		
		DialogUtil.dialogInformation("finished!");
	}
}
