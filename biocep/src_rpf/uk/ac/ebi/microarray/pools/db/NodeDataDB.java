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
package uk.ac.ebi.microarray.pools.db;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class NodeDataDB {
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

	public NodeDataDB(String _nodeName, String _host_ip, String host_name, String _login, String _pwd,
			String _install_dir, String create_servant_command, String kill_servant_command, String os,
			int servant_nbr_min, int servant_nbr_max, String _pool_prefix, int _processCounter) {
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
