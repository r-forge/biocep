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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ConfigServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	public ConfigServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		for (Object key : request.getParameterMap().keySet()) {
			System.setProperty((String) key, request.getParameter((String) key));
		}
		ServletOutputStream out = response.getOutputStream();
		out.println("<html>\n<head>\n</head>\n<body>\n");
		out.println("-----------------<BR>");
		out.println("<strong>System Properties:</strong><BR>");
		out.println("-----------------<BR>");
		for (Object key : PoolUtils.orderO(System.getProperties().keySet())) {
			out.println("<strong>" + key + "</strong> = " + System.getProperty((String) key) + "<BR>");
		}
		out.println("-----------------<BR>");
		out.println("<strong>Environment Variables:</strong><BR>");
		out.println("-----------------<BR>");
		for (String key : PoolUtils.orderS(System.getenv().keySet())) {
			out.println("<strong>" + key + "</strong> = " + System.getenv(key) + "<BR>");
		}
		out.println("<body>\n");
		out.flush();
	}

}