
/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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

package uk.ac.ebi.microarray.pools.db;
import java.awt.Frame;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface SupervisorInterface {
	public void killProcess(String servantName, boolean useKillCommand, Frame referenceFrame) throws Exception;
	public void launchLocalProcess(final boolean showConsole, String homeDir, String command, String prefix, boolean isForWindows) throws Exception;
	public void launch(final String nodeName, final String options, final boolean showConsole) throws Exception;
}
