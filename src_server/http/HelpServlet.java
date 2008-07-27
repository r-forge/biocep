/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class HelpServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	public HelpServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = null;
		Object result = null;

		try {

			do {

				session = request.getSession(false);
				String uri = request.getRequestURI();

				//System.out.println("<"+uri+"><"+session+"><"+(session==null? "NULL" : session.getAttribute("R"))+">");

				if (session == null || session.getAttribute("R") == null) {
					Collection<HttpSession> sessions = ((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).values();
					if (sessions.size() > 0) {
						session = sessions.iterator().next();
					} else {
						result = new NotLoggedInException("No Valid Session available");
						break;
					}
				}

				final RServices r = (RServices) session.getAttribute("R");

				String helpuri = uri.substring(uri.indexOf("/helpme") + "/helpme".length());

				if (uri.toLowerCase().endsWith(".jpg")) {
					response.setContentType("image/jpeg");
				} else {
					response.setContentType("text/html; charset=utf-8");
				}

				//System.out.println("uri="+uri);
				result = r.getRHelpFile(helpuri);

				break;

			} while (true);

		} catch (Exception e) {
			e.printStackTrace();
			result = e;
		}

		if (result != null && result.getClass().equals(byte[].class)) {
			response.getOutputStream().write((byte[]) result);
		}
		if (result != null && result instanceof Throwable) {
			((Throwable) result).printStackTrace();
			response.getOutputStream().print(PoolUtils.getStackTraceAsString((Throwable) result));
		} else {
			new ObjectOutputStream(response.getOutputStream()).writeObject(result);
		}

		response.flushBuffer();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

}