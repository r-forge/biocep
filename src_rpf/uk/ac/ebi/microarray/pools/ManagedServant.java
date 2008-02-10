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

import java.io.Serializable;

/**
 * @author Karim Chine kchine@ebi.ac.uk
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

	public boolean hasPushPopMode() throws java.rmi.RemoteException;

	public Serializable pop(String symbol) throws java.rmi.RemoteException;

	public void push(String symbol, Serializable object) throws java.rmi.RemoteException;

	public String[] listSymbols() throws java.rmi.RemoteException;

	public boolean hasGraphicMode() throws java.rmi.RemoteException;

	public RemotePanel getPanel(int w, int h) throws java.rmi.RemoteException;
	
	public String getProcessId() throws java.rmi.RemoteException;

}
