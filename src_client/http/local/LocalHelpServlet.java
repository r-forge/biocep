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
package http.local;

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class LocalHelpServlet extends javax.servlet.http.HttpServlet {
	private RServices _r;

	public LocalHelpServlet(RServices r) {
		super();
		_r = r;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		Object result = null;
		try {
			do {

				String uri = request.getRequestURI();

				String helpuri = uri.substring(uri.indexOf("/helpme") + "/helpme".length());
				if (helpuri.indexOf(";jsessionid") != -1) {
					helpuri = helpuri.substring(0, helpuri.indexOf(";jsessionid"));
				}
				//System.out.println("helpuri:"+helpuri);	

				if (uri.toLowerCase().endsWith(".jpg")) {
					response.setContentType("image/jpeg");
				} else {
					response.setContentType("text/html; charset=utf-8");
				}

				//System.out.println("uri="+uri);
				result = _r.getRHelpFile(helpuri);

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
