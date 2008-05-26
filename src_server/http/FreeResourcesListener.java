/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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
package http;

import graphics.pop.GDDevice;

import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import remoting.RServices;
import server.ServerManager;

import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine   k.chine@imperial.ac.uk
 */
public class FreeResourcesListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent sessionEvent) {
		System.out.println("Session created :" + sessionEvent.getSession().getId());
	}

	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		System.out.println("Session to destroy :" + sessionEvent.getSession().getId());

		HashMap<String, HashMap<String, Object>> map = ((HashMap<String, HashMap<String, Object>>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP"));
		if (map == null)
			return;
		HashMap<String, Object> attributes = map.get(sessionEvent.getSession().getId());

		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_MAP")).remove(sessionEvent.getSession().getId());
		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).remove(sessionEvent.getSession().getId());
		
		
		
		if (attributes == null)	return;
		RServices rservices = (RServices) attributes.get("R");

		if (rservices == null)
			return;
		
		Vector<HttpSession> r_sessions=((HashMap<RServices, Vector<HttpSession>>) sessionEvent.getSession().getServletContext().getAttribute("R_SESSIONS")).get(rservices);
		
		System.out.println("sessions linked to this r :"+r_sessions);
		for (int i=0; i<r_sessions.size();++i) System.out.print(r_sessions.elementAt(i).getId()+" ");  System.out.println();

		r_sessions.remove(sessionEvent.getSession());
		
		if (r_sessions.size()>0) {
			System.out.println("attributes:"+attributes);	
			for (String a:attributes.keySet()) {
				if (a.startsWith("device_")) {
					try {
						System.out.println("disposing device :"+a);
						((GDDevice)attributes.get(a)).dispose();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		String login = (String) attributes.get("LOGIN");
		boolean nopool = (Boolean) attributes.get("NOPOOL");
		boolean save = (Boolean) attributes.get("SAVE");
		boolean namedAccessMode = (Boolean) attributes.get("NAMED_ACCESS_MODE");
		String processId = (String) attributes.get("PROCESS_ID");
		String privatename = (String) attributes.get("PRIVATE_NAME");

		if (save) {
			try {
				UserUtils.saveWorkspace((String) attributes.get("LOGIN"), rservices);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!namedAccessMode) {
			if (nopool) {
				
				if (privatename==null || privatename.equals("")) {
					try {
						ServerManager.killLocalProcess(processId, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				/*
				try {
					Registry registry = ServantProviderFactory.getFactory().getServantProvider().getRegistry();
					NodeManager nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
					nm.kill(rservices);
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
				
				
			} else {
				try {
					ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(rservices);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {

		}

	}
}
