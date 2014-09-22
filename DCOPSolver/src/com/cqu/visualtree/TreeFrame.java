package com.cqu.visualtree;

import gui.tree.presentation.TreePanel;
import gui.tree.presentation.TreePresentation;
import gui.tree.presentation.TreePanel.Orientation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import kernel.VarIdentifier;
import util.tree.AbstractTree;

/**
 * The Frame for visualizing the graph. 
 * 
 * @author Mohamed Wahbi
 * 
 */
@SuppressWarnings("serial")
public class TreeFrame extends JFrame {

	private final static String title = "DisChoco: Visalising constraint graphs";

	private static final String ABOUT_MESSAGE = "<html><p align=center><h3>DisChoco 2.0</h3><br> " + "Version 2.0, Freeware, 01-01-2013<br><br>"
			+ "<hr>This program is designed for creating and editing different graph types.<br>"
			+ "Trees can be loaded and saved.<br>You can undo or redo up to 10 most recent actions.<br>"
			+ "There several graph algorythms that can be executed on your graph.<br><hr><br> "
			+ "Author: Mohamed Wahbi (<a href=\"mailto:wahbi@lirmm.fr\">wahbi@lirmm.fr</a>)<br> " + "Designed for: </p></html>";

	private final TreePanel<VarIdentifier> treePanel;
	private final JComponent mainComponent;

	/**
	 * Creates the frame
	 */
	public TreeFrame(String treeString) {
		super(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		Dimension size = new Dimension(800, 500);
		AbstractTree<VarIdentifier> tree = AbstractTree.fromString(treeString , VarIdentifier.class);
		TreePresentation<VarIdentifier> treePresentation = new TreePresentation<VarIdentifier>(tree, false, false);
		this.treePanel = new TreePanel<VarIdentifier>(treePresentation, size, Orientation.VERTICAL);

		this.mainComponent = new JComponent() {};
		this.mainComponent.setLayout(new BorderLayout());
		this.mainComponent.add(treePanel, BorderLayout.CENTER);
		setJMenuBar(createMainMenu());
		getContentPane().add(mainComponent, BorderLayout.CENTER);
		setSize(size);
		setPreferredSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (screenSize.getWidth() - size.getWidth()) / 2, (int) (screenSize.getHeight() - size.getHeight()) / 2);
		pack();
	}
	
	public void showTreeFrame()
	{
		setVisible(true);
	}

	private JMenuBar createMainMenu() {
		JMenuBar mainMenu = new JMenuBar();
		mainMenu.add(createLayoutsMenu());
		mainMenu.add(createConvertionMenu());
		mainMenu.add(createHelpMenu());
		return mainMenu;
	}

	private JMenu createLayoutsMenu() {
		JMenu algorythmMenu = new JMenu("Layouts");
		return algorythmMenu;
	}

	private JMenu createConvertionMenu() {
		JMenu convertionMenu = new JMenu("Convert");
		return convertionMenu;
	}

	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new AbstractAction("About") {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(TreeFrame.this, ABOUT_MESSAGE, "About", JOptionPane.QUESTION_MESSAGE);
			}
		});
		return helpMenu;
	}
}
