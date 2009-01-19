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
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.kchine.r.server.RServices;
import org.kchine.rpf.PoolUtils;


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