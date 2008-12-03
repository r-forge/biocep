import static org.kchine.rpf.PoolUtils.getHostIp;
import static org.kchine.rpf.PoolUtils.getHostName;
import static org.kchine.rpf.PoolUtils.getProcessId;
import http.NotLoggedInException;
import http.RHttpProxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.kchine.rpf.RegistryProvider;
import org.kchine.rpf.db.DBLayerInterface;

public class httpregistryClass implements RegistryProvider{
	
	String sessionId=null;
	public Registry getRegistry(final Properties props) throws Exception {
		System.out.println(props.get("httpregistry.url"));
		final HashMap<String, Object> options = new HashMap<String, Object>();

		sessionId = RHttpProxy.logOnDB((String)props.get("httpregistry.url"), "", (String)props.get("httpregistry.login"), (String)props.get("httpregistry.password"), options);			
		
		DBLayerInterface db = (DBLayerInterface)Proxy.newProxyInstance(RHttpProxy.class.getClassLoader(), new Class<?>[]{DBLayerInterface.class}, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
				for (int i=0; i<3; ++i) {
					try {
						if ((method.getName().equals("bind") || method.getName().equals("rebind") && args.length==2)) {					
							HashMap<String, Object> options=new HashMap<String, Object>();
							options.put("process.id",getProcessId());
							options.put("host.name",getHostName());
							options.put("host.ip",getHostIp());
							options.put("os.name",System.getProperty("os.name"));
							options.put("java.rmi.server.codebase",(String)props.get("java.rmi.server.codebase"));
							options.put("job.id",(String)props.get("job.id"));
							options.put("job.name",(String)props.get("job.name"));
							options.put("notify.email",(String)props.get("notify.email"));
							System.out.println("###"+options);
							return RHttpProxy.invoke((String)props.get("httpregistry.url"), sessionId, "REGISTRY", method.getName(), new Class<?>[]{String.class, Remote.class, HashMap.class}, new Object[]{args[0],args[1],options}, new HttpClient(new MultiThreadedHttpConnectionManager()));	
						} else {
							return RHttpProxy.invoke((String)props.get("httpregistry.url"), sessionId, "REGISTRY", method.getName(), method.getParameterTypes(), args, new HttpClient(new MultiThreadedHttpConnectionManager()));
						}
					} catch (NotLoggedInException e) {
						
						sessionId = RHttpProxy.logOnDB((String)props.get("httpregistry.url"), "", (String)props.get("httpregistry.login"), (String)props.get("httpregistry.password"), options);
					} catch (Exception e) {
						throw e;
					}			
				}
				
				throw new Exception("URL Registry Ivokation Failed");
				
			}
		});
		
		System.out.println("db:"+Arrays.toString(db.list()));
		
		return db;
	}
	
}
