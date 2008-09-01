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
package remoting;

import graphics.pop.GDDevice;
import java.rmi.RemoteException;
import java.util.Vector;
import mapping.RPackage;
import model.SpreadsheetModelRemote;
import org.bioconductor.packages.rservices.RObject;
import org.rosuda.ibase.SVarInterfaceRemote;
import org.rosuda.ibase.SVarSetInterfaceRemote;
import uk.ac.ebi.microarray.pools.ManagedServant;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface RServices extends ManagedServant {

	public String evaluate(String expression) throws RemoteException;

	public String evaluate(String expression, int n) throws RemoteException;
		
	public RObject call(String methodName, Object... args) throws RemoteException;

	public Object callAndConvert(String methodName, Object... args) throws RemoteException;
	
	public RObject callAndGetReference(String methodName, Object... args) throws RemoteException;
	
	public RObject callAndGetObjectName(String methodName, Object... args) throws RemoteException;

	public void callAndAssign(String varName, String methodName, Object... args) throws RemoteException;
		
	public boolean isReference(RObject obj) throws RemoteException;

	public RObject referenceToObject(RObject refObj) throws RemoteException;

	public RObject putAndGetReference(Object obj) throws RemoteException;

	public void putAndAssign(Object obj, String name) throws RemoteException;

	public void assignReference(String name, RObject refObj) throws RemoteException;

	public RObject getObject(String expression) throws RemoteException;

	public RObject getReference(String expression) throws RemoteException;
	
	public Object getObjectConverted(String expression) throws RemoteException;
	
	public RObject getObjectName(String expression) throws RemoteException;
	
	public RObject realizeObjectName(RObject objectName) throws RemoteException;
	
	public Object realizeObjectNameConverted(RObject objectName) throws RemoteException;
		
	public void freeAllReferences() throws RemoteException;	
	
	public Object convert(RObject obj) throws RemoteException;
	
	public String print(String expression) throws RemoteException;

	public String printExpressions(String[] expressions) throws RemoteException;

	public void freeReference(RObject refObj) throws RemoteException;

	public String sourceFromResource(String resource) throws RemoteException;

	public String sourceFromBuffer(StringBuffer buffer) throws RemoteException;

	public String getStatus() throws RemoteException;

	public String[] listPackages() throws RemoteException;

	public RPackage getPackage(String packageName) throws RemoteException;
	
	public boolean symbolExists(String symbol) throws RemoteException;	
	
	public void addRCallback(RCallBack callback) throws RemoteException;	
	public void removeRCallback(RCallBack callback) throws RemoteException;	
	public void removeAllRCallbacks() throws RemoteException;	

	public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException;	
	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException;	
	public void removeAllRCollaborationListeners() throws RemoteException;
	public boolean hasRCollaborationListeners() throws RemoteException;
		
	public void addRConsoleActionListener(RConsoleActionListener ractionListener) throws RemoteException;	
	public void removeRConsoleActionListener(RConsoleActionListener ractionListener) throws RemoteException;	
	public void removeAllRConsoleActionListeners() throws RemoteException;
	
	
	public void  registerUser(String sourceUID,String user) throws RemoteException;
	public void  unregisterUser(String sourceUID) throws RemoteException;
	public void  updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException;
	public UserStatus[] getUserStatusTable() throws RemoteException;

	
	public void setUserInput(String userInput) throws RemoteException;
	
	public void setOrginatorUID(String uid) throws RemoteException;
	public String getOriginatorUID() throws RemoteException;
	
	public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException;	
	public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException;

	
	
	void chat(String sourceUID,String user, String message) throws RemoteException;
	void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException;
	
	public RNI getRNI() throws RemoteException;

	public void stop() throws RemoteException;

	public GDDevice newDevice(int w, int h) throws java.rmi.RemoteException;
	public GDDevice newBroadcastedDevice(int w, int h) throws java.rmi.RemoteException;
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

	public byte[] getRHelpFile(String uri) throws java.rmi.RemoteException;
	public String getRHelpFileUri(String topic, String pack) throws java.rmi.RemoteException;

	public Vector<String> getSvg(String script, int width, int height) throws RemoteException;	
	public byte[] getPdf(String script, int width, int height) throws RemoteException;	
	
	public boolean isPortInUse(int port) throws java.rmi.RemoteException;
	public void startHttpServer(int port) throws java.rmi.RemoteException;
	public boolean isHttpServerStarted(int port) throws java.rmi.RemoteException;
	public void stopHttpServer() throws java.rmi.RemoteException;
			
	public String pythonExec(String pythonCommand) throws RemoteException;	
	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException;	
	public String pythonExceFromResource(String resource) throws RemoteException;
	public String pythonExecFromBuffer(StringBuffer buffer) throws RemoteException;	
	public RObject pythonEval(String pythonCommand) throws RemoteException;
	public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException;		
	public RObject pythonGet(String name) throws RemoteException;
	public Object pythonGetAndConvert(String name) throws RemoteException;
	public void pythonSet(String name, Object Value) throws RemoteException;	
	public String getPythonStatus() throws RemoteException;
		
	public boolean isGroovyEnabled() throws RemoteException;	
	public String groovyExec(String groovyCommand) throws RemoteException;	
	public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException;	
	public String groovyExecFromResource(String resource) throws RemoteException;
	public String groovyExecFromBuffer(StringBuffer buffer) throws RemoteException;	
	public Object groovyEval(String expression) throws RemoteException;		
	public Object groovyGet(String name) throws RemoteException;
	public void groovySet(String name, Object Value) throws RemoteException;	
	public String getGroovyStatus() throws RemoteException;
		
	public SpreadsheetModelRemote newSpreadsheetTableModelRemote(int rowCount, int colCount) throws RemoteException;	
	public SpreadsheetModelRemote getSpreadsheetTableModelRemote(String Id) throws RemoteException;	
	public SpreadsheetModelRemote[] listSpreadsheetTableModelRemote() throws RemoteException;	
	public String[] listSpreadsheetTableModelRemoteId() throws RemoteException;
		
    public int countSets() throws RemoteException;
    public SVarSetInterfaceRemote getSet( int i) throws RemoteException;
    public SVarInterfaceRemote getVar( int setId, int i) throws RemoteException;
    public SVarInterfaceRemote getVar( int setId, String name) throws RemoteException;
    public SVarSetInterfaceRemote getCurrentSet() throws RemoteException;
    public int curSetId() throws RemoteException;
    	
}
