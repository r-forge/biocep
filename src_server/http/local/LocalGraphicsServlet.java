package http.local;

import graphics.pop.GDDevice;
import http.TunnelingException;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rosuda.javaGD.GDObject;

import remoting.RKit;
import server.DirectJNI;
import server.Java2DUtils;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
				_rgui.getR().sourceFromBuffer(new StringBuffer(command));				
				BufferedImage bufferedImage = Java2DUtils.getBufferedImage(new Point(0, 0), new Dimension(width,height), device.popAllGraphicObjects()); 				
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