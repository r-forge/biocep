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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.logging.Log;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;
import uk.ac.ebi.microarray.pools.LookUpInterrupted;
import uk.ac.ebi.microarray.pools.LookUpTimeout;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RPFSessionInfo;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;
import static uk.ac.ebi.microarray.pools.ServerDefaults._registryHost;
import static uk.ac.ebi.microarray.pools.ServerDefaults._registryPort;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public abstract class DBLayer implements Registry {

	private Connection _connection;
	private ConnectionProvider _connectionProvider;
	private static final Log _log = org.apache.commons.logging.LogFactory.getLog(DBLayer.class);

	abstract void lock(Statement stmt) throws SQLException;

	abstract void unlock(Statement stmt) throws SQLException;

	abstract String sysdateFunctionName();

	abstract boolean isNoConnectionError(SQLException sqle);

	abstract boolean isConstraintViolationError(SQLException sqle);

	public DBLayer(Connection connection) {
		_connection = connection;
		try {
			_connection.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			lock(stmt);
			rset = stmt.executeQuery("select count(*) from SERVANTS where NAME='" + name + "'");
			rset.next();
			if (rset.getInt(1) > 0) {
				throw new AlreadyBoundException();
			}
		} catch (AlreadyBoundException abe) {
			try {
				if (stmt != null) {
					unlock(stmt);
					_connection.commit();
				}
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
			throw abe;
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				bind(name, obj);
			} else {
				throw new RemoteException("", sqle);
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
		}

		stmt = null;
		String stub_hex = stubToHex(obj);
		try {
			stmt = _connection.createStatement();
			stmt
					.execute("Insert into SERVANTS (NAME,STUB_HEX,IN_USE,PING_FAILURES,REGISTER_TIME,REGISTER_PROCESS_ID,REGISTER_HOST_NAME,REGISTER_HOST_IP,REGISTER_OS,CODEBASE) "
							+ "values ('"
							+ name
							+ "','"
							+ stub_hex
							+ "',0,0,"
							+ sysdateFunctionName()
							+ ",'"
							+ getProcessId()
							+ "','"
							+ getHostName()
							+ "','"
							+ getHostIp()
							+ "','"
							+ System.getProperty("os.name")
							+ "',"
							+ (System.getProperty("java.rmi.server.codebase") == null ? "NULL" : "'" + System.getProperty("java.rmi.server.codebase") + "'")
							+ ")");
		} catch (SQLException sqle) {
			if (isConstraintViolationError(sqle))
				throw new AlreadyBoundException();
			else
				throw new RemoteException("", (sqle));
		} finally {
			try {
				if (stmt != null) {
					unlock(stmt);
					_connection.commit();
				}
			} catch (Exception e) {
				throw new RemoteException("", (e));
			}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public String[] list() throws RemoteException, AccessException {
		Vector<String> result = new Vector<String>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			rset = stmt.executeQuery("select NAME from SERVANTS");
			while (rset.next()) {
				result.add(rset.getString(1));
			}
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				return list();
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}

		return result.toArray(new String[0]);

	}

	public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			rset = stmt.executeQuery("select STUB_HEX,CODEBASE from SERVANTS where NAME='" + name + "'");
			if (rset.next()) {

				final String stubHex = rset.getString(1);
				final String codeBaseStr = rset.getString(2);
				final ClassLoader cl = (codeBaseStr != null ? new URLClassLoader(PoolUtils.getURLS(codeBaseStr), DBLayer.class.getClassLoader())
						: DBLayer.class.getClassLoader());
				System.out.println("codeBaseStr ::" + codeBaseStr);

				final Object[] resultHolder = new Object[1];
				Runnable lookupRunnable = new Runnable() {
					public void run() {
						try {
							resultHolder[0] = hexToStub(stubHex, cl);
						} catch (Exception e) {
							final boolean wasInterrupted = Thread.interrupted();
							if (wasInterrupted) {
								resultHolder[0] = new LookUpInterrupted();
							} else {
								resultHolder[0] = e;
							}
						}
					}
				};

				Thread lookupThread = InterruptibleRMIThreadFactory.getInstance().newThread(lookupRunnable);
				lookupThread.start();

				long t1 = System.currentTimeMillis();
				while (resultHolder[0] == null) {
					if ((System.currentTimeMillis() - t1) > PoolUtils.LOOKUP_TIMEOUT_MILLISEC) {
						lookupThread.interrupt();
						resultHolder[0] = new LookUpTimeout();
						registerPingFailure(name);
						break;
					}
					Thread.sleep(10);
				}

				if (resultHolder[0] instanceof Throwable) {
					if (resultHolder[0] instanceof NotBoundException)
						throw (NotBoundException) resultHolder[0];
					else
						throw (RemoteException) resultHolder[0];
				}

				return (Remote) resultHolder[0];

			} else {
				throw new NotBoundException();
			}
		} catch (NotBoundException nbe) {
			throw nbe;
		} catch (AccessException ae) {
			throw ae;
		} catch (LookUpTimeout lue) {
			throw lue;
		} catch (LookUpInterrupted lui) {
			throw lui;
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				return lookup(name);
			} else {
				throw new RemoteException("", (sqle));
			}
		} catch (Exception e) {
			throw new RemoteException("", (e));
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			lock(stmt);
			rset = stmt.executeQuery("select count(*) from SERVANTS where NAME='" + name + "'");
			rset.next();
			if (rset.getInt(1) > 0) {
				stmt.execute("DELETE FROM SERVANTS WHERE NAME='" + name + "'");
			}
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				rebind(name, obj);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}

		stmt = null;
		String stub_hex = stubToHex(obj);

		try {

			stmt = _connection.createStatement();
			stmt
					.execute("Insert into SERVANTS (NAME,STUB_HEX,IN_USE,PING_FAILURES,REGISTER_TIME,REGISTER_PROCESS_ID,REGISTER_HOST_NAME,REGISTER_HOST_IP,REGISTER_OS,CODEBASE) "
							+ "values ('"
							+ name
							+ "','"
							+ stub_hex
							+ "',0,0,"
							+ sysdateFunctionName()
							+ ",'"
							+ getProcessId()
							+ "','"
							+ getHostName()
							+ "','"
							+ getHostIp()
							+ "','"
							+ System.getProperty("os.name")
							+ "',"
							+ (System.getProperty("java.rmi.server.codebase") == null ? "NULL" : "'" + System.getProperty("java.rmi.server.codebase") + "'")
							+ ")");

		} catch (SQLException sqle) {
			throw new RemoteException("", (sqle));
		} finally {
			try {
				if (stmt != null) {
					unlock(stmt);
					_connection.commit();
				}
			} catch (Exception e) {
				throw new RemoteException("", (e));
			}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void unbind(String name) throws RemoteException, NotBoundException, AccessException {

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			lock(stmt);
			stmt.execute("DELETE FROM SERVANTS WHERE NAME='" + name + "'");
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				unbind(name);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			try {
				if (stmt != null) {
					unlock(stmt);
					_connection.commit();
				}
			} catch (Exception e) {
				throw new RemoteException("", (e));
			}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public Vector<String> list(String[] prefixes) throws RemoteException, AccessException {
		Vector<String> result = new Vector<String>();
		Statement stmt = null;
		ResultSet rset = null;
		try {

			stmt = _connection.createStatement();

			String stmtStr = "select NAME from SERVANTS where IN_USE=0 AND PING_FAILURES<" + PoolUtils.PING_FAILURES_NBR_MAX + " AND (NAME like '"
					+ prefixes[0] + "%'";
			for (int i = 1; i < prefixes.length; ++i)
				stmtStr += " OR NAME like '" + prefixes[i] + "%'";
			stmtStr += ")";

			rset = stmt.executeQuery(stmtStr);

			while (rset.next()) {
				result.add(rset.getString(1));
			}
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				return list(prefixes);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
		return result;
	}

	public Vector<HashMap<String, Object>> listKillable() throws RemoteException, AccessException {
		return getTableData("SERVANTS", "PING_FAILURES>=" + PoolUtils.PING_FAILURES_NBR_MAX);
	}

	public Vector<HashMap<String, Object>> listKillable(String nodeIp, String nodePrefix) throws RemoteException, AccessException {
		return getTableData("SERVANTS", "PING_FAILURES>=" + PoolUtils.PING_FAILURES_NBR_MAX + " AND " + "REGISTER_HOST_IP='" + nodeIp + "' AND NAME like '"
				+ nodePrefix + "%'");
	}

	public void lock() throws RemoteException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			lock(stmt);
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				lock();
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void unlock() throws RemoteException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			unlock(stmt);
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				unlock();
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void reserve(String name) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("UPDATE SERVANTS SET IN_USE=1, BORROW_TIME=" + sysdateFunctionName() + ",BORROW_HOST_NAME='" + getHostName() + "'"
					+ ",BORROW_HOST_IP='" + getHostIp() + "'" + ",BORROW_PROCESS_ID='" + getProcessId() + "'" + ",BORROW_SESSION_INFO_HEX="
					+ (RPFSessionInfo.get() == null ? "NULL" : "'" + objectToHex(RPFSessionInfo.get()) + "'") + ",RETURN_TIME=NULL" + ",RETURN_HOST_NAME=NULL"
					+ ",RETURN_HOST_IP=NULL" + ",RETURN_PROCESS_ID=NULL" + " WHERE NAME='" + name + "'");
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				reserve(name);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void unReserve(String name) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("UPDATE SERVANTS SET IN_USE=0  " + ",RETURN_TIME=" + sysdateFunctionName() + ",RETURN_HOST_NAME='" + getHostName() + "'"
					+ ",RETURN_HOST_IP='" + getHostIp() + "'" + ",RETURN_PROCESS_ID='" + getProcessId() + "'" + " WHERE NAME='" + name + "'");
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				unReserve(name);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void registerPingFailure(String name) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("update SERVANTS SET PING_FAILURES=(PING_FAILURES+1) WHERE NAME='" + name + "'");
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				registerPingFailure(name);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void incrementNodeProcessCounter(String nodeName) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("update NODE_DATA set PROCESS_COUNTER=(PROCESS_COUNTER+1) WHERE NODE_NAME='" + nodeName + "'");
			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				incrementNodeProcessCounter(nodeName);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void updateServantNodeName(String servantName, String nodeName) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("update SERVANTS SET NODE_NAME='" + nodeName + "' WHERE NAME='" + servantName + "'");
			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				updateServantNodeName(servantName, nodeName);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void updateServantAttributes(String servantName, HashMap<String, Object> attributes) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("update SERVANTS SET ATTRIBUTES_HEX='" + PoolUtils.objectToHex(attributes) + "' WHERE NAME='" + servantName + "'");
			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				updateServantAttributes(servantName, attributes);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void commit() throws SQLException {
		_connection.commit();
	}

	public Remote getRemoteObject(String stub, String codeBaseStr) throws RemoteException {
		try {
			ClassLoader cl = null;
			if (codeBaseStr != null) {
				cl = new URLClassLoader(new URL[] { new URL(codeBaseStr) }, DBLayer.class.getClassLoader());
			}
			return hexToStub(stub, cl);
		} catch (Exception e) {
			throw new RemoteException("", (e));
		}
	}

	public HashMap<String, PoolDataDB> getPoolDataHashMap() throws RemoteException, AccessException {
		HashMap<String, PoolDataDB> result = new HashMap<String, PoolDataDB>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			rset = stmt.executeQuery("select POOL_NAME,POOL_PREFIXES,TIMEOUT from POOL_DATA");
			while (rset.next()) {
				result.put(rset.getString(1), new PoolDataDB(rset.getString(1), getPrefixes(rset.getString(2)), rset.getInt(3)));
			}
		} catch (SQLException sqle) {
			throw new RemoteException("", (sqle));
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
		return result;
	}

	public static String[] getPrefixes(String prefixes) {
		StringTokenizer st = new StringTokenizer(prefixes, ",");
		Vector<String> pv = new Vector<String>();
		while (st.hasMoreElements())
			pv.add((String) st.nextElement());
		return pv.toArray(new String[0]);
	}

	public Vector<PoolDataDB> getPoolData() throws RemoteException, AccessException {
		Vector<HashMap<String, Object>> nodeTable = getTableData("POOL_DATA");
		Vector<PoolDataDB> result = new Vector<PoolDataDB>();
		for (HashMap<String, Object> hm : nodeTable) {
			result.add(new PoolDataDB((String) hm.get("POOL_NAME"), getPrefixes((String) hm.get("POOL_PREFIXES")), (Integer) hm.get("TIMEOUT")));
		}
		return result;
	}

	public Vector<NodeDataDB> getNodeData(String condition) throws RemoteException, AccessException {
		Vector<HashMap<String, Object>> nodeTable = getTableData("NODE_DATA", condition);
		Vector<NodeDataDB> result = new Vector<NodeDataDB>();
		for (HashMap<String, Object> hm : nodeTable) {
			String pwd = (String) hm.get("PWD");
			if (!pwd.equals("")) {
				pwd = decipherPwd(pwd);
			}
			result.add(new NodeDataDB((String) hm.get("NODE_NAME"), (String) hm.get("HOST_IP"), (String) hm.get("HOST_NAME"), (String) hm.get("LOGIN"), pwd,
					(String) hm.get("INSTALL_DIR"), (String) hm.get("CREATE_SERVANT_COMMAND"), (String) hm.get("KILL_SERVANT_COMMAND"), (String) hm.get("OS"),
					(Integer) hm.get("SERVANT_NBR_MIN"), (Integer) hm.get("SERVANT_NBR_MAX"), (String) hm.get("POOL_PREFIX"), (Integer) hm
							.get("PROCESS_COUNTER")));
		}
		return result;
	}

	public void addNode(NodeDataDB nodeData) throws RemoteException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt
					.execute("INSERT INTO NODE_DATA(NODE_NAME, HOST_IP,HOST_NAME,LOGIN,PWD,INSTALL_DIR,CREATE_SERVANT_COMMAND, KILL_SERVANT_COMMAND ,OS,SERVANT_NBR_MIN,SERVANT_NBR_MAX,POOL_PREFIX, PROCESS_COUNTER) values ("
							+ "'"
							+ nodeData.getNodeName()
							+ "',"
							+ "'"
							+ nodeData.getHostIp()
							+ "',"
							+ "'"
							+ nodeData.getHostName()
							+ "',"
							+ "'"
							+ nodeData.getLogin()
							+ "',"
							+ "'"
							+ (nodeData.getPwd().trim().equals("") ? "" : cipherPwd(nodeData.getPwd()))
							+ "',"
							+ "'"
							+ nodeData.getInstallDir()
							+ "',"
							+ "'"
							+ nodeData.getCreateServantCommand()
							+ "',"
							+ "'"
							+ nodeData.getKillServantCommand()
							+ "',"
							+ "'"
							+ nodeData.getOS()
							+ "',"
							+ nodeData.getServantNbrMin()
							+ ","
							+ nodeData.getServantNbrMax()
							+ ","
							+ "'"
							+ nodeData.getPoolPrefix() + "'" + ",0" + ")");

			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				addNode(nodeData);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void addPool(PoolDataDB poolData) throws RemoteException {
		Statement stmt = null;
		try {
			String prefixes = "";
			for (int i = 0; i < poolData.getPrefixes().length; ++i)
				prefixes += poolData.getPrefixes()[i] + (i == poolData.getPrefixes().length - 1 ? "" : ",");
			stmt = _connection.createStatement();
			stmt.execute("INSERT INTO POOL_DATA(POOL_NAME, TIMEOUT,POOL_PREFIXES) values (" + "'" + poolData.getPoolName() + "'," + poolData.getBorrowTimeout()
					+ "," + "'" + prefixes + "')");

			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				addPool(poolData);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void updateNode(NodeDataDB nodeData) throws RemoteException {
		Statement stmt = null;

		String ip = nodeData.getHostIp().trim();
		String host = nodeData.getHostName().trim();
		String prefix = nodeData.getPoolPrefix().trim();
		String nodeName = nodeData.getNodeName();

		if (ip.equals("")) {
			try {
				ip = InetAddress.getByName(nodeData.getHostName()).getHostAddress();
			} catch (Exception e) {
				throw new RemoteException("", (e));
			}
		}

		try {
			stmt = _connection.createStatement();
			String updateStr = "UPDATE NODE_DATA set " + " HOST_IP=" + "'" + ip + "'," + " HOST_NAME=" + "'" + host + "'," + " POOL_PREFIX=" + "'" + prefix
					+ "'," + " LOGIN=" + "'" + nodeData.getLogin() + "'," + " PWD=" + "'"
					+ (nodeData.getPwd().trim().equals("") ? "" : cipherPwd(nodeData.getPwd())) + "'," + " INSTALL_DIR=" + "'" + nodeData.getInstallDir()
					+ "'," + " CREATE_SERVANT_COMMAND=" + "'" + nodeData.getCreateServantCommand() + "'," + " KILL_SERVANT_COMMAND=" + "'"
					+ nodeData.getKillServantCommand() + "'," + " OS=" + "'" + nodeData.getOS() + "'," + " SERVANT_NBR_MIN=" + nodeData.getServantNbrMin()
					+ "," + " SERVANT_NBR_MAX=" + nodeData.getServantNbrMax() + "" + " where NODE_NAME='" + nodeName + "'";
			System.out.println(updateStr);
			stmt.execute(updateStr);
			_connection.commit();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			if (isNoConnectionError(sqle) && canReconnect()) {
				updateNode(nodeData);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void removeNode(String nodeName) throws RemoteException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			String deleteStr = "delete from NODE_DATA where NODE_NAME='" + nodeName + "'";
			stmt.execute(deleteStr);
			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				removeNode(nodeName);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void removePool(String poolName) throws RemoteException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			String deleteStr = "delete from POOL_DATA where POOL_NAME='" + poolName + "'";
			stmt.execute(deleteStr);
			_connection.commit();
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				removePool(poolName);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void updatePool(PoolDataDB poolData) throws RemoteException {
		Statement stmt = null;

		String prefixes = "";
		for (int i = 0; i < poolData.getPrefixes().length; ++i)
			prefixes += poolData.getPrefixes()[i] + (i == poolData.getPrefixes().length - 1 ? "" : ",");

		try {
			stmt = _connection.createStatement();
			String updateStr = "UPDATE POOL_DATA set TIMEOUT=" + poolData.getBorrowTimeout() + "," + " POOL_PREFIXES='" + prefixes + "' where POOL_NAME='"
					+ poolData.getPoolName() + "'";
			System.out.println(updateStr);
			stmt.execute(updateStr);
			_connection.commit();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			if (isNoConnectionError(sqle) && canReconnect()) {
				updatePool(poolData);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public void unlockServant(String servantName) throws RemoteException {
		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.execute("UPDATE SERVANTS SET IN_USE=0, PING_FAILURES=0" + ",BORROW_HOST_NAME=NULL" + ",BORROW_HOST_IP=NULL" + ",BORROW_PROCESS_ID=NULL"
					+ ",BORROW_SESSION_INFO_HEX=NULL" + ",RETURN_TIME=" + sysdateFunctionName() + ",RETURN_HOST_NAME='" + getHostName() + "'"
					+ ",RETURN_HOST_IP='" + getHostIp() + "'" + ",RETURN_PROCESS_ID='" + getProcessId() + "'" + " WHERE NAME='" + servantName + "'");

		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				unlockServant(servantName);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public Vector<HashMap<String, Object>> getTableData(String tableName) throws RemoteException {
		return getTableData(tableName, null);
	}

	public Vector<HashMap<String, Object>> getTableData(String tableName, String condition) throws RemoteException {
		Vector<HashMap<String, Object>> result = new Vector<HashMap<String, Object>>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			rset = stmt.executeQuery("select * from " + tableName + (condition == null || condition.equals("") ? "" : " WHERE " + condition));
			while (rset.next()) {
				HashMap<String, Object> hm = new HashMap<String, Object>();
				for (int i = 1; i <= rset.getMetaData().getColumnCount(); ++i) {
					hm.put(rset.getMetaData().getColumnName(i), rset.getObject(i));
				}
				result.add(hm);
			}
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				return getTableData(tableName, condition);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
		return result;
	}

	public String getNameFromStub(Remote stub) throws RemoteException, AccessException {

		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baoStream).writeObject(stub);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String stub_hex = bytesToHex(baoStream.toByteArray());

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			rset = stmt.executeQuery("select NAME from SERVANTS where STUB_HEX='" + stub_hex + "'");
			if (!rset.next())
				throw new RemoteException("no corresponding servant in DB");
			return rset.getString(1);

		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				return getNameFromStub(stub);
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}

	}

	public void unregisterAll() throws RemoteException, NotBoundException, AccessException {
		if (getProcessId().equals(UNKOWN))
			return;

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();
			lock(stmt);
			stmt.execute("UPDATE SERVANTS SET IN_USE=0  " + ",RETURN_TIME=" + sysdateFunctionName() + ",RETURN_HOST_NAME='" + getHostName() + "'"
					+ ",RETURN_HOST_IP='" + getHostIp() + "'" + ",RETURN_PROCESS_ID='" + getProcessId() + "'" + " WHERE BORROW_HOST_IP='" + getHostIp()
					+ "' AND BORROW_PROCESS_ID='" + getProcessId() + "'");
		} catch (SQLException sqle) {
			if (isNoConnectionError(sqle) && canReconnect()) {
				unregisterAll();
			} else {
				throw new RemoteException("", (sqle));
			}
		} finally {
			try {
				if (stmt != null) {
					unlock(stmt);
					_connection.commit();
				}
			} catch (Exception e) {
				throw new RemoteException("", (e));
			}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public static String replaceCode(String s) {
		int p1 = 0;
		while ((p1 = s.indexOf("<%=")) != -1) {
			int p2 = s.indexOf("%>", p1 + 3);
			String expression = s.substring(p1 + 3, p2);
			String className = expression.substring(0, expression.lastIndexOf('.'));
			String functionName = expression.substring(expression.lastIndexOf('.') + 1, expression.lastIndexOf("()"));
			String replaceWith = "ERROR";
			try {
				replaceWith = (String) Class.forName(className).getMethod(functionName, (Class[]) null).invoke(null, (Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			s = s.substring(0, p1) + replaceWith + s.substring(p2 + 2);
		}
		return s;
	}

	public void applyDBScript(InputStream scriptInputStream) throws RemoteException, NotBoundException, AccessException {
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = _connection.createStatement();

			BufferedReader br = new BufferedReader(new InputStreamReader(scriptInputStream));
			String line = null;
			StringBuffer sbuffer = new StringBuffer();
			try {
				while ((line = br.readLine()) != null) {
					sbuffer.append(line.trim());
					sbuffer.append(" ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			StringTokenizer st = new StringTokenizer(replaceCode(sbuffer.toString()), ";");
			while (st.hasMoreElements()) {
				String statmentStr = ((String) st.nextElement()).trim();
				if (statmentStr.equals(""))
					continue;

				System.out.println("<" + statmentStr + ">");

				try {
					if (statmentStr.trim().equalsIgnoreCase("commit")) {
						_connection.commit();
					} else {
						stmt.execute(statmentStr);
					}
					System.out.println("OK");
				} catch (SQLException sqle) {
					if (statmentStr.toUpperCase().startsWith("DROP")) {
						System.out.println("NOK / " + statmentStr + " Failed ");
					} else {
						sqle.printStackTrace();
					}
				}

			}
		} catch (SQLException sqle) {

			if (isNoConnectionError(sqle) && canReconnect()) {
				applyDBScript(scriptInputStream);
			} else {
				throw new RemoteException("", (sqle));
			}

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
			if (rset != null)
				try {
					stmt.close();
				} catch (Exception e) {
					throw new RemoteException("", (e));
				}
		}
	}

	public boolean canReconnect() {
		if (_connectionProvider == null)
			return false;
		_log.info("try to reconnect");

		Statement stmt = null;
		try {
			stmt = _connection.createStatement();
			stmt.executeQuery("select POOL_NAME from POOL_DATA");
			_log.info("reconnection aborted, connection was up");
			return false;
		} catch (SQLException sqle) {

		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}

		Connection connection = null;
		try {
			connection = _connectionProvider.newConnection();
			stmt = connection.createStatement();
			stmt.executeQuery("select POOL_NAME from POOL_DATA");
		} catch (SQLException sqle) {
			_log.info("reconnection failed");
			return false;
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					return false;
				}
		}
		_connection = connection;

		_log.info("reconnection succeeded");
		return true;
	}

	public void setConnectionProvider(ConnectionProvider connectionProvider) {
		this._connectionProvider = connectionProvider;
	}

	public static Registry _registry = null;
	public static Integer _lock = new Integer(0);

	public static Registry getRmiRegistry() throws Exception {

		if (_registry != null)
			return _registry;
		synchronized (_lock) {
			if (_registry == null) {
				final String dburl = System.getProperty("db.url");
				if (dburl != null && !dburl.equals("")) {
					final String user = System.getProperty("db.user");
					final String password = System.getProperty("db.password");
					Class.forName(System.getProperty("db.driver"));
					_registry = getLayer(PoolUtils.getDBType(dburl), new ConnectionProvider() {
						public Connection newConnection() throws java.sql.SQLException {
							return DriverManager.getConnection(dburl, user, password);
						};

					});
				} else {
					_registry = LocateRegistry.getRegistry(_registryHost, _registryPort);
				}
			}
			return _registry;
		}

	}

	public static DBLayer getLayer(String dbtype, Connection conn) throws Exception {
		String className = "uk.ac.ebi.microarray.pools.db.DBLayer" + ("" + dbtype.charAt(0)).toUpperCase() + dbtype.substring(1);
		return (DBLayer) Class.forName(className).getConstructor(new Class[] { Connection.class }).newInstance(new Object[] { conn });
	}

	public static DBLayer getLayer(String dbtype, ConnectionProvider connProvider) throws Exception {
		DBLayer result = getLayer(dbtype, connProvider.newConnection());
		result.setConnectionProvider(connProvider);
		return result;
	}

	static String _pwdKey = "800761F89437B3B0F47F753792A7D69E49E65B5191D52652";
	static SecretKey _key = null;

	static SecretKey getSecretKey() {
		try {
			DESedeKeySpec keyspec = new DESedeKeySpec(PoolUtils.hexToBytes(_pwdKey));
			SecretKeyFactory desEdeFactory = SecretKeyFactory.getInstance("DESede");
			SecretKey k = desEdeFactory.generateSecret(keyspec);
			return k;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static String cipherPwd(String pwd) {
		try {
			Cipher cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
			byte[] ciphertext = cipher.doFinal(PoolUtils.objectToBytes(pwd));
			return PoolUtils.bytesToHex(ciphertext);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static String decipherPwd(String cipheredpwd) {
		try {
			Cipher cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
			return (String) PoolUtils.bytesToObject(cipher.doFinal(PoolUtils.hexToBytes(cipheredpwd)));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}