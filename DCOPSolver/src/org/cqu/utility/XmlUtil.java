package org.cqu.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlUtil {
	
	public static Document openXmlDocument(String path)
	{
		SAXBuilder builder=new SAXBuilder();
		try {
			Document doc=builder.build(new File(path));
			return doc;
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void saveXmlDocument(Document doc, String path)
	{
		try {
			new XMLOutputter(Format.getPrettyFormat()).
			output(doc, new FileWriter (path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
