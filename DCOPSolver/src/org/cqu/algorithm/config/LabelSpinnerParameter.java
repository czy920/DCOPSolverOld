package org.cqu.algorithm.config;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;

import java.awt.Dimension;
import java.awt.FlowLayout;

public final class LabelSpinnerParameter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7270892005445506801L;
	
	private JSpinner spinnerParam;

	/**
	 * Create the panel.
	 * model can be null
	 */
	public LabelSpinnerParameter(String labelString, SpinnerNumberModel model) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel label = new JLabel(labelString);
		add(label);
		
		spinnerParam = new JSpinner();
		if(model!=null)
		{
			spinnerParam.setModel(model);
		}
		spinnerParam.setPreferredSize(new Dimension(60, 22));
		add(spinnerParam);

	}
	
	public void setSpinnerParam(SpinnerNumberModel model)
	{
		this.spinnerParam.setModel(model);
	}
	
	public JSpinner getSpinner()
	{
		return this.spinnerParam;
	}

}
