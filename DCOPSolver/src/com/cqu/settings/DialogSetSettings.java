package com.cqu.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.JComboBox;

import com.cqu.tree.DFSTree;

public class DialogSetSettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8769808015375945076L;
	
	private JComboBox<String> combDFSHeuristics;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DialogSetSettings dialog = new DialogSetSettings();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DialogSetSettings() {
		setTitle("持久性设置");
		setBounds(100, 100, 509, 362);
		getContentPane().setLayout(null);
		setModal(true);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(10, 35, 473, 279);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		combDFSHeuristics= new JComboBox<String>();
		String[] heuristics=new String[]{DFSTree.HEURISTIC_RANDOM, DFSTree.HEURISTIC_MAXDEGREE, DFSTree.HEURISTIC_MINDEGREE};
		for(int i=0;i<heuristics.length;i++)
		{
			combDFSHeuristics.addItem(heuristics[i]);
		}
		combDFSHeuristics.setBounds(170, 10, 293, 28);
		panel.add(combDFSHeuristics);
		combDFSHeuristics.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				SettingsPersistent.settings.persistDFSHeuristics((String)combDFSHeuristics.getSelectedItem());
			}
		});
		
		JLabel lblNewLabel_1 = new JLabel("Heuristics：");
		lblNewLabel_1.setBounds(10, 17, 150, 15);
		panel.add(lblNewLabel_1);
		
		JLabel lblNewLabel = new JLabel("DFS Generation");
		lblNewLabel.setBounds(10, 10, 473, 15);
		getContentPane().add(lblNewLabel);
		
		init();
	}
	
	private void init()
	{
		combDFSHeuristics.setSelectedItem(SettingsPersistent.settings.getDfsHeuristics());
	}
}
