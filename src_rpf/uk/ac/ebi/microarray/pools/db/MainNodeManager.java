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
package uk.ac.ebi.microarray.pools.db;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;

import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.NodeManager;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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

			_registry = (DBLayer) DBLayer.getRmiRegistry();
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

				Vector<HashMap<String, Object>> servants = _registry.getTableData("SERVANTS", "NODE_NAME='" + nodeName + "'" + " OR (REGISTER_HOST_IP='"
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
												_manager.createServant(nodeName);
											}
										}
									}
								} else {

									if (!indexesQueue.isEmpty()) {
										String nodeName = indexesQueue.poll();
										if (nodeName != null && nodeName.equals(_nodeData.getNodeName())) {
											NodeManager _manager = (NodeManager) _registry.lookup(_nodeManagerName);
											_manager.createServant(nodeName);
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
