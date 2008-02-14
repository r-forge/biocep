package server;

import static uk.ac.ebi.microarray.pools.ServerDefaults._registryHost;
import static uk.ac.ebi.microarray.pools.ServerDefaults._registryPort;
import static uk.ac.ebi.microarray.pools.ServerDefaults._servantPoolPrefix;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.commons.logging.Log;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantCreationListener;

public class MainRServer {
	
	private static String _mainServantClassName = System.getProperty("servantclass");

	private static Class<?> mainServantClass = null;

	private static Registry rmiRegistry = null;

	private static String servantName = null;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(MainRServer.class);

	private static ManagedServant mservant = null;
	private static ServantCreationListener servantCreationListener = null;
	public static void main(String[] args) throws Exception {

		try {

			if (System.getProperty("autoname") != null && System.getProperty("autoname").equalsIgnoreCase("true")) {
				log.info("Instantiating " + _mainServantClassName + " with autonaming, prefix " + _servantPoolPrefix);
				servantName = null;
			} else {
				// no autonaming, check the name here
				if (System.getProperty("name") != null && !System.getProperty("name").equals("")) {
					servantName = System.getProperty("name");
				}
				log.info("Instantiating " + _mainServantClassName + " with name " + servantName + " , prefix "
						+ _servantPoolPrefix);
			}

			if (rmiRegistry == null) rmiRegistry = LocateRegistry.getRegistry(_registryHost, _registryPort);
			System.out.println("### code base:"+System.getProperty("java.rmi.server.codebase"));
			
			
			mainServantClass = RServantImpl.class;

			boolean isPrivateServant = (System.getProperty("private") != null && System.getProperty("private")
					.equalsIgnoreCase("true"));

			String servantCreationListenerStub = System.getProperty("listener.stub");
			if (servantCreationListenerStub != null && !servantCreationListenerStub.equals("")) {
				servantCreationListener = (ServantCreationListener) PoolUtils.hexToObject(servantCreationListenerStub);
			}

			if (!isPrivateServant) {
				mservant = (ManagedServant) mainServantClass.getConstructor(
						new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { servantName, _servantPoolPrefix, rmiRegistry });

			} else {

				mservant = (ManagedServant) mainServantClass.getConstructor(
						new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { null, "PRIVATE_", rmiRegistry });

			}

			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, mservant, null);
			}

			String sname = mservant.getServantName();
			log.info("sname :::" + sname);
			log.info("Servant " + sname + " instantiated successfully.");

		} catch (InvocationTargetException ite) {
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("",ite.getTargetException()));
			}
			throw new Exception(PoolUtils.getStackTraceAsString(ite.getTargetException()));

		} catch (Exception e) {
			
			log.info("----------------------");
			log.info(PoolUtils.getStackTraceAsString(e));
			e.printStackTrace();
			log.error(e);
			
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("",e));
			}

			System.exit(1);
		}
	}

}
