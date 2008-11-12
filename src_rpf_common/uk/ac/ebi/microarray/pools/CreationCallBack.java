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
package uk.ac.ebi.microarray.pools;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class CreationCallBack extends UnicastRemoteObject implements ServantCreationListener {
	Remote[] _managedServantHolder;
	RemoteException[] _remoteExceptionHolder;

	public CreationCallBack(Remote[] managedServantHolder, RemoteException[] remoteExceptionHolder) throws RemoteException {
		super();
		_managedServantHolder = managedServantHolder;
		_remoteExceptionHolder = remoteExceptionHolder;
	}

	public void setServantStub(Remote servant) throws RemoteException {
		System.out.println("received:" + PoolUtils.stubToHex(servant));
		_managedServantHolder[0] = servant;
	}

	public void setRemoteException(RemoteException remoteException) throws RemoteException {
		System.out.println("received:" + PoolUtils.getStackTraceAsString(remoteException));
		_remoteExceptionHolder[0] = remoteException;
	}

}