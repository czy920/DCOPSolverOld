package com.cqu.settings;

import org.jdom2.Document;
import org.jdom2.Element;

import com.cqu.util.XmlUtil;

public class SettingsPersistent {
	
	private static final String SETTINGS_PERSISTENT_PATH="resources/settings.xml";
	private static final String DFSGeneration="DFSGeneration";
	private static final String Heuristics="heuristics";
	private static final String Value="value";
	
	private Document doc;
	private Element root;
	
	private String dfsHeuristics;
	
	public static final SettingsPersistent settings=new SettingsPersistent();
	
	public SettingsPersistent() {
		// TODO Auto-generated constructor stub
		doc = XmlUtil.openXmlDocument(SETTINGS_PERSISTENT_PATH);
		this.root = doc.getRootElement();
		
		readAllSettings();
	}
	
	private void readAllSettings()
	{
		readDFSHeuristics();
	}
	
	private void readDFSHeuristics()
	{
		Element nodeDFSHeuristics=root.getChild(DFSGeneration).getChild(Heuristics);
		
		this.dfsHeuristics=nodeDFSHeuristics.getAttributeValue(Value);
	}
	
	public void persistDFSHeuristics(String dfsHeuristics)
	{
		Element nodeDFSHeuristics=root.getChild(DFSGeneration).getChild(Heuristics);
		nodeDFSHeuristics.setAttribute(Value, dfsHeuristics);
		
		this.dfsHeuristics=dfsHeuristics;
		
		XmlUtil.saveXmlDocument(doc, SETTINGS_PERSISTENT_PATH);
	}

	public String getDfsHeuristics() {
		return dfsHeuristics;
	}

}
