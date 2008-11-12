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
public class MappingClassServlet extends HttpServlet {
	
	public static int BUFFER_SIZE=1024*16;
	private static ClassLoader _mappingClassLoader=null;
	
	
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
		
		if (_mappingClassLoader==null) {
			
			/*
			String jar=getClass().getResource("/maps/rjbmaps.xml").toString();
			jar=jar.substring("jar:".length(), jar.length()- "/maps/rjbmaps.xml".length()-1);
			System.out.println("--!->jar:"+jar);
			_mappingClassLoader=new URLClassLoader(new URL[]{new URL(jar)},null);
			*/			
			
			_mappingClassLoader=getClass().getClassLoader();
		}
		 
		String url = req.getRequestURL().toString();
		
		String resource = url.substring(url.indexOf("/classes") + "/classes/".length()).trim();
		
		if (resource.equals("")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		InputStream is = null;
		is = _mappingClassLoader.getResourceAsStream(resource);
		if (is == null && resource.endsWith(".class")) {			
			String className = resource.substring(0, resource.indexOf(".class")).replace('/', '.');			
			try {
				_mappingClassLoader.loadClass(className);
				is = _mappingClassLoader.getResourceAsStream(resource);
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}

		if (is == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			if (url.endsWith(".class"))
				resp.setContentType("application/java");
			else
				resp.setContentType("text/plain");
									
			byte data[] = new byte[BUFFER_SIZE];
			int count=0;
			while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
				resp.getOutputStream().write(data, 0, count);
				resp.getOutputStream().flush();
			}
			
			resp.getOutputStream().close();			
		}
	}

	public static void main(String[] args) throws Exception {
		URLClassLoader cl = new URLClassLoader(new URL[] { new URL("http://127.0.0.1:9999/classes/") }, null);
		System.out.println(cl.getResource("rjbmaps.properties"));
	}
}