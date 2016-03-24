package com.cqu.problemgenerator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import util.Commands;
import benchmark.sensornetwork.SensorDCSPGenerator;

public class DialogSensorNetwork extends JDialog {
	private JPanel panel = null;
	private JButton browse = null;
	private JButton generate = null;
	private JComboBox localLayout = null;
	private JComboBox model = null;
	private JSpinner numberOfIns = null;
	private JSpinner numOfX = null;
	private JSpinner numOfY = null;
	private JSpinner numOfS = null;
	private JSpinner numOfT = null;
	private JSpinner KcFrom = null;
	private JSpinner KcStep = null;
	private JSpinner KcTo = null;
	private JSpinner KvFrom = null;
	private JSpinner KvStep = null;
	private JSpinner KvTo = null;
	private JSpinner PcFrom = null;
	private JSpinner PcStep = null;
	private JSpinner PcTo = null;
	private JSpinner PvFrom = null;
	private JSpinner PvStep = null;
	private JSpinner PvTo = null;
	private JLabel la = null;
	private File file = null;
	
	public DialogSensorNetwork() {
		panel = new JPanel();
		setTitle("SensorNetwork Problem Generator");
		setBounds(400, 100, 600, 500);
		getContentPane().setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
		
		browse = new JButton("Browse");
		panel.add(browse);
		browse.setBounds(180,10,100,20);
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(".");
				fileChooser.setDialogTitle("Select Direcory");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(fileChooser);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();
				}
			}
		});
		
		generate = new JButton("Generate");
		panel.add(generate);
		generate.setBounds(320,10,100,20);
		generate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (file == null) {
					JOptionPane.showMessageDialog(null, "Choose firstly a directoty", "DisChoco", JOptionPane.WARNING_MESSAGE);
					return;
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							Integer nbInstances = getNumberOfIns();
							Integer x_axis = getNumOfX();
							Integer y_axis = getNumOfY();
							Integer nbSensors = getNumOfS();
							Integer nbTargets = getNumOfT();
							Integer minKv = getKvFrom();
							Integer maxKv = getKvTo();
							Integer stepKv = getKvStep();
							Integer minKc = getKcFrom();
							Integer maxKc = getKcTo();
							Integer stepKc = getKcStep();
							Integer minPv = getPvFrom();
							Integer maxPv = getPvTo();
							Integer stepPv = getPvStep();
							Integer minPc = getPcFrom();
							Integer maxPc = getPcTo();
							Integer stepPc = getPcStep();
							String layout = getLocalLayout();
							String benchmark = Commands.GSensorDCSP; 
							String model = getModel();
							String constraintModel = "TKC";
							if(layout.equals("Real")) {
								benchmark = Commands.SensorDCSP; 
							}
							SensorDCSPGenerator sn = new SensorDCSPGenerator(
									benchmark, model, constraintModel, layout, 
									file, nbInstances, nbSensors, nbTargets, 
									minPv, maxPv, stepPv, 
									minKv, maxKv, stepKv, 
									minPc, maxPc, stepPc, 
									minKc, maxKc, stepKc, 
									x_axis, y_axis);
							sn.generate();
							JOptionPane.showMessageDialog(null, "Problems are generated succefully");
							file = null;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		
		String[] layouts = {"Grid", "Real"};
		localLayout = new JComboBox(layouts);
		localLayout.setSelectedItem("Grid");
		panel.add(localLayout);
		localLayout.setBounds(130,50,150,20);
		
		String[] models = {"Simple", "Complex"};
		model = new JComboBox(models);
		model.setSelectedItem("Complex");
		panel.add(model);
		model.setBounds(320,50,150,20);
		
		la = new JLabel("Number of Instances:");
		panel.add(la);
		la.setBounds(200,90,180,20);
		
		numberOfIns = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
		panel.add(numberOfIns);
		numberOfIns.setBounds(350,90,50,20);
		
		la = new JLabel("Number of x-axis:");
		panel.add(la);
		la.setBounds(100,130,100,20);
		
		numOfX = new JSpinner(new SpinnerNumberModel(5,1,100,1));
		panel.add(numOfX);
		numOfX.setBounds(220,130,60,20);
		
		la = new JLabel("Number of y-axis:");
		panel.add(la);
		la.setBounds(320,130,100,20);
		
		numOfY = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		panel.add(numOfY);
		numOfY.setBounds(440,130,60,20);
		
		la = new JLabel("Number of Sensors:");
		panel.add(la);
		la.setBounds(80,170,120,20);
		
		numOfS = new JSpinner(new SpinnerNumberModel(25, 5, 1000, 1));
		panel.add(numOfS);
		numOfS.setBounds(220,170,60,20);
		
		la = new JLabel("Number of Targets:");
		panel.add(la);
		la.setBounds(300,170,120,20);
		
		numOfT = new JSpinner(new SpinnerNumberModel(5, 3, 100, 1));
		panel.add(numOfT);
		numOfT.setBounds(440,170,60,20);
		
		la = new JLabel("From");
		panel.add(la);
		la.setBounds(250,220,50,20);
		
		la = new JLabel("Step");
		panel.add(la);
		la.setBounds(340,220,50,20);
		
		la = new JLabel("To");
		panel.add(la);
		la.setBounds(430,220,50,20);
		
		la = new JLabel("Kc-restricted compatibility:");
		panel.add(la);
		la.setBounds(60,270,170,20);
		
		KcFrom = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
		panel.add(KcFrom);
		KcFrom.setBounds(250,270,50,20);
		
		KcStep = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		panel.add(KcStep);
		KcStep.setBounds(340,270,50,20);
		
		KcTo = new JSpinner(new SpinnerNumberModel(2, 2, 50, 1));
		panel.add(KcTo);
		KcTo.setBounds(430,270,50,20);
		
		la = new JLabel("Kv-restricted visibility:");
		panel.add(la);
		la.setBounds(80,310,150,20);
		
		KvFrom = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
		panel.add(KvFrom);
		KvFrom.setBounds(250,310,50,20);
		
		KvStep = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		panel.add(KvStep);
		KvStep.setBounds(340,310,50,20);
		
		KvTo = new JSpinner(new SpinnerNumberModel(2, 2, 50, 1));
		panel.add(KvTo);
		KvTo.setBounds(430,310,50,20);
		
		la = new JLabel("Density of communication(Pc):");
		panel.add(la);
		la.setBounds(50,350,180,20);
		
		PcFrom = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
		panel.add(PcFrom);
		PcFrom.setBounds(250,350,50,20);
		
		PcStep = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		panel.add(PcStep);
		PcStep.setBounds(340,350,50,20);
		
		PcTo = new JSpinner(new SpinnerNumberModel(80, 1, 100, 1));
		panel.add(PcTo);
		PcTo.setBounds(430,350,50,20);
		
		la = new JLabel("Density of visibility(Pv)");
		panel.add(la);
		la.setBounds(80,390,150,20);
		
		PvFrom = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
		panel.add(PvFrom);
		PvFrom.setBounds(250,390,50,20);
		
		PvStep = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		panel.add(PvStep);
		PvStep.setBounds(340,390,50,20);
		
		PvTo = new JSpinner(new SpinnerNumberModel(90, 1, 100, 1));
		panel.add(PvTo);
		PvTo.setBounds(430,390,50,20);
		
	}

	public String getLocalLayout() {
		return (String)localLayout.getSelectedItem();
	}

	public String getModel() {
		return (String)model.getSelectedItem();
	}

	public Integer getNumberOfIns() {
		return (Integer)numberOfIns.getValue();
	}

	public Integer getNumOfX() {
		return (Integer)numOfX.getValue();
	}

	public Integer getNumOfY() {
		return (Integer)numOfY.getValue();
	}

	public Integer getNumOfS() {
		return (Integer)numOfS.getValue();
	}

	public Integer getNumOfT() {
		return (Integer)numOfT.getValue();
	}

	public Integer getKcFrom() {
		return (Integer)KcFrom.getValue();
	}

	public Integer getKcStep() {
		return (Integer)KcStep.getValue();
	}

	public Integer getKcTo() {
		return (Integer)KcTo.getValue();
	}

	public Integer getKvFrom() {
		return (Integer)KvFrom.getValue();
	}

	public Integer getKvStep() {
		return (Integer)KvStep.getValue();
	}

	public Integer getKvTo() {
		return (Integer)KvTo.getValue();
	}

	public Integer getPcFrom() {
		return (Integer)PcFrom.getValue();
	}

	public Integer getPcStep() {
		return (Integer)PcStep.getValue();
	}

	public Integer getPcTo() {
		return (Integer)PcTo.getValue();
	}

	public Integer getPvFrom() {
		return (Integer)PvFrom.getValue();
	}

	public Integer getPvStep() {
		return (Integer)PvStep.getValue();
	}

	public Integer getPvTo() {
		return (Integer)PvTo.getValue();
	}

	public static void main(String[] args) {
		new DialogSensorNetwork();
	}
}
