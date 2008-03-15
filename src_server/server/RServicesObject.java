package server;

import graphics.pop.GDDevice;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Vector;

import mapping.RPackage;

import org.bioconductor.packages.rservices.RObject;

import remoting.FileDescription;
import remoting.RAction;
import remoting.RCallback;
import remoting.RNI;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.RemotePanel;

public class RServicesObject implements RServices {

	public void addErrListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public ManagedServant cloneServer() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void die() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String getHostIp() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLogs() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProcessId() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServantName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasConsoleMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasGraphicMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasPushPopMode() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isResetEnabled() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] listSymbols() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void logInfo(String message) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String ping() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable pop(String symbol) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeAllErrListeners() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeAllOutListeners() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeErrListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void removeOutListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void reset() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void assignNameToObjectReference(String name, RObject refObj) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public RObject call(String methodName, RObject... args) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void callAndAssignName(String varName, String methodName, RObject... args) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public RObject callAsReference(String methodName, RObject... args) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void createWorkingDirectoryFile(String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public RObject evalAndGetObject(String expression) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RObject evalAndGetObjectAsReference(String expression) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String evaluate(String expression) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String evaluateExpressions(String expression, int n) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void freeReference(RObject refObj) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String[] getAllPackageNames() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public StringBuffer getDemoSource(String demoName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RObject getObjectFromReference(RObject refObj) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RPackage getPackage(String packageName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRHelpFile(String uri) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRHelpFileUri(String topic, String pack) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public RNI getRNI() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public FileDescription getWorkingDirectoryFileDescription(String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public FileDescription[] getWorkingDirectoryFileDescriptions() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getWorkingDirectoryFileNames() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBusy() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHttpServerStarted(int port) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPortInUse(int port) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isProgressiveConsoleLogEnabled() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReference(RObject obj) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] listDemos() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public GDDevice newDevice(int w, int h) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<RAction> popRActions() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String print(String expression) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String printExpressions(String[] expressions) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void putObjectAndAssignName(RObject obj, String name) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public RObject putObjectAndGetReference(RObject obj) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeWorkingDirectoryFile(String fileName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setCallBack(RCallback callback) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setProgressiveConsoleLogEnabled(boolean progressiveLog) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String sourceFromBuffer(StringBuffer buffer) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void startHttpServer(int port) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void stop() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void stopHttpServer() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
}