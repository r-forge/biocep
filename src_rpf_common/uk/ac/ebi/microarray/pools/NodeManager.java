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
package uk.ac.ebi.microarray.pools;

import java.rmi.RemoteException;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface NodeManager extends ManagedServant {
	ManagedServant createPrivateServant(String nodeName) throws RemoteException;

	void createServant(String nodeName) throws RemoteException;

	void kill(ManagedServant servant) throws RemoteException;

	public void kill(String servantName) throws RemoteException;

	public void die(ManagedServant servant) throws RemoteException;

	public void incrementPingFailure(ManagedServant servant) throws RemoteException;

	public String[] getNodeNames() throws RemoteException;
}
