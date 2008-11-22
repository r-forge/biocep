/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
import java.io.InputStream;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

public interface DBLayerInterface extends Registry {
	public Vector<String> list(String[] prefixes) throws RemoteException, AccessException ;
	public Vector<HashMap<String, Object>> listKillable() throws RemoteException, AccessException;
	public Vector<HashMap<String, Object>> listKillable(String nodeIp, String nodePrefix) throws RemoteException, AccessException;
	public void lock() throws RemoteException, AccessException ;
	public void unlock() throws RemoteException, AccessException;
	public void reserve(String name) throws RemoteException, NotBoundException, AccessException;
	public void unReserve(String name) throws RemoteException, NotBoundException, AccessException;
	public void registerPingFailure(String name) throws RemoteException, NotBoundException, AccessException;
	public void incrementNodeProcessCounter(String nodeName) throws RemoteException, NotBoundException, AccessException;
	public void updateServantNodeName(String servantName, String nodeName) throws RemoteException, NotBoundException, AccessException;
	public void updateServantAttributes(String servantName, HashMap<String, Object> attributes) throws RemoteException, NotBoundException, AccessException;
	public void commit() throws SQLException;	public Remote getRemoteObject(String stub, String codeBaseStr) throws RemoteException;
	public HashMap<String, PoolDataDB> getPoolDataHashMap() throws RemoteException, AccessException;
	public Vector<PoolDataDB> getPoolData() throws RemoteException, AccessException;
	public Vector<NodeDataDB> getNodeData(String condition) throws RemoteException, AccessException;
	public void addNode(NodeDataDB nodeData) throws RemoteException;
	public void addPool(PoolDataDB poolData) throws RemoteException;
	public void updateNode(NodeDataDB nodeData) throws RemoteException;
	public void removeNode(String nodeName) throws RemoteException;
	public void removePool(String poolName) throws RemoteException;
	public void updatePool(PoolDataDB poolData) throws RemoteException;
	public void unlockServant(String servantName) throws RemoteException;
	public Vector<HashMap<String, Object>> getTableData(String tableName) throws RemoteException;
	public Vector<HashMap<String, Object>> getTableData(String tableName, String condition) throws RemoteException;
	public String getNameFromStub(Remote stub) throws RemoteException, AccessException;
	public void unregisterAll() throws RemoteException, NotBoundException, AccessException;	
	public void applyDBScript(InputStream scriptInputStream) throws RemoteException, NotBoundException, AccessException;
	public boolean canReconnect();	
	public void setJobID(String servantName, String jobID) throws RemoteException;
	public void setNotified(String servantName, boolean notified) throws RemoteException;
	
	public void bind(String name, Remote obj, HashMap<String, Object> options) throws RemoteException, AlreadyBoundException, AccessException ;
	public void rebind(String name, Remote obj, HashMap<String, Object> options) throws RemoteException, AccessException; 
	
}
