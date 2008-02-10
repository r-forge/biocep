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

import java.rmi.RemoteException;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class DieTimeout extends RemoteException {

	public DieTimeout() {
		super();
	}

	public DieTimeout(String s, Throwable cause) {
		super(s, cause);
	}

	public DieTimeout(String s) {
		super(s);
	}

}
