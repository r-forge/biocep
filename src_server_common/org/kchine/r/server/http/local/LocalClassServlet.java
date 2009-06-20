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
package org.kchine.r.server.http.local;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLClassLoader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LocalClassServlet extends HttpServlet {

	ClassLoader cl = this.getClass().getClassLoader();

	public static int BUFFER_SIZE = 1024 * 64;

	public void init() throws ServletException {
		super.init();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doAny(req, resp);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doAny(req, resp);
	}

	protected void doAny(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String url = req.getRequestURL().toString();
		String resource = url.substring(url.indexOf("/classes") + "/classes".length());

		if (resource.equals("")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		if (resource.endsWith(".class")) {
			String className = resource.substring(1, resource.indexOf(".class")).replace('/', '.');
			Class<?> c = null;
			try {
				c = cl.loadClass(className);
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			resp.setContentType("application/java");

			InputStream is = cl.getResourceAsStream(resource.substring(1));
			resp.setContentType("text/plain");
			byte data[] = new byte[BUFFER_SIZE];
			int count = 0;
			while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
				resp.getOutputStream().write(data, 0, count);
				resp.getOutputStream().flush();
			}
			resp.getOutputStream().close();

		} else {
			InputStream is = cl.getResourceAsStream(resource.substring(1));
			if (is == null) {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				resp.setContentType("text/plain");
				byte data[] = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
					resp.getOutputStream().write(data, 0, count);
					resp.getOutputStream().flush();
				}
				resp.getOutputStream().close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ClassLoader cl = new URLClassLoader(new URL[] { new URL("http://www.biocep.net/bbb/bin/") }, null);
		System.out.println(cl.getResource("org/kchine/r/server/http/local/LocalClassServlet.class"));
	}
}