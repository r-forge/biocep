package org.kchine.r.workbench.dialogs;

public class DbInfo {
	public DbInfo(String dbDriver, String dbHostIp, Integer dbHostPort, String dbName, String dbUser, String dbPwd) {
		super();
		this.dbDriver = dbDriver;
		this.dbHostIp = dbHostIp;
		this.dbHostPort = dbHostPort;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
	}
	private String dbDriver;
	private String dbHostIp;
	private Integer dbHostPort;
	private String dbName;
	private String dbUser;
	private String dbPwd;
	public String getDbDriver() {
		return dbDriver;
	}
	public String getDbHostIp() {
		return dbHostIp;
	}
	public Integer getDbHostPort() {
		return dbHostPort;
	}
	public String getDbName() {
		return dbName;
	}
	public String getDbUser() {
		return dbUser;
	}
	public String getDbPwd() {
		return dbPwd;
	}
	
	public String toString() {
		return 		
		" this.dbDriver = "+dbDriver+
		" this.dbHostIp = "+dbHostIp+
		" this.dbHostPort = "+dbHostPort+
		" this.dbName = "+dbName+
		" this.dbUser = "+dbUser+
		" this.dbPwd = "+dbPwd;
	}
	


}
