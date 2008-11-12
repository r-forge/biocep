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
	
	private boolean _defaultR;
	private String _defaultRBin;

	private String _sshHostIp;
	private Integer _sshPort;
	private String _sshLogin;
	private String _sshPwd;
	
	private boolean _useSshTunnel;
	private String _sshTunnelHostIp;
	private Integer _sshTunnelPort;
	private String _sshTunnelLogin;
	private String _sshTunnelPwd;
	
	private String _privateName;

	public Identification(int mode, String url, String user, String pwd, boolean nopool, boolean waitForResource, String privateName, int rmiMode, String rmiregistryIp,
			Integer rmiregistryPort, String servantName, String dbDriver, String dbHostIp, Integer dbHostPort, String dbName, String dbUser, String dbPwd,
			String dbServantName, String stub, int memoryMin, int memoryMax,

			boolean keepAlive, 
			
			boolean useSsh, 
			
			boolean defaultR,
			String defaultRBin,
			
			String sshHostIp, int sshPort, String sshLogin, String sshPwd,
			
			boolean useSshTunnel,	
			String sshTunnelHostIp, int sshTunnelPort, String sshTunnelLogin, String sshTunnelPwd	
		) {
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
		
		this._defaultR=defaultR;
		if (!defaultR) this._defaultRBin=defaultRBin;
		else this._defaultRBin=null;
		
		this._sshHostIp = sshHostIp;
		this._sshPort = sshPort;
		this._sshLogin = sshLogin;
		this._sshPwd = sshPwd;
		
		this._useSshTunnel = useSshTunnel;
		this._sshTunnelHostIp = sshTunnelHostIp;
		this._sshTunnelPort = sshTunnelPort;
		this._sshTunnelLogin = sshTunnelLogin;
		this._sshTunnelPwd = sshTunnelPwd;
		
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

	
	public boolean isDefaultR() {
		return _defaultR;
	}

	public String getDefaultRBin() {
		return _defaultRBin;
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
	
	
	public boolean isUseSshTunnel() {
		return _useSshTunnel;
	}

	public String getSshTunnelHostIp() {
		return _sshTunnelHostIp;
	}

	public Integer getSshTunnelPort() {
		return _sshTunnelPort;
	}
	
	public String getSshTunnelLogin() {
		return _sshTunnelLogin;
	}

	public String getSshTunnelPwd() {
		return _sshTunnelPwd;
	}
	
}
