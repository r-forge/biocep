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

import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;



/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class GUtils {

	public static String INSTALL_DIR = new File(System.getProperty("user.home") + "/RWorkbench/").getAbsolutePath()
			+ "/";
	static final String RLIBSTART = "R$LIB$START";
	static final String RLIBEND = "R$LIB$END";
	static final String RVERSTART = "R$VER$START";
	static final String RVEREND = "R$VER$END";
	
	public static String[] getRInfo(String rhome) {
	
		File getInfoFile = new File(INSTALL_DIR + "getInfo.R");
	
		File getInfoOutputFile = new File(INSTALL_DIR + "getInfo.Rout");
	
		String rversion = null;
	
		String rlibraypath = null;
	
		try {
	
			FileWriter fw = new FileWriter(getInfoFile);
	
			PrintWriter pw = new PrintWriter(fw);
	
			pw.println("paste('" + RLIBSTART + "',.Library, '" + RLIBEND + "',sep='%')");
	
			pw.println("paste('" + RVERSTART + "', R.version.string , '" + RVEREND + "', sep='%')");
	
			fw.close();
	
			Vector<String> getInfoCommand = new Vector<String>();
	
			if (rhome != null) {
				getInfoCommand.add(rhome + "bin/R");
				getInfoCommand.add("CMD");
				getInfoCommand.add("BATCH");
				getInfoCommand.add("--no-save");
				getInfoCommand.add(getInfoFile.getAbsolutePath());
				getInfoCommand.add(getInfoOutputFile.getAbsolutePath());
	
			} else {
	
				if (isWindowsOs()) {
	
					getInfoCommand.add(System.getenv().get("ComSpec"));
					getInfoCommand.add("/C");
					getInfoCommand.add("R");
					getInfoCommand.add("CMD");
					getInfoCommand.add("BATCH");
					getInfoCommand.add("--no-save");
					getInfoCommand.add(getInfoFile.getAbsolutePath());
					getInfoCommand.add(getInfoOutputFile.getAbsolutePath());
	
				} else {
					getInfoCommand.add(/* System.getenv().get("SHELL") */"/bin/sh");
					getInfoCommand.add("-c");
					getInfoCommand.add("R CMD BATCH --no-save " + getInfoFile.getAbsolutePath() + " "
							+ getInfoOutputFile.getAbsolutePath());
				}
			}
	
			Vector<String> systemEnvVector = new Vector<String>();
	
			{
	
				Map<String, String> osenv = System.getenv();
	
				Map<String, String> env = new HashMap<String, String>(osenv);
	
				for (String k : env.keySet()) {
	
					systemEnvVector.add(k + "=" + env.get(k));
	
				}
	
			}
	
			System.out.println("exec->" + getInfoCommand);
	
			final Process getInfoProc = Runtime.getRuntime().exec(getInfoCommand.toArray(new String[0]),
	
			systemEnvVector.toArray(new String[0]));
	
			new Thread(new Runnable() {
	
				public void run() {
	
					try {
	
						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getErrorStream()));
	
						String line = null;
	
						while ((line = br.readLine()) != null) {
	
							//System.out.println(line);
	
						}
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
	
				}
	
			}).start();
	
			new Thread(new Runnable() {
	
				public void run() {
	
					try {
	
						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getInputStream()));
	
						String line = null;
	
						while ((line = br.readLine()) != null) {
	
							//System.out.println(line);
	
						}
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
	
				}
	
			}).start();
	
			getInfoProc.waitFor();
	
			if (getInfoOutputFile.exists() && getInfoOutputFile.lastModified() > getInfoFile.lastModified()) {
	
				BufferedReader br = new BufferedReader(new FileReader(getInfoOutputFile));
	
				String line = null;
	
				while ((line = br.readLine()) != null) {
	
					//System.out.println(line);
	
					if (line.contains(RLIBSTART + "%")) {
	
						rlibraypath = line.substring(line.indexOf(RLIBSTART + "%") + (RLIBSTART + "%").length(), (line
								.indexOf("%" + RLIBEND) > 0 ? line.indexOf("%" + RLIBEND) : line.length()));
	
					}
	
					if (line.contains(RVERSTART + "%")) {
	
						rversion = line.substring(line.indexOf(RVERSTART + "%") + (RVERSTART + "%").length(), line
								.indexOf("%" + RVEREND));
	
					}
	
				}
	
			}
	
		} catch (Exception e) {
	
			e.printStackTrace();
	
		}
	
		if (rversion != null && rlibraypath != null) {
	
			return new String[] { rlibraypath, rversion };
	
		} else {
	
			return null;
	
		}
	
	}

}