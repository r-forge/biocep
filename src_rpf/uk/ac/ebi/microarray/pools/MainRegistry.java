/*
 * Copyright (C) 2007 EMBL-EBI
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class MainRegistry {
	static Registry registry;

	public static void main(String[] args) throws Exception {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}

		if (System.getProperty("kill") != null && System.getProperty("kill").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup("REGMANAGER"));
			try {
				rk.kill();
			} catch (Exception e) {

			}
		} else if (System.getProperty("show") != null && System.getProperty("show").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			String[] list = ((RegistryKiller) registry.lookup("REGMANAGER")).show();
			System.out.println("+Retrieved from remiregistry on " + new Date() + " : ");
			for (int i = 0; i < list.length; ++i) {
				System.out.println(list[i]);
			}

		} else if (System.getProperty("unbindall") != null && System.getProperty("unbindall").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup("REGMANAGER"));
			try {
				rk.unbindAll();
			} catch (Exception e) {

			}
		} else if (System.getProperty("invoke") != null && !System.getProperty("invoke").equals("")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup("REGMANAGER"));
			String[] list = registry.list();
			for (int i = 0; i < list.length; ++i) {
				if (!list[i].equalsIgnoreCase("REGMANAGER")) {
					try {
						Remote r = registry.lookup(list[i]);
						r.getClass().getMethod(System.getProperty("invoke")).invoke(r);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			registry = LocateRegistry.createRegistry(Integer.decode(System.getProperty("port")));
			System.out.println("rmiregistry process id : " + PoolUtils.getProcessId());
			registry.rebind("REGMANAGER", new RegistryKillerImpl());
			while (true) {
				Thread.sleep(100);
			}
		}
	}

	interface RegistryKiller extends Remote {
		void kill() throws RemoteException;

		String[] show() throws RemoteException;

		void unbindAll() throws RemoteException;
	}

	static class RegistryKillerImpl extends UnicastRemoteObject implements RegistryKiller {
		public RegistryKillerImpl() throws RemoteException {
			super();
		}

		public void kill() throws RemoteException {
			System.out.println("rmiregistry is going to die");
			System.exit(0);
		}

		public String[] show() throws RemoteException {
			String[] list = registry.list();
			System.out.println(new Date());
			for (int i = 0; i < list.length; ++i) {
				System.out.println(list[i]);
			}
			return list;
		}

		public void unbindAll() throws RemoteException {
			System.out.println("unbinding all");
			String[] list = registry.list();
			for (int i = 0; i < list.length; ++i) {
				if (!list[i].equalsIgnoreCase("REGMANAGER")) {
					try {
						registry.unbind(list[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
