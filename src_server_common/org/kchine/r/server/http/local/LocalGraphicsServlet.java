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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kchine.r.server.RKit;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.http.Java2DUtils;
import org.kchine.r.server.http.frontend.TunnelingException;
import org.kchine.rpf.PoolUtils;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LocalGraphicsServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private RKit _rgui;

	public LocalGraphicsServlet(RKit rgui) {
		super();
		_rgui = rgui;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Object result = null;

		do {

			GDDevice device = null;
			try {
				_rgui.getRLock().lock();
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

				device = _rgui.getR().newDevice(width, height);
				_rgui.getR().sourceFromBuffer(command);				
				BufferedImage bufferedImage = Java2DUtils.getBufferedImage(new Point(0, 0), new Dimension(width,height), device.popAllGraphicObjects(-1)); 				
				response.setContentType("image/jpeg");
				ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
				response.getOutputStream().flush();
				response.getOutputStream().close();
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
				_rgui.getRLock().unlock();
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

		response.getOutputStream().flush();
		response.getOutputStream().close();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

}