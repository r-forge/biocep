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
import java.util.Arrays;
import java.util.Vector;

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

				String type = null;
				try {
					type = request.getParameter("type");
				} catch (Exception e) {
				}
				
				if (type == null)
					type = "jpg";
				
				
				Boolean wait = null;
				try {
					wait = new Boolean(request.getParameter("wait"));
				} catch (Exception e) {
				}
				if (wait == null) wait = false;
				
				
				if (wait) {
					r = (RServices) spFactory.getServantProvider().borrowServantProxy();
				} else {
					r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
				}
				
				if (r == null) {
					result = new NoServantAvailableException();
					break;
				}
				
				
				if (request.getParameter("demo")!=null) {
					System.out.println(Arrays.toString(r.listDemos()));
					String demoName=r.listDemos()[Integer.decode(request.getParameter("demo"))];					
					command=r.getDemoSource(demoName).toString();
					System.out.println(command);
				}
				
				
				if (type.equals("svg")) {					
					response.setContentType("image/svg+xml");
					Vector<String> svg=r.getSvg(command,400,400);
					for (int i=0; i<svg.size(); ++i) {
						response.getOutputStream().println(svg.elementAt(i));
					}					
				} else if (type.equals("pdf")) {		
					response.setContentType("application/pdf");
					response.getOutputStream().write(r.getPdf(command, 400,400));					
				} else if (type.equals("pdfapplet")) {
					pdfAppletHtml(request, response, r.getPdf(command, 400,400));
				} else {
					try {
					device = r.newDevice(width, height);	
					r.sourceFromBuffer(new StringBuffer(command));
					BufferedImage bufferedImage = Java2DUtils.getBufferedImage(new Point(0, 0), new Dimension(width,height), device.popAllGraphicObjects());
					response.setContentType("image/jpeg");
					ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
					} finally {
						try {
							device.dispose();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}					
				}

				return;

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

	private static void pdfAppletHtml(HttpServletRequest request, HttpServletResponse response, byte[] pdf)throws IOException {

		boolean isIE=request.getHeader("User-Agent").toLowerCase().indexOf("msie")!=-1;

		String resultBuffer=PoolUtils.bytesToHex(pdf);					
		int b=300;
		int d=resultBuffer.length()/b;
		int m=resultBuffer.length()%b;					
		
		
		response.setContentType("text/html");					
		response.getWriter().println("<html><head></head><body><center>");					
		response.getWriter().println("<!--[if !IE]> Firefox and others will use outer object -->");
		response.getWriter().println("<object align=\"center\" 	height=99%  width=99% classid=\"java:applet.PDFViewer\" archive=\"appletlibs/PDFRenderer_unsigned.jar,appletlibs/pdfviewer_unsigned.jar\" type = \"application/x-java-applet;version=1.5\" pluginspage = \"http://java.sun.com/products/plugin/index.html#download\"");
		response.getWriter().println("	embedded = \"true\"");
		
		if (!isIE) {
			response.getWriter().println("	pdfhex.block.number = \""+(m==0?d:d+1)+"\"");
			for (int i=0; i<d;++i) {
				response.getWriter().println("	pdfhex.block."+i+" = \""+resultBuffer.substring(i*b,i*b+b)+"\"");												
			}
			if (m>0) {
				response.getWriter().println("	pdfhex.block."+d+" = \""+resultBuffer.substring(d*b,resultBuffer.length())+"\"");
			}
		}
		 
		response.getWriter().println("><!--<![endif]--><!-- MSIE (Microsoft Internet Explorer) will use inner object -->"); 
		response.getWriter().println("<object align=\"center\" height=99% width=99% classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\">");
		response.getWriter().println("<param name=\"archive\" value=\"appletlibs/PDFRenderer_unsigned.jar,appletlibs/pdfviewer_unsigned.jar\">");
		response.getWriter().println("<param name=\"code\" value=\"applet.PDFViewer\">");
		response.getWriter().println("<param name = \"type\" value = \"application/x-java-applet;version=1.5\">");
		response.getWriter().println("<param name = \"embedded\" value = \"true\">");

		if (isIE) {
			response.getWriter().println("<param name = \"pdfhex.block.number\" value = \""+(m==0?d:d+1)+"\">");
			for (int i=0; i<d;++i) {
				response.getWriter().println("<param name = \""+"pdfhex.block."+i+"\" value = \""+resultBuffer.substring(i*b,i*b+b)+"\">");												
			}
			if (m>0) {
				response.getWriter().println("<param name = \""+"pdfhex.block."+d+"\" value = \""+resultBuffer.substring(d*b,resultBuffer.length())+"\">");
			}
		}
				
		response.getWriter().println("</object><!--[if !IE]> close outer object --></object><!--<![endif]--></div></center></body></html>");							
	
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {

	}

}