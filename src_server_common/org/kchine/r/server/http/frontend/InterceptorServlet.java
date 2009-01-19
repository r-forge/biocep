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
package org.kchine.r.server.http.frontend;

import java.io.IOException;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.kchine.rpf.PoolUtils.*;
/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class InterceptorServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	private static URL _mappingUrl;
	private static URL _rvirtualUrl;
	
	public static URL getRMappingUrl() {
		return _mappingUrl;
	}
	
	public static URL getRVirtualUrl() {
		return _rvirtualUrl;
	}

	public InterceptorServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doAny(req, resp);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		injectSystemProperties(true);
		if (_mappingUrl==null || _rvirtualUrl==null)  {
			URL thisUrl=new URL(request.getRequestURL().toString());
			_rvirtualUrl=new URL("http://"+thisUrl.getHost()+":"+thisUrl.getPort()+"/rvirtual/cmd");
			_mappingUrl=new URL("http://"+thisUrl.getHost()+":"+thisUrl.getPort()+request.getRequestURI().substring(0,request.getRequestURI().indexOf('/',1))+"/mapping/classes/" );
			if (System.getProperty("http.frontend.url")==null || System.getProperty("http.frontend.url").equals("")) {
				System.setProperty("http.frontend.url", _rvirtualUrl.toString());
			}
		}
		
		RequestDispatcher dispatcher=getServletContext().getNamedDispatcher("WSServlet");
		try {
			dispatcher.forward(request, response);
		} catch (Throwable e) {
			//e.printStackTrace();
		}
	}
	
	

}