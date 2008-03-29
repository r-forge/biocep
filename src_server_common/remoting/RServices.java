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
package remoting;

import graphics.pop.GDDevice;
import java.rmi.RemoteException;
import java.util.Vector;
import mapping.RPackage;
import org.bioconductor.packages.rservices.RObject;
import uk.ac.ebi.microarray.pools.ManagedServant;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface RServices extends ManagedServant {

	public String evaluate(String expression) throws RemoteException;

	public String evaluate(String expression, int n) throws RemoteException;

	public RObject call(String methodName, RObject... args) throws RemoteException;

	public RObject callAndGetReference(String methodName, RObject... args) throws RemoteException;

	public void callAndAssign(String varName, String methodName, RObject... args) throws RemoteException;

	public boolean isReference(RObject obj) throws RemoteException;

	public RObject referenceToObject(RObject refObj) throws RemoteException;

	public RObject putAndGetReference(RObject obj) throws RemoteException;

	public void putAndAssign(RObject obj, String name) throws RemoteException;

	public void assignReference(String name, RObject refObj) throws RemoteException;

	public RObject get(String expression) throws RemoteException;

	public RObject getReference(String expression) throws RemoteException;

	public String print(String expression) throws RemoteException;

	public String printExpressions(String[] expressions) throws RemoteException;

	public void freeReference(RObject refObj) throws RemoteException;

	public String sourceFromResource(String resource) throws RemoteException;

	public String sourceFromBuffer(StringBuffer buffer) throws RemoteException;

	public String getStatus() throws RemoteException;

	public String[] listPackages() throws RemoteException;

	public RPackage getPackage(String packageName) throws RemoteException;

	public void setCallBack(RCallback callback) throws RemoteException;

	public RNI getRNI() throws RemoteException;

	public void stop() throws RemoteException;

	public GDDevice newDevice(int w, int h) throws java.rmi.RemoteException;
	
	public GDDevice[] listDevices() throws java.rmi.RemoteException;

	public String[] getWorkingDirectoryFileNames() throws java.rmi.RemoteException;

	public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException;

	public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException;

	public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException;

	public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException;

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException;

	public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException;

	public String[] listDemos() throws java.rmi.RemoteException;

	public StringBuffer getDemoSource(String demoName) throws java.rmi.RemoteException;

	public void setProgressiveConsoleLogEnabled(boolean progressiveLog) throws java.rmi.RemoteException;

	public boolean isProgressiveConsoleLogEnabled() throws java.rmi.RemoteException;

	public byte[] getRHelpFile(String uri) throws java.rmi.RemoteException;

	public String getRHelpFileUri(String topic, String pack) throws java.rmi.RemoteException;

	public Vector<RAction> popRActions() throws java.rmi.RemoteException;

	
	public boolean isPortInUse(int port) throws java.rmi.RemoteException;

	public void startHttpServer(int port) throws java.rmi.RemoteException;

	public boolean isHttpServerStarted(int port) throws java.rmi.RemoteException;

	public void stopHttpServer() throws java.rmi.RemoteException;
	
	
	public Vector<String> getSvg(String expression, int width, int height) throws RemoteException;
	
	
	public String pythonExec(String pythonCommand) throws RemoteException;
	
	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException;
	
	public String pythonExceFromResource(String resource) throws RemoteException;

	public String pythonExecFromBuffer(StringBuffer buffer) throws RemoteException;

	public RObject pythonEval(String pythonCommand) throws RemoteException;
	
	public RObject pythonGet(String name) throws RemoteException;
	
	public void pythonSet(String name, RObject Value) throws RemoteException;
	
	public String getPythonStatus() throws RemoteException;

}
