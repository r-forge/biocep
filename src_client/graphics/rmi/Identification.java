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

	private String _user;
	private String _pwd;
	private boolean _persistentWorkspace;
	private boolean _nopool;
	private boolean _waitForResource;
	private boolean _playDemo;
	

	public Identification(String user, String pwd, boolean persistentWorkspace, boolean nopool,
			boolean waitForResource, boolean playDemo) {
		this._user = user;
		this._pwd = pwd;
		this._persistentWorkspace = persistentWorkspace;
		this._nopool = nopool;
		this._waitForResource = waitForResource;
		this._playDemo = playDemo;
	}

	public String getUser() {
		return _user;
	}

	public String getPwd() {
		return _pwd;
	}

	public boolean isPersistentWorkspace() {
		return _persistentWorkspace;
	}

	public boolean isNopool() {
		return _nopool;
	}

	public boolean isWaitForResource() {
		return _waitForResource;
	}

	public boolean isPlayDemo() {
		return _playDemo;
	}
}
