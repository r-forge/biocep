package org.kchine.openoffice.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.RemotePanel;

public class OpenOfficeServicesImpl implements OpenOfficeServices {

	public void convertFile(String inputFile,  String outputFile, String conversionFilter, boolean useserver) throws RemoteException {
		
	}

	public void addErrListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public ManagedServant cloneServer() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String consoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void die() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostIp() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJobId() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLogs() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProcessId() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServantName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStub() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getSystemEnv() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getSystemProperties() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasConsoleMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasGraphicMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPushPopMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBusy() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isResetEnabled() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] listSymbols() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void logInfo(String message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String ping() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable pop(String symbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeAllErrListeners() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeAllOutListeners() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeErrListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeOutListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void reset() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setJobId(String jobId) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
