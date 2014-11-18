package com.cqu.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

public class DialogSettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2649743350270457034L;
	
	private final JPanel contentPanel = new JPanel();
	
	private JSpinner spinnerDPOPCommunicationTime;
	private JSpinner spinnerCommunicationNCCC;
	private JSpinner spinnerBNBmergeADOPTarg;
	private JSpinner spinnerADOPT_K;
	private JCheckBox cbDisplayGraphFrame;
	
	private Settings settings;
	/**
	 * Create the dialog.
	 */
	public DialogSettings(final Settings settings) {
		setTitle("Settings");
		setBounds(100, 100, 314, 245);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		spinnerDPOPCommunicationTime = new JSpinner();
		spinnerDPOPCommunicationTime.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spinnerDPOPCommunicationTime.setBounds(187, 10, 101, 22);
		contentPanel.add(spinnerDPOPCommunicationTime);
		
		JLabel lblDpop = new JLabel("DPOP类算法通信时间：");
		lblDpop.setBounds(10, 13, 167, 15);
		contentPanel.add(lblDpop);
		
		JLabel lblnccc = new JLabel("每次消息通信NCCC：");
		lblnccc.setBounds(10, 48, 167, 15);
		contentPanel.add(lblnccc);
		
		JLabel bdarg = new JLabel("BNB合并算法分层：");
		bdarg.setBounds(10, 76, 167, 15);
		contentPanel.add(bdarg);
		
		JLabel adoptk = new JLabel("ADOPT_k算法的K：");
		adoptk.setBounds(10, 108, 167, 15);
		contentPanel.add(adoptk);
		
		spinnerCommunicationNCCC = new JSpinner();
		spinnerCommunicationNCCC.setModel(new SpinnerNumberModel(0, 0, 1000, 10));
		spinnerCommunicationNCCC.setBounds(187, 42, 101, 22);
		contentPanel.add(spinnerCommunicationNCCC);
		
		spinnerBNBmergeADOPTarg = new JSpinner();
		spinnerBNBmergeADOPTarg.setModel(new SpinnerNumberModel(0, 0, 1, 0.1));
		spinnerBNBmergeADOPTarg.setBounds(187, 74, 101, 22);
		contentPanel.add(spinnerBNBmergeADOPTarg);
		
		spinnerADOPT_K = new JSpinner();
		spinnerADOPT_K.setModel(new SpinnerNumberModel(0, 0, 5000, 1));
		spinnerADOPT_K.setBounds(187, 106, 101, 22);
		contentPanel.add(spinnerADOPT_K);
		
		cbDisplayGraphFrame = new JCheckBox("每次显示GraphFrame");
		cbDisplayGraphFrame.setSelected(true);
		cbDisplayGraphFrame.setBounds(6, 138, 282, 23);
		contentPanel.add(cbDisplayGraphFrame);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnOK = new JButton("OK");
				btnOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						DialogSettings.this.setValues();
						
						DialogSettings.this.dispose();
					}
				});
				btnOK.setActionCommand("OK");
				buttonPane.add(btnOK);
				getRootPane().setDefaultButton(btnOK);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						DialogSettings.this.dispose();
					}
				});
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
		
		this.settings=settings;
		this.initValues();
	}
	
	private void initValues()
	{
		spinnerDPOPCommunicationTime.setValue(settings.getCommunicationTimeInDPOPs());
		spinnerCommunicationNCCC.setValue(settings.getCommunicationNCCCInAdopts());
		spinnerBNBmergeADOPTarg.setValue(settings.getBNBmergeADOPTboundArg());
		spinnerADOPT_K.setValue(settings.getADOPT_K());
		cbDisplayGraphFrame.setSelected(settings.isDisplayGraphFrame());	
	}
	
	private void setValues()
	{
		settings.setCommunicationTimeInDPOPs((Integer)spinnerDPOPCommunicationTime.getValue());
		settings.setCommunicationNCCCInAdopts((Integer)spinnerCommunicationNCCC.getValue());
		settings.setBNBmergeADOPTboundArg((Double)spinnerBNBmergeADOPTarg.getValue());
		settings.setADOPT_K((Integer) spinnerADOPT_K.getValue());
		settings.setDisplayGraphFrame(cbDisplayGraphFrame.isSelected());
	}
}
