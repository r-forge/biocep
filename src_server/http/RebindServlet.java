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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.kchine.r.server.RServices;
import org.kchine.r.server.Utils;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.YesSecurityManager;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RebindServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RebindServlet.class);

	public RebindServlet() {
		super();
		PoolUtils.initLog4J();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		try {
			
			String[] names=ServerDefaults.getRmiRegistry().list();
			Properties props=new Properties();
			Map<String, String> map=request.getParameterMap();
			for (String s:map.keySet()) props.put(s, map.get(s));
			
			for (int i=0;i<names.length;++i) {
				try {
					RServices r=(RServices)ServerDefaults.getRmiRegistry().lookup(names[i]);
					String newName=r.export(props, request.getParameter("prefix"), true);
					response.getOutputStream().println("<"+names[i]+"> rebinded to <"+newName+">");
				} catch (Exception e) {
					response.getOutputStream().println("Couldn't Rebind <"+names[i]+">");
				}
			}
			
			response.getOutputStream().println("Rebind succeded");
			response.flushBuffer();

		} catch (Throwable e) {
			
			response.getOutputStream().println(Utils.getStackTraceAsString(e));
			response.flushBuffer();
			
		}

	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		log.info("command servlet init");
		PoolUtils.injectSystemProperties(true);ServerDefaults.init();
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}
	}
	

}