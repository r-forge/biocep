/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
package graphics.rmi;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
class Identification {

	private int _mode;

	private String _url;
	private String _user;
	private String _pwd;
	private boolean _nopool;
	private boolean _waitForResource;

	private int _rmiMode;

	private String _rmiregistryIp;
	private Integer _rmiregistryPort;
	private String _servantName;

	private String _dbDriver;
	private String _dbHostIp;
	private Integer _dbHostPort;
	private String _dbName;
	private String _dbUser;
	private String _dbPwd;
	private String _dbServantName;

	private String _stub;

	private int _memoryMin;
	private int _memoryMax;
	private boolean _keepAlive;
	private boolean _useSsh;
	private String _sshHostIp;
	private Integer _sshPort;
	private String _sshLogin;
	private String _sshPwd;

	private boolean _persistentWorkspace;
	private boolean _playDemo;
	
	private String _privateName;

	public Identification(int mode, String url, String user, String pwd, boolean nopool, boolean waitForResource, String privateName, int rmiMode, String rmiregistryIp,
			Integer rmiregistryPort, String servantName, String dbDriver, String dbHostIp, Integer dbHostPort, String dbName, String dbUser, String dbPwd,
			String dbServantName, String stub, int memoryMin, int memoryMax,

			boolean keepAlive, boolean useSsh, String sshHostIp, int sshPort, String sshLogin, String sshPwd,

			boolean persistentWorkspace, boolean playDemo) {
		this._mode = mode;
		this._url = url;
		this._user = user;
		this._pwd = pwd;
		this._nopool = nopool;
		this._waitForResource = waitForResource;
		this._privateName=privateName;		
		this._rmiMode = rmiMode;
		this._rmiregistryIp = rmiregistryIp;
		this._rmiregistryPort = rmiregistryPort;
		this._servantName = servantName;

		this._dbDriver = dbDriver;

		this._dbHostIp = dbHostIp;
		this._dbHostPort = dbHostPort;
		this._dbName = dbName;

		this._dbUser = dbUser;
		this._dbPwd = dbPwd;
		this._dbServantName = dbServantName;

		this._stub = stub;
		this._memoryMin = memoryMin;
		this._memoryMax = memoryMax;
		this._keepAlive = keepAlive;
		this._useSsh = useSsh;
		this._sshHostIp = sshHostIp;
		this._sshPort = sshPort;
		this._sshLogin = sshLogin;
		this._sshPwd = sshPwd;
		this._persistentWorkspace = persistentWorkspace;
		this._playDemo = playDemo;
	}

	public int getMode() {
		return _mode;
	}

	public String getUrl() {
		return _url;
	}

	public String getUser() {
		return _user;
	}

	public String getPwd() {
		return _pwd;
	}

	public boolean isNopool() {
		return _nopool;
	}

	public boolean isWaitForResource() {
		return _waitForResource;
	}

	public String getRmiregistryIp() {
		return _rmiregistryIp;
	}

	public Integer getRmiregistryPort() {
		return _rmiregistryPort;
	}

	public String getServantName() {
		return _servantName;
	}

	public String getStub() {
		return _stub;
	}

	public int getMemoryMax() {
		return _memoryMax;
	}

	public boolean isPersistentWorkspace() {
		return _persistentWorkspace;
	}

	public boolean isPlayDemo() {
		return _playDemo;
	}

	public int getRmiMode() {
		return _rmiMode;
	}

	public String getDbDriver() {
		return _dbDriver;
	}

	public String getDbUser() {
		return _dbUser;
	}

	public String getDbServantName() {
		return _dbServantName;
	}

	public String getDbPwd() {
		return _dbPwd;
	}

	public String getDbHostIp() {
		return _dbHostIp;
	}

	public Integer getDbHostPort() {
		return _dbHostPort;
	}

	public String getDbName() {
		return _dbName;
	}

	public int getMemoryMin() {
		return _memoryMin;
	}

	public boolean isKeepAlive() {
		return _keepAlive;
	}

	public boolean isUseSsh() {
		return _useSsh;
	}

	public String getSshHostIp() {
		return _sshHostIp;
	}

	public Integer getSshPort() {
		return _sshPort;
	}
	
	public String getSshLogin() {
		return _sshLogin;
	}

	public String getSshPwd() {
		return _sshPwd;
	}

	public String getPrivateName() {
		return _privateName;
	}
}
