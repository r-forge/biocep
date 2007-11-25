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
package http;

import java.io.File;

import remoting.RServices;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class UserUtils {
	public static String WKS_FILE_NAME = ".RData_biocep";

	public static void saveWorkspace(String login, RServices rservices) throws Exception {
		if (rservices != null && login != null && !login.equals("") && !login.equalsIgnoreCase("guest")) {
			File wdirFile = new File(System.getProperty("users.data.dir.root") + System.getProperty("file.separator")
					+ login);
			if (!wdirFile.exists()) {
				wdirFile.mkdirs();
			}
			rservices.consoleSubmit("save.image(file = '" + WKS_FILE_NAME + "')");
			FileLoad.download(WKS_FILE_NAME, new File(wdirFile.getAbsolutePath() + System.getProperty("file.separator")
					+ WKS_FILE_NAME), rservices);
			rservices.consoleSubmit("unlink('" + WKS_FILE_NAME + "')");
		}
	}

	public static void loadWorkspace(String login, RServices rservices) throws Exception {
		if (rservices != null && login != null && !login.equals("") && !login.equalsIgnoreCase("guest")) {
			File wdirFile = new File(System.getProperty("users.data.dir.root") + System.getProperty("file.separator")
					+ login);
			if (!wdirFile.exists())
				return;
			File f = new File(wdirFile.getAbsolutePath() + System.getProperty("file.separator") + WKS_FILE_NAME);
			if (!f.exists())
				return;
			FileLoad.upload(f, WKS_FILE_NAME, rservices);
			rservices.consoleSubmit("load('" + WKS_FILE_NAME + "', .GlobalEnv)");
			rservices.consoleSubmit("unlink('" + WKS_FILE_NAME + "')");

		}
	}
}
