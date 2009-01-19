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
package http;



import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.kchine.r.server.GenericCallbackDevice;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.SSHTunnelingProxy;
import org.kchine.rpf.SSHUtils;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.ServantProviderFactory;

import server.GenericCallbackDeviceImpl;


/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class FreeResourcesListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent sessionEvent) {
		System.out.println(" % Session created :" + sessionEvent.getSession().getId());
	}

	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		System.out.println(" % Session to destroy :" + sessionEvent.getSession().getId());

		HashMap<String, HashMap<String, Object>> map = ((HashMap<String, HashMap<String, Object>>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP"));
		if (map == null)return;
		HashMap<String, Object> attributes = map.get(sessionEvent.getSession().getId());

		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_MAP")).remove(sessionEvent.getSession().getId());
		((HashMap<String, HttpSession>) sessionEvent.getSession().getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).remove(sessionEvent.getSession().getId());
		
		if (attributes == null)	return;
		
		if (!attributes.get("TYPE").equals("RS")) return;
		
		RServices rservices = (RServices) attributes.get("R");

		if (rservices == null)
			return;
		
		Vector<HttpSession> r_sessions=((HashMap<RServices, Vector<HttpSession>>) sessionEvent.getSession().getServletContext().getAttribute("R_SESSIONS")).get(rservices);
		
		System.out.println("sessions linked to this r :"+r_sessions);
		for (int i=0; i<r_sessions.size();++i) System.out.print(r_sessions.elementAt(i).getId()+" ");  System.out.println();

		r_sessions.remove(sessionEvent.getSession());
		
		System.out.println("attributes:"+attributes);	
		for (String a:attributes.keySet()) {
			if (a.startsWith(GenericCallbackDeviceImpl.GenericCallbackDeviceIdPrefix)) {
				try {
					System.out.println("disposing device :"+a);
					((GenericCallbackDevice)attributes.get(a)).dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if (!(Boolean) attributes.get("SELFISH")) {
			boolean removeDevices=false;
			
			try {
				 removeDevices=rservices.hasRCollaborationListeners();
				 //removeDevices=r_sessions.size()>0;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			System.out.println("----> remove devices :"+removeDevices);
			if (removeDevices) {
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
		}

		String login = (String) attributes.get("LOGIN");
		boolean nopool = (Boolean) attributes.get("NOPOOL");
		boolean save = (Boolean) attributes.get("SAVE");
		boolean namedAccessMode = (Boolean) attributes.get("NAMED_ACCESS_MODE");
		String processId = (String) attributes.get("PROCESS_ID");
		String jobId = (String) attributes.get("JOB_ID");
		String privatename = (String) attributes.get("PRIVATE_NAME");
		boolean isRelay= (Boolean) attributes.get("IS_RELAY");

		if (isRelay) return ;
		
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
					
					if (System.getProperty("submit.mode")!=null && System.getProperty("submit.mode").equals("ssh")) {
						try {
							SSHUtils.execSsh( System.getProperty("submit.ssh.kill")+" "+jobId, System.getProperty("submit.ssh.host"), Integer.decode(System.getProperty("submit.ssh.port")) ,System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					} else {
						try {
							ServerManager.killLocalProcess(processId, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
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
				if (System.getProperty("submit.mode").equals("ssh")) {
					ServantProvider servantProvider =(ServantProvider)SSHTunnelingProxy.getDynamicProxy(
			        		System.getProperty("submit.ssh.host") ,Integer.decode(System.getProperty("submit.ssh.port")),System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home"),
			                "java -Dpools.provider.factory=org.kchine.rpf.db.ServantsProviderFactoryDB -Dpools.dbmode.defaultpoolname=R -Dpools.dbmode.shutdownhook.enabled=false -cp %{install.dir}/biocep-core.jar org.kchine.rpf.SSHTunnelingWorker %{file}",
			                "servant.provider",new Class<?>[]{ServantProvider.class});
					servantProvider.returnServantProxy(rservices);
				} else {				
					try {
						ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(rservices);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {

		}

	}
}
