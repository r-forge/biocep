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
package http;

import java.rmi.registry.Registry;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.kchine.r.server.http.RHttpProxy;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.monitor.RegistryProvider;


public class DBHttpProxy implements RegistryProvider{ 
	public Registry getRegistry() throws Exception {
		HashMap<String, Object> options = new HashMap<String, Object>();
		final String sessionId = RHttpProxy.logOnDB(System.getProperty("dbhttp.url"), "", System.getProperty("dbhttp.login"), System.getProperty("dbhttp.password"), options);			
		DBLayerInterface db = (DBLayerInterface)RHttpProxy.getDynamicProxy(System.getProperty("dbhttp.url"), sessionId, "REGISTRY", new Class<?>[]{DBLayerInterface.class}, new HttpClient(new MultiThreadedHttpConnectionManager()));
		return db;
	}
}
