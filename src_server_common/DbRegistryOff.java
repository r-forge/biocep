import static uk.ac.ebi.microarray.pools.PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT;
import static uk.ac.ebi.microarray.pools.PoolUtils.cacheJar;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

import server.ServerManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServerDefaults;


public class DbRegistryOff {
	public static void main(String[] args) throws Exception{

		cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/derby.jar"), ServerManager.INSTALL_DIR, LOG_PRGRESS_TO_SYSTEM_OUT, false);
		cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/derbynet.jar"), ServerManager.INSTALL_DIR, LOG_PRGRESS_TO_SYSTEM_OUT, false);
		
		String port=new Integer(PoolUtils.DEFAULT_DB_PORT).toString();	
		if (System.getProperty("db.port")!=null && !System.getProperty("db.port").equals("")) {
			port=System.getProperty("db.port");
		}
		
		Class.forName(ServerDefaults._dbDriver);
		Connection c=null;	
		try {
			c=DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);			
		} catch (Exception e) {
			return;
		} finally {
			if (c!=null) {
				try {c.close();} catch (Exception e) {e.printStackTrace();}
			}
		}

		String cp=new File(ServerManager.INSTALL_DIR+"/derby.jar").getAbsolutePath()+ System.getProperty("path.separator") + new File(ServerManager.INSTALL_DIR+"/derbynet.jar").getAbsolutePath(); 		
		Vector<String> command = new Vector<String>();
		command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));
		command.add("-cp");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add("org.apache.derby.drda.NetworkServerControl");
		
		command.add("shutdown");
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
		
		proc.waitFor();
	}
	
	
}
