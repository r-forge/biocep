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

import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;

import java.io.File;
import java.net.URL;

import uk.ac.ebi.microarray.pools.PoolUtils.EqualNameFilter;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class MainPsToolsDownload {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String location=null;
		if (System.getProperty("location")!=null && !System.getProperty("location").equals("")) {
			location = System.getProperty("location");
		} else {
			location=args[0];
		}
		if (!location.endsWith("/") && !location.endsWith("\\")) location += "/";

		try {
			if (isWindowsOs()
					&& (!new File(location + "pskill.exe").exists() || !new File(location + "pslist.exe").exists() || !new File(location + "psexec.exe").exists())) {
				unzip(new URL("http://download.sysinternals.com/Files/PsTools.zip").openConnection().getInputStream(),
						location, new EqualNameFilter("pslist.exe", "pskill.exe", "psexec.exe"), 1024 * 16, true,
						"Unzipping psTools..", 3);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
