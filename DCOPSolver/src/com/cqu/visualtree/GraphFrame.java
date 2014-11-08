package com.cqu.visualtree;

import gui.graph.presentation.GraphPanel;
import gui.graph.presentation.GraphPresentation;
import gui.tree.presentation.TreePanel.Orientation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import kernel.VarIdentifier;
import util.graph.UndirectedGraph;

/**
 * The Frame for visualizing the graph. 
 * 
 * @author Mohamed Wahbi
 * 
 */
public class GraphFrame extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3383117932888752403L;

	private final static String title = "DisChoco: Visalising constraint graphs";

	private static final String ABOUT_MESSAGE = 
			"<html><p align=center><h3>DisChoco 2.0</h3><br> "
			+ "Version 2.0, Freeware, 01-01-2013<br><br>"
			+ "<hr>This program is designed for creating and editing different graph types.<br>"
			+ "Graphs can be loaded and saved.<br>You can undo or redo up to 10 most recent actions.<br>"
			+ "There several graph algorythms that can be executed on your graph.<br><hr><br> "
			+ "Author: Mohamed Wahbi (<a href=\"mailto:wahbi@lirmm.fr\">wahbi@lirmm.fr</a>)<br> "
			+ "Designed for: </p></html>";
		
	private final GraphPanel<VarIdentifier> graphPanel;
	private final JComponent mainComponent= new JComponent() 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6112732782274642321L;
		
		
	};;
	
	/**
	 * Creates the frame
	 */
	public GraphFrame(Map<Integer, int[]> neighbourNodes) {
		super(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		Dimension size = new Dimension(800, 500);
		
		UndirectedGraph<VarIdentifier> graph=this.getConstraintGraph(neighbourNodes);
		GraphPresentation<VarIdentifier> graphPresentation = new GraphPresentation<VarIdentifier>(graph, false, false);
		this.graphPanel = new GraphPanel<VarIdentifier>(graphPresentation, size, Orientation.VERTICAL);
		
		this.mainComponent.setLayout(new BorderLayout());
		this.mainComponent.add(graphPanel, BorderLayout.CENTER);
		setJMenuBar(createMainMenu());
		getContentPane().add(mainComponent, BorderLayout.CENTER);
		setSize(size);
		setPreferredSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int)(screenSize.getWidth()-size.getWidth())/2, (int)(screenSize.getHeight()-size.getHeight())/2);
		pack();
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 3567169626651744786L;

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(GraphFrame.this, ABOUT_MESSAGE, "About", JOptionPane.QUESTION_MESSAGE);
			}
		});
		return helpMenu;
	}
	
	public void showGraphFrame()
	{
		setVisible(true);
	}
	
	private UndirectedGraph<VarIdentifier> getConstraintGraph(Map<Integer, int[]> neighbourNodes){
		UndirectedGraph<VarIdentifier> graph = new UndirectedGraph<VarIdentifier>();
		Map<Integer, VarIdentifier> vars=new HashMap<Integer, VarIdentifier>();
		for(Integer nodeId : neighbourNodes.keySet())
		{
			vars.put(nodeId, new VarIdentifier(nodeId, 1)); 
		}
		for(Integer nodeId : neighbourNodes.keySet())
		{
			VarIdentifier curNode=vars.get(nodeId);
			int[] neighbours=neighbourNodes.get(nodeId);
			for(int i=0;i<neighbours.length;i++)
			{
				graph.addEdge(curNode, vars.get(neighbours[i]));
			}
		}
		return graph;
	}
}
