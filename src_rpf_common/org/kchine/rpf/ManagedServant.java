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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface ManagedServant extends java.rmi.Remote {

	public void reset() throws java.rmi.RemoteException;

	public String ping() throws java.rmi.RemoteException;

	public void die() throws java.rmi.RemoteException;

	public String getLogs() throws java.rmi.RemoteException;

	public String getServantName() throws java.rmi.RemoteException;

	public void addOutListener(RemoteLogListener listener) throws java.rmi.RemoteException;

	public void removeOutListener(RemoteLogListener listener) throws java.rmi.RemoteException;

	public void removeAllOutListeners() throws java.rmi.RemoteException;

	public void addErrListener(RemoteLogListener listener) throws java.rmi.RemoteException;

	public void removeErrListener(RemoteLogListener listener) throws java.rmi.RemoteException;

	public void removeAllErrListeners() throws java.rmi.RemoteException;

	public void logInfo(String message) throws java.rmi.RemoteException;

	public boolean isResetEnabled() throws java.rmi.RemoteException;

	public void setResetEnabled(boolean enable) throws java.rmi.RemoteException;

	public boolean hasConsoleMode() throws java.rmi.RemoteException;

	public String consoleSubmit(String cmd) throws java.rmi.RemoteException;
	
	public String getStatus() throws RemoteException;

	public void asynchronousConsoleSubmit(String cmd) throws java.rmi.RemoteException;

	public boolean isBusy() throws java.rmi.RemoteException;

	public boolean hasPushPopMode() throws java.rmi.RemoteException;

	public Serializable pop(String symbol) throws java.rmi.RemoteException;

	public void push(String symbol, Serializable object) throws java.rmi.RemoteException;

	public String[] listSymbols() throws java.rmi.RemoteException;

	public boolean hasGraphicMode() throws java.rmi.RemoteException;

	public RemotePanel getPanel(int w, int h) throws java.rmi.RemoteException;

	public String getProcessId() throws java.rmi.RemoteException;

	public String getHostIp() throws java.rmi.RemoteException;
	
	public String getHostName() throws java.rmi.RemoteException;
	
	public String getJobId() throws java.rmi.RemoteException;
		
	public void setJobId(String jobId) throws java.rmi.RemoteException;
	
	public ManagedServant cloneServer() throws java.rmi.RemoteException;
	
	public String getStub() throws java.rmi.RemoteException;
	
	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException ;
	
	public Properties getSystemProperties() throws RemoteException ;
	
	public Map<String,String> getSystemEnv() throws RemoteException ;

}
