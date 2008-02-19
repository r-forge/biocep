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
package graphics.rmi;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
class Identification {

	private int _mode;
	
	private String _url;
	private String _user;
	private String _pwd;	
	private boolean _nopool;
	private boolean _waitForResource;	
	
	private String _rmiregistryIp;
	private Integer _rmiregistryPort;
	private String _servantName;
	private String _stub;
	
	private String _memoryMax;

	private boolean _persistentWorkspace;	
	private boolean _playDemo;

	public Identification(int mode, 
			String url, String user, String pwd,  boolean nopool, boolean waitForResource,  
			String rmiregistryIp, Integer rmiregistryPort, String servantName, String stub, 
			String memoryMax,
			boolean persistentWorkspace, boolean playDemo) {		
		this._mode=mode;	
		this._url=url;
		this._user = user;
		this._pwd = pwd;
		this._nopool = nopool;		
		this._waitForResource = waitForResource;
		this._rmiregistryIp=rmiregistryIp;
		this._rmiregistryPort=rmiregistryPort;
		this._servantName=servantName;		
		this._stub=stub;		
		this._memoryMax=memoryMax;	
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

	public String getMemoryMax() {
		return _memoryMax;
	}

	public boolean isPersistentWorkspace() {
		return _persistentWorkspace;
	}

	public boolean isPlayDemo() {
		return _playDemo;
	}	
}
