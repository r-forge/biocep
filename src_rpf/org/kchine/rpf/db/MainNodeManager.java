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
package org.kchine.rpf.db;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.NodeManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.NodeDataDB;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class MainNodeManager {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(MainNodeManager.class);
	private static DBLayer _registry;
	private static String _nodeManagerName;
	private static NodeDataDB _nodeData = null;

	public static void main(String args[]) throws Exception {
		try {

			// for (Object
			// key:PoolUtils.orderO(System.getProperties().keySet()))
			// {System.out.println(key+" = "+System.getProperty((String)key));}

			_registry = (DBLayer) ServerDefaults.getRmiRegistry();
			_nodeManagerName = System.getProperty("node.manager.name");
			if (System.getProperty("node.name") != null && !System.getProperty("node.name").equals("")) {
				_nodeManagerName += '_' + System.getProperty("node.name");
				_nodeData = _registry.getNodeData("NODE_NAME='" + System.getProperty("node.name") + "'").elementAt(0);
			}

			boolean chronjob = System.getProperty("job") != null && System.getProperty("job").equalsIgnoreCase("chron");
			if (chronjob) {
				killTask();
				createTask();
				return;
			}

			boolean srv = System.getProperty("server") != null && System.getProperty("server").equalsIgnoreCase("true");

			if (srv) {
				NodeManager manager = new NodeManagerImpl(_registry);
				ManagedServant oldServant = null;

				try {
					oldServant = ((ManagedServant) _registry.lookup(_nodeManagerName));
				} catch (NotBoundException e) {

				}

				if (oldServant != null) {
					log.info("Found an old servant with this name<" + _nodeManagerName + ">. Killing old servant.");

					try {
						PoolUtils.die(oldServant);
					} catch (RemoteException re) {
						log.info("Old servant wouldn't die! ");
					}
				}

				_registry.rebind(_nodeManagerName, manager);

				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					public void run() {
						try {
							log.info("Shutting Down");
							_registry.unbind(_nodeManagerName);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}));
			}

			boolean javajob = System.getProperty("job") != null && System.getProperty("job").equalsIgnoreCase("java");
			if (javajob) {
				initKillWorker();
				initCreateWorker();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void killTask() {
		try {
			NodeManager _manager = (NodeManager) _registry.lookup(_nodeManagerName);
			Vector<HashMap<String, Object>> servants = null;
			if (_nodeData == null) {
				servants = _registry.listKillable();
			} else {
				servants = _registry.listKillable(_nodeData.getHostIp(), _nodeData.getPoolPrefix());
			}
			for (int i = 0; i < servants.size(); ++i) {
				_manager.kill((String) servants.elementAt(i).get("NAME"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createTask() {
		try {
			System.out.println(" create worker round at " + new Date());
			long T1 = System.currentTimeMillis();

			final ArrayBlockingQueue<String> indexesQueue = new ArrayBlockingQueue<String>(200);

			Vector<NodeDataDB> nodes = _registry.getNodeData("");
			for (int i = 0; i < nodes.size(); ++i) {
				final String nodeName = nodes.elementAt(i).getNodeName();
				String nodeIp = nodes.elementAt(i).getHostIp();
				String nodePrefix = nodes.elementAt(i).getPoolPrefix();

				Vector<HashMap<String, Object>> servants = _registry.getTableData("SERVANTS", "NODE_NAME='" + nodeName + "'" + " OR (HOST_IP='"
						+ nodeIp + "' AND NAME like '" + nodePrefix + "%')");

				final int missing = nodes.elementAt(i).getServantNbrMin() - servants.size();
				if (missing > 0) {
					System.out.println("Node<" + nodeName + "> missing :" + missing);
					for (int j = 0; j < missing; ++j) {
						indexesQueue.add(nodeName);
					}
				}
			}

			Thread[] t = new Thread[10];
			for (int i = 0; i < t.length; ++i) {
				t[i] = new Thread(new Runnable() {
					public void run() {

						while (true) {

							if (indexesQueue.isEmpty())
								break;
							try {
								if (_nodeData == null) {
									if (!indexesQueue.isEmpty()) {
										String nodeName = indexesQueue.poll();
										if (nodeName != null) {
											try {
												_registry.lookup(System.getProperty("node.manager.name") + '_' + nodeName);
											} catch (NotBoundException nbe) {
												NodeManager _manager = (NodeManager) _registry.lookup(System.getProperty("node.manager.name"));
												ManagedServant ms=_manager.createServant(nodeName);
												System.out.println(ms +"  successfully created");
											}
										}
									}
								} else {

									if (!indexesQueue.isEmpty()) {
										String nodeName = indexesQueue.poll();
										if (nodeName != null && nodeName.equals(_nodeData.getNodeName())) {
											NodeManager _manager = (NodeManager) _registry.lookup(_nodeManagerName);
											ManagedServant ms=_manager.createServant(nodeName);
											System.out.println(ms +"  successfully created");
										}
									}

								}

							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try {
									Thread.sleep(500);
								} catch (Exception e) {
								}
							}
						}

					}
				});

				t[i].start();

			}

			for (int i = 0; i < t.length; ++i) {
				t[i].join();
			}

			System.out.println("Last create servants round took :" + (System.currentTimeMillis() - T1) + " millisec");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initKillWorker() {
		new Thread(new Runnable() {
			public void run() {

				while (true) {
					killTask();
					try {
						Thread.sleep(5000);
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}

	private static void initCreateWorker() {
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					createTask();
					try {
						Thread.sleep(5000);
					} catch (Exception e) {
					}
				}
			}
		}).start();

	}
}
