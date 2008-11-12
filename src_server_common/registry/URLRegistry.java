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
package registry;

import static uk.ac.ebi.microarray.pools.PoolUtils.getHostIp;
import static uk.ac.ebi.microarray.pools.PoolUtils.getHostName;
import static uk.ac.ebi.microarray.pools.PoolUtils.getProcessId;
import http.NotLoggedInException;
import http.RHttpProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import uk.ac.ebi.microarray.pools.RegistryProvider;
import uk.ac.ebi.microarray.pools.db.DBLayerInterface;

public class URLRegistry implements RegistryProvider{
	
	String sessionId=null;
	public Registry getRegistry() throws Exception {
		System.out.println(System.getProperty("generic.urlregistry.url"));
		
		final HashMap<String, Object> options = new HashMap<String, Object>();
		sessionId = RHttpProxy.logOnDB(System.getProperty("generic.urlregistry.url"), "", System.getProperty("generic.urlregistry.login"), System.getProperty("generic.urlregistry.password"), options);			
		
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
							options.put("java.rmi.server.codebase",System.getProperty("java.rmi.server.codebase"));
							options.put("job.id",System.getProperty("job.id"));
							options.put("job.name",System.getProperty("job.name"));
							options.put("notify.email",System.getProperty("notify.email"));
							System.out.println("###"+options);
							return RHttpProxy.invoke(System.getProperty("generic.urlregistry.url"), sessionId, "REGISTRY", method.getName(), new Class<?>[]{String.class, Remote.class, HashMap.class}, new Object[]{args[0],args[1],options}, new HttpClient(new MultiThreadedHttpConnectionManager()));	
						} else {
							return RHttpProxy.invoke(System.getProperty("generic.urlregistry.url"), sessionId, "REGISTRY", method.getName(), method.getParameterTypes(), args, new HttpClient(new MultiThreadedHttpConnectionManager()));
						}
					} catch (NotLoggedInException e) {
						sessionId = RHttpProxy.logOnDB(System.getProperty("generic.urlregistry.url"), "", System.getProperty("generic.urlregistry.login"), System.getProperty("generic.urlregistry.password"), options);
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
