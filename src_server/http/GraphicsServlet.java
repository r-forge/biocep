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

import graphics.pop.GDDevice;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import remoting.RServices;
import server.Java2DUtils;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GraphicsServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	public GraphicsServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Object result = null;
		do {

			ServantProviderFactory spFactory = ServantProviderFactory.getFactory();
			if (spFactory == null) {
				result = new NoRegistryAvailableException();
				break;
			}

			RServices r = null;
			GDDevice device = null;
			try {

				boolean wait = false;
				if (wait) {
					r = (RServices) spFactory.getServantProvider().borrowServantProxy();
				} else {
					r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
				}
				if (r == null) {
					result = new NoServantAvailableException();
					break;
				}
				Integer width = null;
				try {
					width = Integer.decode(request.getParameter("width"));
				} catch (Exception e) {
				}
				if (width == null)
					width = 600;

				Integer height = null;
				try {
					height = Integer.decode(request.getParameter("height"));
				} catch (Exception e) {
				}
				if (height == null)
					height = 400;

				String command = request.getParameter("expression");
				if (command == null) {
					command = "hist(rnorm(100))";
				}

				device = r.newDevice(width, height);
				r.sourceFromBuffer(new StringBuffer(command));
				BufferedImage bufferedImage = Java2DUtils.getBufferedImage(new Point(0, 0), new Dimension(width,height), device.popAllGraphicObjects());
				response.setContentType("image/jpeg");
				ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

				return;

			} catch (Exception e) {
				result = e;
				break;
			} finally {
				try {
					device.dispose();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {

	}

}