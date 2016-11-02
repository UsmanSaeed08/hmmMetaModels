package CAMPS.utils;

/*
 * ConfigFile
 * 
 * Version 1.0
 * 
 * 2007-01-22
 * 
 */


import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * @author sneumann
 *
 */
public class ConfigFile {
	
	static ConfigFile me;
	static String file = "/home/users/saeed/workspace/CAMPS3/Common/config/config.xml";
	//static String file = "/eclipse_workspace/CAMPS3/Common/config/config.xml";
	Document doc;
	Element root;
	

	public ConfigFile(String file){
		SAXBuilder builder = new SAXBuilder(false);
		try {
			doc = builder.build(new File(file));
			root = doc.getRootElement();
			me = this;
			ConfigFile.file =file;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ConfigFile getInstance(){
		if(me == null && file != null){
			me = new ConfigFile(file);
		}
		if(file == null){return null;}
		else return me;
	}
	
	public String getProperty(String prop){
		String[] temp=prop.split(":");
		Element e=root;
		
		for(int i=0;i<temp.length;i++){
			e = e.getChild(temp[i]);
		}
		return e.getText();
	}

}
