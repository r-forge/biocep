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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;

import org.kchine.r.server.manager.ServantCreationFailed;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;

import static org.kchine.r.server.manager.ServerManager.*;
import static org.kchine.rpf.PoolUtils.isWindowsOs;
import static org.kchine.rpf.PoolUtils.unzip;

public class ToolsMain {


	public static void main(String[] args) throws Exception {

		File tempFile = null;

		tempFile = new File(System.getProperty("java.io.tmpdir") + "/temp_globals.properties").getCanonicalFile();
		if (tempFile.exists())
			tempFile.delete();

		
		
		
		String[] embedPropertiesNames = new String[] { 
				
				"regsitry.host", "regsitry.port", "naming.mode",
				
				"db.type", "db.host", "db.port", "db.dir", "db.name", "db.user", "db.password", 			
				"httpregistry.url", "httpregistry.login", "httpregistry.password", 
				
				"pools.provider.factory",  "pools.dbmode.type",	"pools.dbmode.host", "pools.dbmode.port", "pools.dbmode.dir", "pools.dbmode.name", "pools.dbmode.user", "pools.dbmode.password", 
				"pools.dbmode.defaultpoolname",				
				"pools.dbmode.killused", 
				
				"node.manager.name", "private.servant.node.name", 
				"http.frontend.url",
				
				"cloud"};

		String[] embedPropertiesDefaultValues = new String[embedPropertiesNames.length]; // all null

		Vector<String> genArgs = new Vector<String>();
		genArgs.add(tempFile.getAbsolutePath());
		for (int i = 0; i < embedPropertiesNames.length; ++i) {
			if (embedPropertiesDefaultValues[i] != null)
				genArgs.add(embedPropertiesNames[i] + "=" + embedPropertiesDefaultValues[i]);
		}
		org.kchine.rpf.PropertiesGenerator.main((String[]) genArgs.toArray(new String[0]));

		Properties argMap = new Properties();
		String[] argNames = new String[] { "file", "dir", "outputdir", "mappingjar", "warname", "propsembed", "keepintermediate", "formatsource", "ws.r.api",
				"targetjdk" };
		for (int i = 0; i < argNames.length; ++i) {
			if (System.getProperty(argNames[i]) != null && !System.getProperty(argNames[i]).equals(""))
				argMap.put(argNames[i], System.getProperty(argNames[i]));
		}

		if (argMap.get("propsembed") == null) {
			argMap.put("propsembed", tempFile.getAbsolutePath());
		}

		String rbinary=System.getProperty("r.binary");	

		
		ServerManager.createRInternal(rbinary, false, false, "", 80, argMap, 256, 256, "", false, null, null, null, new Runnable() {
			public void run() {
				System.exit(0);							
			}
		}, "Gen", false);
		
	}

}
