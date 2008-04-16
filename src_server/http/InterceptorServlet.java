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

import java.io.IOException;
import java.net.URL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;
/**
 * @author Karim Chine k.chine@imperial.ac.uk
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