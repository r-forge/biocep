package uk.ac.ebi.microarray.pools.db;
import java.io.InputStream;
import java.rmi.AccessException;
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
	
}
