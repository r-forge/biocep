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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kchine.r.server.RKit;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServantProviderFactory;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GraphicsServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private RKit _rkit;
	private boolean _downloadpdflibs=false;
	boolean _firstPdfApplet=true;
	
	public GraphicsServlet() {
		super();
	}
	
	public GraphicsServlet(RKit rkit) {
		super();
		_rkit=rkit;
	}

	public GraphicsServlet(boolean downloadpdflibs) {
		super();
		_downloadpdflibs=downloadpdflibs;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setHeader("Cache-Control","no-cache");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader("Expires", 0);
		
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
					type = "gif";

				if (_rkit==null) {
					Boolean wait = null;
					try {
						wait = new Boolean(request.getParameter("wait"));
					} catch (Exception e) {
					}
					if (wait == null)
						wait = true;
	
					if (wait) {
						r = (RServices) spFactory.getServantProvider().borrowServantProxy();
					} else {
						r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
					}
	
					if (r == null) {
						result = new NoServantAvailableException();
						break;
					}
				} else {
					r=_rkit.getR();
				}

				if (request.getParameter("demo") != null) {
					System.out.println(Arrays.toString(r.listDemos()));
					String demoName = r.listDemos()[Integer.decode(request.getParameter("demo"))];
					command = r.getDemoSource(demoName).toString();
					System.out.println(command);
				}

				if (request.getParameter("url") != null) {
					
					System.out.println("url:"+request.getParameter("url"));
					StringBuffer commandBuffer=new StringBuffer();
					Enumeration<String> allParamnames=request.getParameterNames();
					while (allParamnames.hasMoreElements()) {
						String name=allParamnames.nextElement();
						if (name.startsWith("input_")) {							
							commandBuffer.append(name.substring("input_".length())+"='"+request.getParameter(name)+"'\n");
						}
					}
									
					URLConnection urlC = new URL(request.getParameter("url")).openConnection();
					BufferedReader br=new BufferedReader(new InputStreamReader(urlC.getInputStream()));
					String line=null;					
					while((line=br.readLine())!=null) {commandBuffer.append(line);commandBuffer.append('\n');}
					command=commandBuffer.toString();
					System.out.println(command);
				}
				
				
				response.setDateHeader("Last-Modified ", System.currentTimeMillis());
				if (type.equals("svg")) {
					
					response.setContentType("image/svg+xml");
					response.getOutputStream().write(r.getSvg(command, width, height));
					System.out.println(r.getStatus());
					
					
				} else if (type.equals("pdf")) {

					response.setContentType("application/pdf");
					response.getOutputStream().write(r.getPdf(command, width, height));
					
					

				} else if (type.equals("pdfapplet")) {

					if (_firstPdfApplet && (_rkit!=null || _downloadpdflibs)) {
						
						_firstPdfApplet=false;
						
						try {
							PoolUtils.cacheJar(new URL("http://biocep.net/pdflibs/pdfviewer_unsigned.jar"), new File(ServerManager.WWW_DIR).getAbsolutePath(), PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, false);
							PoolUtils.cacheJar(new URL("http://biocep.net/pdflibs/PDFRenderer_unsigned.jar"), new File(ServerManager.WWW_DIR).getAbsolutePath(), PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, false);
						}  catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					response.setContentType("text/html");
					boolean isIE=request.getHeader("User-Agent").toLowerCase().indexOf("msie") != -1;
					pdfAppletHtml(response.getWriter(), r.getPdf(command, width, height), isIE, !isIE);
					

				} if (type.equals("png")) {
					
					response.setContentType("image/png");
					response.getOutputStream().write(r.getPng(command, width, height));
					System.out.println(r.getStatus());
					
					
				} else {
					
					response.setContentType("image/"+type);
					response.getOutputStream().write(r.getFromImageIOWriter(command, width, height, type));
					
					
					/*
					try {
						device = r.newDevice(width, height);
						r.sourceFromBuffer(command);
						BufferedImage bufferedImage = Java2DUtils
								.getBufferedImage(new Point(0, 0), new Dimension(width, height), device.popAllGraphicObjects(-1));
						response.setContentType("image/jpeg");
						ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
					} finally {
						try {
							device.dispose();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					*/
				}

				response.flushBuffer();

				return;

			} catch (Exception e) {
				result = e;
				break;
			} finally {

				if (_rkit==null) spFactory.getServantProvider().returnServantProxy(r);
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
	}

	public static void pdfAppletHtml(PrintWriter pw, byte[] pdf, boolean embedForIE, boolean embedForMozilla) throws IOException {

		String resultBuffer = PoolUtils.bytesToHex(pdf);
		int b = 1024*64;
		int d = resultBuffer.length() / b;
		int m = resultBuffer.length() % b;
		
		pw.println("<html><head></head><body><center>");
		pw.println("<!--[if !IE]> Firefox and others will use outer object -->");
		pw.println("<object align=\"center\" 	height=99%  width=99% classid=\"java:applet.PDFViewer\" archive=\"appletlibs/PDFRenderer_unsigned.jar,appletlibs/pdfviewer_unsigned.jar\" type = \"application/x-java-applet;version=1.5\" pluginspage = \"http://java.sun.com/products/plugin/index.html#download\"");
		pw.println("	embedded = \"true\"");

		if (embedForMozilla) {
			pw.println("	pdfhex.block.number = \"" + (m == 0 ? d : d + 1) + "\"");
			for (int i = 0; i < d; ++i) {
				pw.println("	pdfhex.block." + i + " = \"" + resultBuffer.substring(i * b, i * b + b) + "\"");
			}
			if (m > 0) {
				pw.println("	pdfhex.block." + d + " = \"" + resultBuffer.substring(d * b, resultBuffer.length()) + "\"");
			}
		}
		pw.println("><!--<![endif]--><!-- MSIE (Microsoft Internet Explorer) will use inner object -->");
		pw.println("<object align=\"center\" height=99% width=99% classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\">");
		pw.println("<param name=\"archive\" value=\"appletlibs/PDFRenderer_unsigned.jar,appletlibs/pdfviewer_unsigned.jar\">");
		pw.println("<param name=\"code\" value=\"applet.PDFViewer\">");
		pw.println("<param name = \"type\" value = \"application/x-java-applet;version=1.5\">");
		pw.println("<param name = \"embedded\" value = \"true\">");

		if (embedForIE) {
			pw.println("<param name = \"pdfhex.block.number\" value = \"" + (m == 0 ? d : d + 1) + "\">");
			for (int i = 0; i < d; ++i) {
				pw.println("<param name = \"" + "pdfhex.block." + i + "\" value = \"" + resultBuffer.substring(i * b, i * b + b) + "\">");
			}
			if (m > 0) {
				pw.println(
						"<param name = \"" + "pdfhex.block." + d + "\" value = \"" + resultBuffer.substring(d * b, resultBuffer.length()) + "\">");
			}
		}
		pw.println("</object><!--[if !IE]> close outer object --></object><!--<![endif]--></div></center></body></html>");
		pw.println("<!-- This applet was generated with Biocep : www.biocep.net , Biocep is a GPL Software , (C) 2007-2009 Karim Chine -->");
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {

	}
	
	
	static public void main(String[] args) throws Exception{
		URLConnection urlC = new URL("http://www.biocep.net/kaleidoscope/index.html").openConnection();
		BufferedReader br=new BufferedReader(new InputStreamReader(urlC.getInputStream()));
		String line=null;
		StringBuffer commandBuffer=new StringBuffer();
		while((line=br.readLine())!=null) {commandBuffer.append(line);commandBuffer.append('\n');}
		System.out.println(commandBuffer);

	}

}