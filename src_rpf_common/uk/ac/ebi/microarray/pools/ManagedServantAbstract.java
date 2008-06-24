/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.microarray.pools;

import java.io.Serializable;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.logging.Log;


/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public abstract class ManagedServantAbstract extends java.rmi.server.UnicastRemoteObject implements ManagedServant {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ManagedServant.class);

	private String _servantName;
	private boolean _resetEnabled = true;

	
	public ManagedServantAbstract(String name, String prefix, Registry registry) throws RemoteException {
		this(name, prefix,registry,0);		
	}
	public ManagedServantAbstract(String name, String prefix, Registry registry, int port) throws RemoteException {
		super(port);

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
					registry.bind(newname, this);
					break;
				} catch (AlreadyBoundException e) {
				}
			}
		} else {
			if (!name.startsWith(prefix))
				throw new RemoteException("The server name must start with :" + prefix);
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

			registry.rebind(name, this);
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
	public String makeName(String servantPoolPrefix, Registry rmiRegistry) throws RemoteException {

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

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
	}

	public ManagedServant cloneServer() throws RemoteException {
		return null;
	}

	public boolean isBusy() throws RemoteException {
		return false;
	}

	public String toString() {
		return super.toString() + " " + _servantName;
	}

}
