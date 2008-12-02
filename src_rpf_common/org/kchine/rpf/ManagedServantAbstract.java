/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
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
package org.kchine.rpf;

import java.io.Serializable;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public abstract class ManagedServantAbstract extends java.rmi.server.UnicastRemoteObject implements ManagedServant {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ManagedServant.class);

	protected String _servantName;
	private boolean _resetEnabled = true;
	protected String _jobId = "";
	protected Registry _registry;

	public ManagedServantAbstract(String name, String prefix, Registry registry) throws RemoteException {
		this(name, prefix, registry, 0);
	}

	public ManagedServantAbstract(String name, String prefix, Registry registry, int port) throws RemoteException {
		super(port);

		_registry = registry;
		try {
			registry.list();
			log.info("ping registry:ok");
		} catch (ConnectException ce) {
			String message = "can't connect to the naming server, make sure an instance of rmiregistry is running";
			log.info(message);
			throw new RemoteException(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String newname = null;
		if (name == null) {
			while (true) {
				newname = makeName(prefix, registry);
				try {
					registry.bind(newname, java.rmi.server.RemoteObject.toStub(this));
					break;
				} catch (AlreadyBoundException e) {
				}
			}
		} else {

			// if (!name.startsWith(prefix)) throw new
			// RemoteException("The server name must start with :" + prefix);
			ManagedServant oldServant = null;

			try {
				oldServant = ((ManagedServant) registry.lookup(name));
			} catch (NotBoundException e) {

			}

			if (oldServant != null) {
				log.info("Found an old servant with this name. Killing old servant.");

				try {
					PoolUtils.die(oldServant);
				} catch (RemoteException re) {
					log.info("Old servant wouldn't die! ");
				}
			}

			registry.rebind(name, java.rmi.server.RemoteObject.toStub(this));
		}

		_servantName = name == null ? newname : name;

		final Registry reg = registry;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					log.info("Shutting Down");
					reg.unbind(_servantName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));

	}

	/**
	 * Obtain the name this servant is registered under
	 * 
	 * @return Servant's name, if any.
	 * @throws java.rmi.RemoteException
	 */
	public static String makeName(String servantPoolPrefix, Registry rmiRegistry) throws RemoteException {

		String servantName = null;
		String[] servantNames = rmiRegistry.list();

		Vector<Integer> idsVector = new Vector<Integer>();
		for (int i = 0; i < servantNames.length; ++i) {
			String name = PoolUtils.shortRmiName(servantNames[i]);
			if (name.startsWith(servantPoolPrefix)) {
				String prefix = name.substring(servantPoolPrefix.length());
				try {
					idsVector.add(Integer.decode(prefix));
				} catch (Exception e) {
				}
			}
		}

		if (idsVector.size() == 0) {
			servantName = servantPoolPrefix + "1";
		} else {
			idsVector.add(0);
			int[] ids = new int[idsVector.size()];
			for (int i = 0; i < ids.length; ++i) {
				ids[i] = idsVector.elementAt(i);
			}
			Arrays.sort(ids);

			for (int i = 0; i < ids.length - 1; ++i) {
				if (ids[i + 1] > (ids[i] + 1)) {
					servantName = servantPoolPrefix + (ids[i] + 1);
					break;
				}
			}
			if (servantName == null) {
				servantName = servantPoolPrefix + (ids[ids.length - 1] + 1);
			}
		}
		return servantName;

	}

	public String getServantName() throws RemoteException {
		return _servantName;
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.addOutLogListener(listener);
	}

	public void addErrListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.addErrLogListener(listener);
	}

	public void removeOutListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.removeOutLogListener(listener);
	}

	public void removeErrListener(RemoteLogListener listener) throws RemoteException {
		RemoteAppender.removeErrLogListener(listener);
	}

	public void removeAllOutListeners() throws RemoteException {
		RemoteAppender.removeAllOutLogListeners();
	}

	public void removeAllErrListeners() throws RemoteException {
		RemoteAppender.removeAllErrLogListeners();
	}

	public void logInfo(String message) throws RemoteException {
		log.info(message);
	}

	public boolean isResetEnabled() throws RemoteException {
		return _resetEnabled;
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
		_resetEnabled = enable;
	}

	public boolean hasConsoleMode() throws RemoteException {
		return false;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		throw new RemoteException("console mode not supported");
	}

	public boolean hasPushPopMode() throws RemoteException {
		return false;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public Serializable pop(String symbol) throws RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public String[] listSymbols() throws java.rmi.RemoteException {
		throw new RemoteException("push/pop mode not supported");
	}

	public boolean hasGraphicMode() throws RemoteException {
		return false;
	}

	public void startGraphicSession() throws RemoteException {
		throw new RemoteException("graphic mode not supported");
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		throw new RemoteException("graphic mode not supported");
	}

	public void endGraphicSession() throws RemoteException {
		throw new RemoteException("graphic mode not supported");

	}

	public String getProcessId() throws RemoteException {
		return PoolUtils.getProcessId();
	}

	public String getHostIp() throws RemoteException {
		return PoolUtils.getHostIp();
	}

	public boolean isPortInUse(int port) throws RemoteException {
		Socket s = null;
		try {
			s = new Socket("127.0.0.1", port);
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception ex) {
				}
		}
		return true;
	}

	public String getJobId() throws RemoteException {
		return _jobId;
	}

	public void setJobId(String jobId) throws RemoteException {
		_jobId = jobId;
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
	}

	public ManagedServant cloneServer() throws RemoteException {
		return null;
	}

	public boolean isBusy() throws RemoteException {
		return false;
	}

	public String getStub() throws RemoteException {
		return PoolUtils.stubToHex(this);
	}

	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		try {
			Registry registry = ServerDefaults.getRegistry(namingRegistryProperties);
			if (autoName) {
				String newname = null;
				while (true) {
					newname = makeName(prefixOrName, registry);
					try {
						registry.bind(newname, java.rmi.server.RemoteObject.toStub(this));
						break;
					} catch (AlreadyBoundException e) {
					}
				}
				return newname;
			} else {
				registry.rebind(prefixOrName, java.rmi.server.RemoteObject.toStub(this));
				return prefixOrName;
			}
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	public String toString() {
		return super.toString() + " " + _servantName;
	}

}
