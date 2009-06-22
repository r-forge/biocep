package org.kchine.scilab.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javasci.SciDouble;
import javasci.Scilab;

import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.RemotePanel;

public class ScilabServicesImpl implements ScilabServices{

	public ScilabServicesImpl() {
		
		new Exception().printStackTrace();
		
		try {
			scilabSetWorkingDirectory(DirectJNI.getInstance().getRServices().getWorkingDirectory());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (ServerManager.sci != null) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					Scilab.Finish();
				}
			});
		}
	}

	
	public void scilabSetWorkingDirectory(String dir) throws RemoteException {
		Scilab.Exec("chdir('"+dir+"')");		
	}
	
    public boolean scilabExec(String cmd) throws java.rmi.RemoteException {
		return Scilab.Exec(cmd);
    }
    
    public String scilabConsoleSubmit(String cmd) throws java.rmi.RemoteException {
    	final StringBuffer result = new StringBuffer();
		try {
			
			
			String fn=System.getProperty("java.io.tmpdir")+"/"+"scilab"+System.currentTimeMillis();
			Scilab.Exec("diary(\""+fn+"\")");
			Scilab.Exec( cmd );
			if (Scilab.GetLastErrorCode()!=0) Scilab.Exec("disp(lasterror())");
			Scilab.Exec("diary(0)");
			BufferedReader br=new BufferedReader(new FileReader(fn));
			String line=null;
			while ((line =br.readLine())!=null) {
				if (!line.startsWith("-->")) result.append(line+"\n");
			}
			br.close();
			new File(fn).delete();

			System.out.println("Result:"+result.toString());
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("",e);
		 }finally {
			
		}

    }
    
    public Object scilabGetObject(String expression) throws RemoteException {
		SciDouble d=new SciDouble("var_import");
		Scilab.Exec("var_import="+expression);
		d.Get();
		Double result=d.getData();
		Scilab.Exec("clear var_import");
		return result;
    }
    
    public void scilabPutAndAssign(Object obj, String name) throws RemoteException {
    	if (obj instanceof Double) {
			SciDouble d=new SciDouble(name, (Double)obj);
			d.Send();
			}
    }
    
    public String scilabGetStatus() throws RemoteException {
    	return "";
    }

    
    
    
	public void addErrListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public ManagedServant cloneServer() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String consoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
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

	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostIp() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHostName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJobId() throws RemoteException {
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

	public String getStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStub() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getSystemEnv() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getSystemProperties() throws RemoteException {
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

	public boolean isBusy() throws RemoteException {
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

	public void setJobId(String jobId) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
    
    
    

}
