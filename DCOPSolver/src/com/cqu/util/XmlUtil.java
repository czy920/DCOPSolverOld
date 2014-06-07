package com.cqu.util;

import java.io.File;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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
}
