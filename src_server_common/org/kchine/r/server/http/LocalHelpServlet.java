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
package org.kchine.r.server.http;

import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kchine.r.server.RKit;
import org.kchine.rpf.PoolUtils;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LocalHelpServlet extends javax.servlet.http.HttpServlet {

	private RKit _rgui;

	public LocalHelpServlet(RKit rgui) {
		super();
		_rgui = rgui;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Object result = null;
		try {
			do {

				String uri = request.getRequestURI();

				String helpuri = uri.substring(uri.indexOf("/helpme") + "/helpme".length());
				if (helpuri.indexOf(";jsessionid") != -1) {
					helpuri = helpuri.substring(0, helpuri.indexOf(";jsessionid"));
				}
				// System.out.println("helpuri:"+helpuri);

				if (uri.toLowerCase().endsWith(".jpg")) {
					response.setContentType("image/jpeg");
				} else {
					response.setContentType("text/html; charset=utf-8");
				}

				// System.out.println("uri="+uri);
				result = _rgui.getR().getRHelpFile(helpuri);

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

}
