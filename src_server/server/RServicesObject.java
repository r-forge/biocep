package server;

import graphics.pop.GDDevice;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Vector;
import mapping.RPackage;
import model.SpreadsheetModelRemote;

import org.bioconductor.packages.rservices.RObject;
import remoting.FileDescription;
import remoting.RAction;
import remoting.RCallBack;
import remoting.RCollaborationListener;
import remoting.RHelpListener;
import remoting.RNI;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.RemotePanel;

/**
 * @author Karim Chine   k.chine@imperial.ac.uk
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

	public StringBuffer getDemoSource(String demoName) throws RemoteException {
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

	public String sourceFromBuffer(StringBuffer buffer) throws RemoteException {
		return null;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		return null;
	}

	public void startHttpServer(int port) throws RemoteException {
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

	public String getPythonStatus() throws RemoteException {
		return null;
	}

	public String pythonExceFromResource(String resource) throws RemoteException {
		return null;
	}

	public String pythonExec(String pythonCommand) throws RemoteException {
		return null;
	}

	public String pythonExecFromBuffer(StringBuffer buffer) throws RemoteException {
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

	public String groovyExecFromBuffer(StringBuffer buffer) throws RemoteException {
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

	public boolean isGroovyEnabled() throws RemoteException {
		return false;
	}

	public String getGroovyStatus() throws RemoteException {
		return null;
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

	public remoting.GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
		return null;
	}

	public remoting.GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
		return null;
	}

	public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {

	}

	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {

	}

	public void removeAllRCollaborationListeners() throws RemoteException {

	}
	
	public void addRHelpListener(RHelpListener helpListener) throws RemoteException {
		
	}
	
	public void removeRHelpListener(RHelpListener helpListener) throws RemoteException {
		
	}
	
	public void removeAllRHelpListeners() throws RemoteException {
		
	}

	public void chat(String sourceSession, String message) throws RemoteException {

	}

	public void consolePrint(String sourceSession, String expression, String result) throws RemoteException {

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

}