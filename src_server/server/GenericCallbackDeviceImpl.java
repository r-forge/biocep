package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import remoting.GenericCallbackDevice;
import remoting.RAction;
import remoting.RConsoleAction;

public class GenericCallbackDeviceImpl extends UnicastRemoteObject implements GenericCallbackDevice {
	
	private HashMap<String, GenericCallbackDevice> _genericCallbackDeviceHashMap;
	private Vector<RAction> _rActions = new Vector<RAction>();	
	private static int _genericCallbackDeviceCounter=0;
	private String _id="GenericCallbackDevice_"+(_genericCallbackDeviceCounter++);
	private static int _port=System.getProperty("rmi.port.start")!=null && !System.getProperty("rmi.port.start").equals("") ? 2+Integer.decode(System.getProperty("rmi.port.start")) : 0;
	
	public GenericCallbackDeviceImpl(HashMap<String, GenericCallbackDevice> genericCallbackDeviceHashMap) throws RemoteException{
		super(_port);
		_genericCallbackDeviceHashMap=genericCallbackDeviceHashMap;	
		_genericCallbackDeviceHashMap.put(_id, this);
	}
	
	public void notify(HashMap<String, String> parameters) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("parameters", parameters);		
		RAction action = new RAction("notify", attributes);
		_rActions.add(action);
	}
		
	public void rConsoleActionPerformed(RConsoleAction consoleAction) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("consoleAction", consoleAction);		
		RAction action = new RAction("rConsoleActionPerformed", attributes);
		_rActions.add(action);		
	}
	
	public void help(String pack, String topic) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("package", pack);
		attributes.put("topic", topic);	
		RAction action = new RAction("help", attributes);
		_rActions.add(action);
	}
	
	public void chat(String sourceUID, String user, String message) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("sourceUID", sourceUID);
		attributes.put("user", user);
		attributes.put("message", message);	
		RAction action = new RAction("chat", attributes);
		_rActions.add(action);
	}
	
	public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("sourceUID", sourceUID);
		attributes.put("user", user);		
		attributes.put("expression", expression);
		attributes.put("result", result);
		RAction action = new RAction("consolePrint", attributes);
		_rActions.add(action);		
	}
	
	public String getId() throws RemoteException {
		return _id;
	}
	
	public void dispose() throws RemoteException {
		final String id = _id;
		new Thread(new Runnable() {
			public void run() {
				boolean shutdownSucceeded = false;
				while (true) {
					try {
						shutdownSucceeded = unexportObject(GenericCallbackDeviceImpl.this, false);
					} catch (Exception e) {
						e.printStackTrace();
						shutdownSucceeded = true;
					}
					System.out.println("-----shutdownSucceeded:" + shutdownSucceeded);
					if (shutdownSucceeded) {
						_genericCallbackDeviceHashMap.remove(id);
						break;
					}
					try {Thread.sleep(200);} catch (Exception e) {}
				}
			}
		}).start();

	}

	public Vector<RAction> popRActions() throws RemoteException {
		if (_rActions.size() == 0)
			return null;
		Vector<RAction> result = (Vector<RAction>) _rActions.clone();
		for (int i = 0; i < result.size(); ++i)	_rActions.remove(0);
		return result;
	}
		
}
