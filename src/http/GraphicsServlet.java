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
package http;

import graphics.pop.GDDevice;
import graphics.rmi.GUtils;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class GraphicsServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	public GraphicsServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		Object result = null;
		do {

			long t1=System.currentTimeMillis();
			ServantProviderFactory spFactory = ServantProviderFactory.getFactory();
			if (spFactory == null) {
				result = new NoRegistryAvailableException();
				break;
			}

			long t2=System.currentTimeMillis();
			RServices r = null;
			try {

				boolean wait = true;
				if (wait) {
					r = (RServices) spFactory.getServantProvider().borrowServantProxy();
				} else {
					r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
				}
				if (r == null) {
					result = new NoServantAvailableException();
					break;
				}
				long t3=System.currentTimeMillis();
				GDDevice device = r.newDevice(Integer.decode(request.getParameter("width")), Integer.decode(request.getParameter("height")));
				long t4=System.currentTimeMillis();
				
				String command = request.getParameter("expression");
				if (command==null) {command="hist(rnorm(100))";}				
				r.sourceFromBuffer(new StringBuffer(command));				
				long t5=System.currentTimeMillis();
				JGDPanelPop panel = new JGDPanelPop(device, false, false, null,null,null);
				panel.popNow();
				long t6=System.currentTimeMillis();
				response.setContentType("image/jpg");
	            ImageIO.write(panel.getImage(), "jpg", response.getOutputStream());
	            long t7=System.currentTimeMillis();
	            
	            System.out.println("delta1:"+(t2-t1));
	            System.out.println("delta2:"+(t3-t2));
	            System.out.println("delta3:"+(t4-t3));
	            System.out.println("delta4:"+(t5-t4));
	            System.out.println("delta5:"+(t6-t5));
	            System.out.println("delta6:"+(t7-t6));
	            
	            
	            
	            return ;

			} catch (Exception e) {
				result = e;
				break;
			} finally {
				spFactory.getServantProvider().returnServantProxy(r);
			}

		} while (true);

		String log = null;
		if (result instanceof TunnelingException) {
			log = PoolUtils.getStackTraceAsString((Throwable) result);
		} else if (result instanceof Throwable) {
			log = PoolUtils.getStackTraceAsString((Throwable) result);
		} else {
			log = result.toString();
		}

		response.setContentType("text/html");
		response.getWriter().println("<html><head></head><body>");
		response.getWriter().println(log);
		response.getWriter().println("</body></html>");
		response.flushBuffer();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {

	}

}