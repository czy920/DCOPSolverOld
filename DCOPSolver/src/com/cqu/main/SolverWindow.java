package com.cqu.main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.awt.CardLayout;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.border.BevelBorder;
import javax.swing.JSplitPane;
import javax.swing.JCheckBox;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.JEditorPane;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import javax.swing.border.LineBorder;

public class SolverWindow {

	private JFrame frmDcopsolver;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField;

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
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDcopsolver = new JFrame();
		frmDcopsolver.setResizable(false);
		frmDcopsolver.setTitle("DCOPSolver");
		frmDcopsolver.setBounds(100, 100, 687, 443);
		frmDcopsolver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmDcopsolver.setJMenuBar(menuBar);
		
		JMenu mnf = new JMenu("文件(F)");
		mnf.setMnemonic('F');
		menuBar.add(mnf);
		
		JMenuItem miOpen = new JMenuItem("打开");
		mnf.add(miOpen);
		
		JMenuItem miOpenDir = new JMenuItem("打开目录");
		mnf.add(miOpenDir);
		
		JMenu mnr = new JMenu("运行(R)");
		mnr.setMnemonic('R');
		menuBar.add(mnr);
		
		JMenuItem miRun = new JMenuItem("运行");
		mnr.add(miRun);
		
		JMenu mnh = new JMenu("帮助(H)");
		mnh.setMnemonic('H');
		menuBar.add(mnh);
		
		JMenuItem miAbout = new JMenuItem("关于");
		mnh.add(miAbout);
		frmDcopsolver.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setLayout(null);
		panel.setBounds(10, 10, 459, 259);
		frmDcopsolver.getContentPane().add(panel);
		
		JLabel label = new JLabel("路径：");
		label.setBounds(10, 13, 45, 15);
		panel.add(label);
		
		textField = new JTextField();
		textField.setColumns(10);
		textField.setBounds(64, 10, 385, 21);
		panel.add(textField);
		
		JLabel label_1 = new JLabel("算法：");
		label_1.setBounds(10, 47, 45, 15);
		panel.add(label_1);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(64, 41, 385, 32);
		panel.add(comboBox);
		
		JSpinner spinner = new JSpinner();
		spinner.setBounds(64, 83, 65, 22);
		panel.add(spinner);
		
		JLabel label_2 = new JLabel("0/0/0");
		label_2.setBounds(207, 86, 118, 15);
		panel.add(label_2);
		
		JCheckBox checkBox = new JCheckBox("批处理");
		checkBox.setBounds(384, 79, 65, 23);
		panel.add(checkBox);
		
		JLabel label_3 = new JLabel("遍数：");
		label_3.setBounds(10, 86, 45, 15);
		panel.add(label_3);
		
		JLabel label_4 = new JLabel("");
		label_4.setHorizontalAlignment(SwingConstants.CENTER);
		label_4.setBounds(395, 193, 54, 56);
		panel.add(label_4);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(10, 278, 459, 106);
		frmDcopsolver.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel label_5 = new JLabel("DPOP类算法通信时间：");
		label_5.setBounds(10, 13, 141, 15);
		panel_1.add(label_5);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setBounds(161, 10, 70, 22);
		panel_1.add(spinner_1);
		
		JLabel label_6 = new JLabel("每次消息通信NCCC：");
		label_6.setBounds(241, 16, 130, 15);
		panel_1.add(label_6);
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setBounds(381, 13, 70, 22);
		panel_1.add(spinner_2);
		
		JLabel label_7 = new JLabel("BNB合并算法分层：");
		label_7.setBounds(10, 45, 141, 15);
		panel_1.add(label_7);
		
		JSpinner spinner_3 = new JSpinner();
		spinner_3.setBounds(161, 42, 70, 22);
		panel_1.add(spinner_3);
		
		JCheckBox checkBox_1 = new JCheckBox("每次显示GraphFrame");
		checkBox_1.setSelected(true);
		checkBox_1.setBounds(241, 41, 210, 23);
		panel_1.add(checkBox_1);
		
		JCheckBox checkBox_2 = new JCheckBox("输出Debug信息");
		checkBox_2.setEnabled(true);
		checkBox_2.setBounds(6, 76, 225, 23);
		panel_1.add(checkBox_2);
		
		JCheckBox checkBox_3 = new JCheckBox("每次显示Tree Frame");
		checkBox_3.setEnabled(true);
		checkBox_3.setBounds(241, 76, 210, 23);
		panel_1.add(checkBox_3);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.setLayout(null);
		panel_2.setBounds(478, 10, 193, 374);
		frmDcopsolver.getContentPane().add(panel_2);
		
		JLabel label_8 = new JLabel("Total Time：");
		label_8.setBounds(10, 10, 80, 15);
		panel_2.add(label_8);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(100, 7, 83, 21);
		panel_2.add(textField_1);
		
		JLabel label_9 = new JLabel("Total Cost：");
		label_9.setBounds(10, 47, 80, 15);
		panel_2.add(label_9);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(100, 44, 83, 21);
		panel_2.add(textField_2);
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setBounds(10, 72, 173, 292);
		panel_2.add(editorPane);
	}
}
