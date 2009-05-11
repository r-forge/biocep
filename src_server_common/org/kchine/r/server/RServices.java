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
package org.kchine.r.server;

import java.rmi.RemoteException;
import java.util.HashMap;
import org.kchine.r.RObject;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.iplots.SVarInterfaceRemote;
import org.kchine.r.server.iplots.SVarSetInterfaceRemote;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.rpf.ManagedServant;


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
		
	public String sourceFromBuffer(String buffer) throws RemoteException;
	
	public String sourceFromBuffer(String buffer, HashMap<String, Object> clientProperties) throws RemoteException;

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

	public String unsafeGetObjectAsString( String cmd ) throws RemoteException;
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
	public String getDemoSource(String demoName) throws java.rmi.RemoteException;

	public byte[] getRHelpFile(String uri) throws java.rmi.RemoteException;
	public String getRHelpFileUri(String topic, String pack) throws java.rmi.RemoteException;
	
	public byte[] getSvg(String script, int width, int height) throws RemoteException;
	public byte[] getSvg(String script, Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException;
	
	public byte[] getPostscript(String script, int width, int height) throws RemoteException;
	public byte[] getPostscript(String script, Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg, Integer width, Integer height, Boolean horizontal, Integer pointsize, String paper , Boolean pagecentre, String colormodel) throws RemoteException;
	
	public byte[] getPdf(String script, int width, int height) throws RemoteException;
	public byte[] getPdf(String script, Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper, 
			String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException;
	
	public byte[] getPictex(String script, int width, int height) throws RemoteException;
	public byte[] getPictex(String script, Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException;
	
	public byte[] getBmp(String script, int width, int height) throws RemoteException;
	public byte[] getBmp(String script, Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException;
	
	public byte[] getJpeg(String script, int width, int height) throws RemoteException;
	public byte[] getJpeg(String script, Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg,Integer res) throws RemoteException ;
	
	public byte[] getPng(String script, int width, int height) throws RemoteException;
	public byte[] getPng(String script, Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException;
	
	public byte[] getTiff(String script, int width, int height) throws RemoteException;
	public byte[] getTiff(String script, Integer width, Integer height, String units, Integer pointsize, String compression, String bg,Integer res) throws RemoteException;
	
	public byte[] getXfig(String script, int width, int height) throws RemoteException;
	public byte[] getXfig(String script, Boolean onefile, String encoding , String paper, Boolean horizontal, 
			Integer width, Integer height, String family , Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException;
	
	public byte[] getWmf(String script, int width, int height, boolean useserver) throws RemoteException;
	public byte[] getEmf(String script, int width, int height, boolean useserver) throws RemoteException;	
	public byte[] getOdg(String script, int width, int height) throws RemoteException;
	
	public byte[] getFromImageIOWriter(String script, int width, int height,String format) throws RemoteException;
	
	public boolean isPortInUse(int port) throws java.rmi.RemoteException;
	public void startHttpServer(int port) throws java.rmi.RemoteException;
	public boolean isHttpServerStarted(int port) throws java.rmi.RemoteException;
	public void stopHttpServer() throws java.rmi.RemoteException;
			
	public String pythonExec(String pythonCommand) throws RemoteException;	
	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException;	
	public String pythonExceFromResource(String resource) throws RemoteException;
	public String pythonExecFromBuffer(String buffer) throws RemoteException;	
	public RObject pythonEval(String pythonCommand) throws RemoteException;
	public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException;		
	public RObject pythonGet(String name) throws RemoteException;
	public Object pythonGetAndConvert(String name) throws RemoteException;
	public void pythonSet(String name, Object Value) throws RemoteException;	
	public String getPythonStatus() throws RemoteException;
		
	public String groovyExec(String groovyCommand) throws RemoteException;	
	public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException;	
	public String groovyExecFromResource(String resource) throws RemoteException;
	public String groovyExecFromBuffer(String buffer) throws RemoteException;	
	public Object groovyEval(String expression) throws RemoteException;		
	public Object groovyGet(String name) throws RemoteException;
	public void groovySet(String name, Object Value) throws RemoteException;	
	public String getGroovyStatus() throws RemoteException;
	
	public void reinitializeGroovyInterpreter () throws RemoteException;	
	public boolean isExtensionAvailable(String extensionName) throws RemoteException;
	public String[] listExtensions() throws RemoteException;
	public void installExtension(String extensionName, byte[] extension) throws RemoteException;
	public void installExtension(String extensionName, String extensionURL) throws RemoteException;
	public void removeExtension(String extensionName) throws RemoteException; 
	
	void convertFile(String inputFile,  String outputFile, String conversionFilter, boolean useserver) throws RemoteException;
		
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
    
    public void addProbeOnVariables(String[] variables) throws RemoteException; 
    public void removeProbeOnVariables(String[] variables) throws RemoteException;
    public String[] getProbedVariables() throws RemoteException;
    public void setProbedVariables(String[] variables) throws RemoteException;
    
    public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException;
    
    RObject cellsGet(String range , String type, String spreadsheetName ) throws RemoteException;
    Object cellsGetConverted(String range , String type, String spreadsheetName ) throws RemoteException;
    void  cellsPut(Object value , String location, String spreadsheetName ) throws RemoteException;    

    void addProbeOnCells(String spreadsheetName)throws RemoteException;    
    boolean isProbeOnCell(String spreadsheetName)throws RemoteException;    
    void removeProbeOnCells(String spreadsheetName)throws RemoteException;
    
    String getWorkingDirectory() throws RemoteException;
    String getInstallDirectory() throws RemoteException;
    String getExtensionsDirectory() throws RemoteException;
    
    public boolean scilabExec(String cmd) throws java.rmi.RemoteException;
    
    public String scilabConsoleSubmit(String cmd) throws java.rmi.RemoteException;    
    public Object scilabGetObject(String expression) throws RemoteException;
    public void scilabPutAndAssign(Object obj, String name) throws RemoteException;
    
}
