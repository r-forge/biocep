/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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

import http.FileLoad;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class SaveToR {

	static GDApplet applet;

	public synchronized static void execute(final String path, final boolean sourceIt) {
		final String fileName = new File(path).getName();
		try {
			applet.getR().createWorkingDirectoryFile(fileName);
			FileLoad.upload(new File(path), fileName, applet.getR());
			
			
			if (sourceIt) {
				final String cmd = "source('" + fileName + "')";
				applet.safeConsoleSubmit(cmd);
			}
			
			/*
			if (sourceIt) {
				final String cmd = "source('" + fileName + "')";
				
							if (applet.getRLock().isLocked()) {
								JOptionPane.showMessageDialog(applet, "R is busy, please retry\n");
								return;
							}
							
							try {
								applet.getRLock().lock();
								
								
								final String log = applet.getR().pythonExecFromWorkingDirectoryFile(fileName);
								if (!log.equals("")) {
									JOptionPane.showMessageDialog(applet, log);
								}
								
								
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								applet.getRLock().unlock();
							}
							
							
			

				
				
				//applet.safeConsoleSubmit(cmd);
			}
			*/

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

	}
}
