/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

import org.kchine.rpf.PoolUtils;


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
