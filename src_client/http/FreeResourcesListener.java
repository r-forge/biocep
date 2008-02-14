/*
 * Copyright (C) 2007 EMBL-EBI
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

import java.rmi.registry.Registry;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.NodeManager;

import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class FreeResourcesListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent sessionEvent) {
		System.out.println("Session created :" + sessionEvent.getSession().getId());
	}

	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		System.out.println("Session to destroy :" + sessionEvent.getSession().getId());

		HashMap<String, HashMap<String, Object>> map = ((HashMap<String, HashMap<String, Object>>) sessionEvent
				.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP"));

		if (map == null)
			return;
		HashMap<String, Object> attributes = map.get(sessionEvent.getSession().getId());

		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_MAP"))
				.remove(sessionEvent.getSession().getId());
		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute(
				"SESSIONS_ATTRIBUTES_MAP")).remove(sessionEvent.getSession().getId());

		if (attributes == null)
			return;
		RServices rservices = (RServices) attributes.get("R");

		if (rservices == null)
			return;

		String login = (String) attributes.get("LOGIN");
		boolean nopool = (Boolean) attributes.get("NOPOOL");
		boolean save = (Boolean) attributes.get("SAVE");
		boolean namedAccessMode = (Boolean) attributes.get("NAMED_ACCESS_MODE");

		if (save) {
			try {
				UserUtils.saveWorkspace((String) attributes.get("LOGIN"), rservices);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!namedAccessMode) {
			if (nopool) {
				try {
					Registry registry = ServantProviderFactory.getFactory().getServantProvider().getRegistry();
					NodeManager nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
					nm.kill(rservices);
				} catch (Exception e) {
					e.printStackTrace();
				}
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