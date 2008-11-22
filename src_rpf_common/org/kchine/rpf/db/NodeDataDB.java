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

import java.io.Serializable;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class NodeDataDB implements Serializable{
	private String _nodeName;
	private String _hostIp;
	private String _hostName;
	private String _login;
	private String _pwd;
	private String _installDir;
	private String _createServantCommand;
	private String _killServantCommand;
	private String _os;
	private int _servantNbrMin;
	private int _servantNbrMax;
	private String _poolPrefix;
	private int _processCounter;

	public NodeDataDB(String _nodeName, String _host_ip, String host_name, String _login, String _pwd, String _install_dir, String create_servant_command,
			String kill_servant_command, String os, int servant_nbr_min, int servant_nbr_max, String _pool_prefix, int _processCounter) {
		super();
		this._nodeName = _nodeName;
		this._hostIp = _host_ip;
		this._hostName = host_name;
		this._login = _login;
		this._pwd = _pwd;
		this._installDir = _install_dir;
		this._createServantCommand = create_servant_command;
		this._killServantCommand = kill_servant_command;
		this._os = os;
		this._servantNbrMin = servant_nbr_min;
		this._servantNbrMax = servant_nbr_max;
		this._poolPrefix = _pool_prefix;
		this._processCounter = _processCounter;
	}

	public String getHostIp() {
		return _hostIp;
	}

	public String getInstallDir() {
		return _installDir;
	}

	public String getLogin() {
		return _login;
	}

	public int getServantNbrMin() {
		return _servantNbrMin;
	}

	public int getServantNbrMax() {
		return _servantNbrMax;
	}

	public String getPoolPrefix() {
		return _poolPrefix;
	}

	public String getPwd() {
		return _pwd;
	}

	public String getCreateServantCommand() {
		return _createServantCommand;
	}

	public String getKillServantCommand() {
		return _killServantCommand;
	}

	public String getNodeName() {
		return _nodeName;
	}

	public String getHostName() {
		return _hostName;
	}

	public String getOS() {
		return _os;
	}

	public int getProcessCounter() {
		return _processCounter;
	}

	public String toString() {
		return "Name=" + _nodeName + " prefix=" + _poolPrefix;
	}
}
