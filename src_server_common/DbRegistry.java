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
import static org.kchine.rpf.PoolUtils.getDBType;
import static org.kchine.rpf.PoolUtils.isWindowsOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.ConnectionProvider;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.NodeDataDB;
import org.kchine.rpf.db.PoolDataDB;
import org.kchine.rpf.db.RegenerateDB;



public class DbRegistry {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		String port=new Integer(PoolUtils.DEFAULT_DB_PORT).toString();	
		if (System.getProperty("db.port")!=null && !System.getProperty("db.port").equals("")) {
			port=System.getProperty("db.port");
		}
		
		String dir="";
		if (System.getProperty("db.dir")!=null && !System.getProperty("db.dir").equals("")) {
			dir=System.getProperty("db.dir");
		}
		
		DbRegistryOff.main(new String[0]);
		
		String cp=new File(ServerManager.INSTALL_DIR+"/derby.jar").getAbsolutePath()+ System.getProperty("path.separator") + new File(ServerManager.INSTALL_DIR+"/derbynet.jar").getAbsolutePath(); 		
		Vector<String> command = new Vector<String>();
		command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));
		command.add("-Dderby.system.home="+dir);		
		command.add("-cp");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add("org.apache.derby.drda.NetworkServerControl");
		
		command.add("start");
		command.add("-h");
		command.add("0.0.0.0");
		command.add("-p");
		command.add(port);
		
		final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]));

		final Vector<String> outPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		System.out.println(" command : " + command);

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						errorPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						outPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		
		
		boolean create= System.getProperty("create")!=null && System.getProperty("create").equals("true");		
		if (create) {
			Connection c=null;
			while (true) {
				try {
					c=DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
					break;
				} catch (Exception e) {
					//e.printStackTrace();
				} finally {
					if (c!=null) {
						c.close();
					}
				}
				try {Thread.sleep(1000);}catch (Exception e) {}
			}
						
			DBLayer dbLayer = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
				public Connection newConnection() throws SQLException {
					return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
				}
			});
			
			dbLayer.applyDBScript(RegenerateDB.class.getResourceAsStream("/dbscript.sql"));
			
			dbLayer.addPool(new PoolDataDB("R",new String[]{PoolUtils.DEFAULT_PREFIX}, 400000));
			
			
			String jar = DbRegistry.class.getResource("/DbRegistry.class").toString();
			if (jar.startsWith("jar:")) {								
				String jarurl=jar.substring("jar:".length(), jar.length()-"/DbRegistry.class".length()-1);				
				String jarfile = PoolUtils.getFileFromURL(new URL(jarurl)).getAbsolutePath();								
				String cmd=(isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java"+(isWindowsOs() ? "\"" : "")
				+" -Dnaming.mode=db"				
				+" -Ddb.type="+PoolUtils.DEFAULT_DB_TYPE
				+" -Ddb.host="+PoolUtils.getHostIp()
				+" -Ddb.port="+port
				+" -Ddb.name="+PoolUtils.DEFAULT_DB_NAME		
				+" -Ddb.user="+PoolUtils.DEFAULT_DB_USER
				+" -Ddb.password="+PoolUtils.DEFAULT_DB_PASSWORD
				+" -Dnode=N1"
				+(PoolUtils.isAmazonCloud() ? " -Dcloud=ec2":"")
				+ (System.getProperty("r.binary")!=null && !System.getProperty("r.binary").equals("")? " "+(isWindowsOs() ? "\"" : "")+"-Dr.binary="+System.getProperty("r.binary")+(isWindowsOs() ? "\"" : "") : "") 
				+ (System.getProperty("biocep.home")!=null && !System.getProperty("biocep.home").equals("")? " "+(isWindowsOs() ? "\"" : "")+"-Dbiocep.home="+System.getProperty("biocep.home")+(isWindowsOs() ? "\"" : "") : "")
				
				+" -cp "+jarfile+" RmiServer";
				dbLayer.addNode(new NodeDataDB("N1",PoolUtils.getHostIp(),
						PoolUtils.getHostName(),
						"","",ServerManager.INSTALL_DIR,cmd,"",PoolUtils.getOs(),3,5,"RSERVANT_",0));
			}
						
			
		}
	}
}
