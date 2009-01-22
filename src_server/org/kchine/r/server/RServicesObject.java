/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package org.kchine.r.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;

import org.kchine.r.RObject;
import org.kchine.r.server.FileDescription;
import org.kchine.r.server.RAction;
import org.kchine.r.server.RCallBack;
import org.kchine.r.server.RCollaborationListener;
import org.kchine.r.server.RConsoleActionListener;
import org.kchine.r.server.RNI;
import org.kchine.r.server.RPackage;
import org.kchine.r.server.RServices;
import org.kchine.r.server.UserStatus;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.iplots.SVarInterfaceRemote;
import org.kchine.r.server.iplots.SVarSetInterfaceRemote;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.RemotePanel;


/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class RServicesObject implements RServices {

	public void addErrListener(RemoteLogListener listener) throws RemoteException {
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
	}

	public ManagedServant cloneServer() throws RemoteException {
		return null;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		return null;
	}

	public void die() throws RemoteException {
	}

	public String getHostIp() throws RemoteException {
		return null;
	}

	public String getHostName() throws RemoteException {
		return null;
	}
	
	public String getLogs() throws RemoteException {
		return null;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		return null;
	}

	public String getProcessId() throws RemoteException {
		return null;
	}

	public String getServantName() throws RemoteException {
		return null;
	}

	public boolean hasConsoleMode() throws RemoteException {
		return false;
	}

	public boolean hasGraphicMode() throws RemoteException {
		return false;
	}

	public boolean hasPushPopMode() throws RemoteException {
		return false;
	}

	public boolean isResetEnabled() throws RemoteException {
		return false;
	}

	public String[] listSymbols() throws RemoteException {
		return null;
	}

	public void logInfo(String message) throws RemoteException {
	}

	public String ping() throws RemoteException {
		return null;
	}

	public Serializable pop(String symbol) throws RemoteException {
		return null;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
	}

	public void removeAllErrListeners() throws RemoteException {
	}

	public void removeAllOutListeners() throws RemoteException {
	}

	public void removeErrListener(RemoteLogListener listener) throws RemoteException {
	}

	public void removeOutListener(RemoteLogListener listener) throws RemoteException {
	}

	public void reset() throws RemoteException {
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
	}

	public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws RemoteException {
	}

	public void assignReference(String name, RObject refObj) throws RemoteException {
	}

	public RObject call(String methodName, Object... args) throws RemoteException {
		return null;
	}

	public void callAndAssign(String varName, String methodName, Object... args) throws RemoteException {
	}

	public RObject callAndGetReference(String methodName, Object... args) throws RemoteException {
		return null;
	}

	public RObject callAndGetObjectName(String methodName, Object... args) throws RemoteException {
		return null;
	}

	public void createWorkingDirectoryFile(String fileName) throws RemoteException {
	}

	public RObject getObject(String expression) throws RemoteException {
		return null;
	}

	public RObject getReference(String expression) throws RemoteException {
		return null;
	}

	public String evaluate(String expression) throws RemoteException {
		return null;
	}

	public String evaluate(String expression, int n) throws RemoteException {
		return null;
	}

	public void freeReference(RObject refObj) throws RemoteException {
	}

	public String[] listPackages() throws RemoteException {
		return null;
	}

	public String getDemoSource(String demoName) throws RemoteException {
		return null;
	}

	public RObject referenceToObject(RObject refObj) throws RemoteException {
		return null;
	}

	public RPackage getPackage(String packageName) throws RemoteException {
		return null;
	}

	public byte[] getRHelpFile(String uri) throws RemoteException {
		return null;
	}

	public String getRHelpFileUri(String topic, String pack) throws RemoteException {
		return null;
	}

	public RNI getRNI() throws RemoteException {
		return null;
	}

	public String getStatus() throws RemoteException {
		return null;
	}

	public FileDescription getWorkingDirectoryFileDescription(String fileName) throws RemoteException {
		return null;
	}

	public FileDescription[] getWorkingDirectoryFileDescriptions() throws RemoteException {
		return null;
	}

	public String[] getWorkingDirectoryFileNames() throws RemoteException {
		return null;
	}

	public boolean isBusy() throws RemoteException {
		return false;
	}

	public boolean isHttpServerStarted(int port) throws RemoteException {
		return false;
	}

	public boolean isPortInUse(int port) throws RemoteException {
		return false;
	}

	public boolean isProgressiveConsoleLogEnabled() throws RemoteException {
		return false;
	}

	public boolean isReference(RObject obj) throws RemoteException {
		return false;
	}

	public String[] listDemos() throws RemoteException {
		return null;
	}

	public GDDevice newDevice(int w, int h) throws RemoteException {
		return null;
	}

	public GDDevice[] listDevices() throws RemoteException {
		return null;
	}

	public Vector<RAction> popRActions() throws RemoteException {
		return null;
	}

	public String print(String expression) throws RemoteException {
		return null;
	}

	public String printExpressions(String[] expressions) throws RemoteException {
		return null;
	}

	public void putAndAssign(Object obj, String name) throws RemoteException {
	}

	public RObject putAndGetReference(Object obj) throws RemoteException {
		return null;
	}

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws RemoteException {
		return null;
	}

	public void removeWorkingDirectoryFile(String fileName) throws RemoteException {
	}

	public void setCallBack(RCallBack callback) throws RemoteException {
	}

	public void setProgressiveConsoleLogEnabled(boolean progressiveLog) throws RemoteException {
	}

	public String sourceFromBuffer(String buffer) throws RemoteException {
		return null;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		return null;
	}

	public void startHttpServer(int port) throws RemoteException {
	}

	public String unsafeGetObjectAsString(String cmd) throws RemoteException {
		return null;
	}
	
	public void stop() throws RemoteException {
	}

	public void stopHttpServer() throws RemoteException {
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
	}

	public Vector<String> getSvg(String expression, int width, int height) throws RemoteException {
		return null;
	}
	
	public byte[] getPdf(String script, int width, int height) throws RemoteException {
		return null;
	}

	public String getPythonStatus() throws RemoteException {
		return null;
	}

	public String pythonExceFromResource(String resource) throws RemoteException {
		return null;
	}

	public String pythonExec(String pythonCommand) throws RemoteException {
		return null;
	}

	public String pythonExecFromBuffer(String buffer) throws RemoteException {
		return null;
	}

	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
		return null;
	}

	public RObject pythonEval(String pythonCommand) throws RemoteException {
		return null;
	}

	public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException {
		return null;
	}

	public RObject pythonGet(String name) throws RemoteException {
		return null;
	}

	public Object pythonGetAndConvert(String name) throws RemoteException {
		return null;
	}

	public void pythonSet(String name, Object Value) throws RemoteException {
	}

	public Object callAndConvert(String methodName, Object... args) throws RemoteException {
		return null;
	}

	public Object getObjectConverted(String expression) throws RemoteException {
		return null;
	}

	public Object convert(RObject obj) throws RemoteException {
		return null;
	}

	public Object groovyEval(String expression) throws RemoteException {
		return null;
	}

	public String groovyExecFromResource(String resource) throws RemoteException {
		return null;
	}

	public String groovyExec(String groovyCommand) throws RemoteException {
		return null;
	}

	public String groovyExecFromBuffer(String buffer) throws RemoteException {
		return null;
	}

	public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
		return null;
	}

	public Object groovyGet(String name) throws RemoteException {
		return null;
	}

	public void groovySet(String name, Object Value) throws RemoteException {
	}

	public String getGroovyStatus() throws RemoteException {
		return null;
	}

	public void reinitializeGroovyInterpreter() throws RemoteException {
	}
	
	public boolean isExtensionAvailable(String extensionName) throws RemoteException {
		return false;
	}
	
	public String[] listExtensions() throws RemoteException {
		return null;
	}
	
	public void installExtension(String extensionName, String extensionURL) throws RemoteException {		
	}
	
	public void installExtension(String extensionName, byte[] extension) {
	}
	
	public void removeExtension(String extensionName) throws RemoteException {
	}
	
	public void convertFile(String inputFile, String outputFile, String conversionFilter, boolean useServer) throws RemoteException {
	}
	
	public void freeAllReferences() throws RemoteException {
	}

	public RObject getObjectName(String expression) throws RemoteException {
		return null;
	}

	public RObject realizeObjectName(RObject objectName) throws RemoteException {
		return null;
	}

	public RObject realizeObjectNameConverted(RObject objectName) throws RemoteException {
		return null;
	}

	public void addRCallback(RCallBack callback) throws RemoteException {
	}

	public void removeAllRCallbacks() throws RemoteException {
	}

	public void removeRCallback(RCallBack callback) throws RemoteException {
	}

	public org.kchine.r.server.GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
		return null;
	}

	public org.kchine.r.server.GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
		return null;
	}

	public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {

	}

	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {

	}

	public void removeAllRCollaborationListeners() throws RemoteException {

	}
	
	public void addRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
		
	}
	
	public void removeRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
		
	}
	
	public void removeAllRConsoleActionListeners() throws RemoteException {
		
	}

	
	public void  registerUser(String sourceUID,String user) throws RemoteException {
		
	}
	
	public void  unregisterUser(String sourceUID) throws RemoteException {
		
	}
	public void  updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException {
		
	}
	public UserStatus[] getUserStatusTable() throws RemoteException {
		return null;
	}
	
	
	public void setUserInput(String userInput) throws RemoteException {

		
	}

	public void chat(String sourceUID, String user, String message) throws RemoteException {
		
	}
	
	public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
		
	}
	
	public GDDevice newBroadcastedDevice(int w, int h) throws RemoteException {
		return null;
	}
	
	public void broadcastGraphics() throws RemoteException {	
	}
	
	public boolean hasRCollaborationListeners() throws RemoteException {
		return false;
	}

	public SpreadsheetModelRemote getSpreadsheetTableModelRemote(String Id) throws RemoteException {
		return null;
	}

	public SpreadsheetModelRemote[] listSpreadsheetTableModelRemote() throws RemoteException {
		return null;
	}

	public String[] listSpreadsheetTableModelRemoteId() throws RemoteException {
		return null;
	}

	public SpreadsheetModelRemote newSpreadsheetTableModelRemote(int rowCount, int colCount) throws RemoteException {
		return null;
	}
		
	public int countSets() throws RemoteException {
		return 0;
	}
	
	public SVarSetInterfaceRemote getSet(int i) throws RemoteException {
		return null;
	}
	
	public SVarSetInterfaceRemote getCurrentSet() throws RemoteException {
		return null;
	}
	
	public int curSetId() throws RemoteException {
		return 0;
	}
	
	public SVarInterfaceRemote getVar(int setId, int i) throws RemoteException {
		return null;
	}
	
	public SVarInterfaceRemote getVar(int setId, String name) throws RemoteException {
		return null;
	}
	
	public void setOrginatorUID(String uid) throws RemoteException {
		
	}
	
	public String getOriginatorUID() throws RemoteException {
		return null;
	}
	
	public String getJobId() throws RemoteException {
		return null;
	}
	
	public void setJobId(String jobId) throws RemoteException {
	}
	
	public boolean symbolExists(String symbol) throws RemoteException {
		return false;
	}
	
	public String getStub() throws RemoteException {

		return null;
	}
	
	public void addProbeOnCells(String spreadsheetName) {

		
	}
	
	public void addProbeOnVariables(String[] variables) throws RemoteException {

		
	}
	
	public RObject cellsGet(String range, String type, String spreadsheetName) throws RemoteException {

		return null;
	}
	
	public Object cellsGetConverted(String range, String type, String spreadsheetName) throws RemoteException {

		return null;
	}
	
	public void cellsPut(Object value, String location, String spreadsheetName) throws RemoteException {

		
	}
	
	public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException {

		return null;
	}
	
	public String[] getProbedVariables() throws RemoteException {

		return null;
	}
	
	public boolean isProbeOnCell(String spreadsheetName)throws RemoteException {

		return false;
	}
	
	public void removeProbeOnCells(String spreadsheetName) throws RemoteException {

		
	}
	
	public void removeProbeOnVariables(String[] variables) throws RemoteException {

		
	}
	
	public void setProbedVariables(String[] variables) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		return null;
		
	}

}