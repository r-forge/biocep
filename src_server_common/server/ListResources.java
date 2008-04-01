package server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

import uk.ac.ebi.microarray.pools.PoolUtils;

public class ListResources {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String rootPath=args[0].replace('\\', '/');
		if (!rootPath.endsWith("/")) rootPath+='/';
		{
			Vector<String> classes=new Vector<String>();
			PoolUtils.getClasses(new File(rootPath),null, classes);
			Properties props=new Properties();
			for (int i=0; i<classes.size(); ++i) props.put(classes.elementAt(i),"");
			props.storeToXML(new FileOutputStream(rootPath+"classlist.xml"), "");
		}		
		{
			Vector<String> resources=new Vector<String>();
			PoolUtils.getResources(new File(rootPath),null, resources);
			Properties props=new Properties();
			for (int i=0; i<resources.size(); ++i) props.put(resources.elementAt(i),"");
			props.storeToXML(new FileOutputStream(rootPath+"resourcelist.xml"), "");
		}
	}

}
