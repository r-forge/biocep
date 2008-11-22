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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.YesSecurityManager;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class MainRegistry {
	static Registry registry;
	
	public static final String REGISTRY_MANAGER_NAME="REGMANAGER";

	public static void main(String[] args) throws Exception {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}

		if (System.getProperty("kill") != null && System.getProperty("kill").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			try {
				rk.kill();
			} catch (Exception e) {

			}
		} else if (System.getProperty("show") != null && System.getProperty("show").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			String[] list = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME)).show();
			System.out.println("+Retrieved from remiregistry on " + new Date() + " : ");
			for (int i = 0; i < list.length; ++i) {
				System.out.println(list[i]);
			}

		} else if (System.getProperty("unbindall") != null && System.getProperty("unbindall").equals("true")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			try {
				rk.unbindAll();
			} catch (Exception e) {

			}
		} else if (System.getProperty("invoke") != null && !System.getProperty("invoke").equals("")) {
			registry = LocateRegistry.getRegistry(Integer.decode(System.getProperty("port")));
			RegistryKiller rk = ((RegistryKiller) registry.lookup(REGISTRY_MANAGER_NAME));
			String[] list = registry.list();
			for (int i = 0; i < list.length; ++i) {
				if (!list[i].equalsIgnoreCase(REGISTRY_MANAGER_NAME)) {
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
			registry.rebind(REGISTRY_MANAGER_NAME, new RegistryKillerImpl());
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
				if (!list[i].equalsIgnoreCase(REGISTRY_MANAGER_NAME)) {
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
