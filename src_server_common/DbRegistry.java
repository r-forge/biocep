import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import server.ServerManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServerDefaults;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.NodeDataDB;
import uk.ac.ebi.microarray.pools.db.PoolDataDB;
import uk.ac.ebi.microarray.pools.db.RegenerateDB;


public class DbRegistry {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		String port=new Integer(PoolUtils.DEFAULT_DB_PORT).toString();	
		if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) {
			port=System.getProperty("port");
		}
		
		System.setProperty("db.port",port);
		
		DbRegistryOff.main(new String[0]);
		
		String cp=new File(ServerManager.INSTALL_DIR+"/derby.jar").getAbsolutePath()+ System.getProperty("path.separator") + new File(ServerManager.INSTALL_DIR+"/derbynet.jar").getAbsolutePath(); 		
		Vector<String> command = new Vector<String>();
		command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));
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
		
		
		
		boolean create= args.length==0 && System.getProperty("create")!=null && System.getProperty("create").equals("true");		
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
				String jarfile = jar.substring("jar:file:".length(), jar.length() - "/DbRegistry.class".length() - 1);
				jarfile.replace('\\', '/');
				
				String cmd=(isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java"+(isWindowsOs() ? "\"" : "")
				+" -Dnaming.mode=db"
				+" -Ddb.type="+PoolUtils.DEFAULT_DB_TYPE
				+" -Ddb.host="+PoolUtils.getHostIp()
				+" -Ddb.port="+port
				+" -Ddb.name="+PoolUtils.DEFAULT_DB_NAME		
				+" -Ddb.user="+PoolUtils.DEFAULT_DB_USER
				+" -Ddb.password="+PoolUtils.DEFAULT_DB_PASSWORD
				+" -Dnode=N1 -cp "+jarfile+" RmiServer";
				dbLayer.addNode(new NodeDataDB("N1",PoolUtils.getHostIp(),
						PoolUtils.getHostName(),
						"","",ServerManager.INSTALL_DIR,cmd,"",PoolUtils.getOs(),3,5,"RSERVANT_",0));
			}
						
			
		}
	}
}
