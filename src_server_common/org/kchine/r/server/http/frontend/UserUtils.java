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
package org.kchine.r.server.http.frontend;

import java.io.File;

import org.kchine.r.server.RServices;
import org.kchine.r.server.http.FileLoad;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class UserUtils {
	public static String WKS_FILE_NAME = ".RData_biocep";

	public static void saveWorkspace(String login, RServices rservices) throws Exception {
		if (rservices != null && login != null && !login.equals("") && !login.equalsIgnoreCase("guest")) {
			File wdirFile = new File(System.getProperty("users.data.dir.root") + System.getProperty("file.separator") + login);
			if (!wdirFile.exists()) {
				wdirFile.mkdirs();
			}
			rservices.consoleSubmit("save.image(file = '" + WKS_FILE_NAME + "')");
			FileLoad.download(WKS_FILE_NAME, new File(wdirFile.getAbsolutePath() + System.getProperty("file.separator") + WKS_FILE_NAME), rservices);
			rservices.consoleSubmit("unlink('" + WKS_FILE_NAME + "')");
		}
	}

	public static void loadWorkspace(String login, RServices rservices) throws Exception {
		if (rservices != null && login != null && !login.equals("") && !login.equalsIgnoreCase("guest")) {
			File wdirFile = new File(System.getProperty("users.data.dir.root") + System.getProperty("file.separator") + login);
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
