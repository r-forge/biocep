package http.local;

import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;
import http.TunnelingException;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.ebi.microarray.pools.PoolUtils;

public class LocalGraphicsServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private RGui _rgui;
	public LocalGraphicsServlet(RGui rgui) {
		super();
		_rgui=rgui;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		Object result = null;
		
		do {
			
			GDDevice device = null;
			try {
				_rgui.getRLock().lock();
				Integer width=null;
				try {width=Integer.decode(request.getParameter("width"));} catch (Exception e) {}
				if (width==null) width=600;
				
				Integer height=null;
				try {height=Integer.decode(request.getParameter("height"));} catch (Exception e) {}
				if (height==null) height=400;
				
				
				String command = request.getParameter("expression");
				if (command==null) {command="hist(rnorm(100))";}
				
				
				device = _rgui.getR().newDevice(width,height);				
				_rgui.getR().sourceFromBuffer(new StringBuffer(command));				
				JGDPanelPop panel = new JGDPanelPop(device, false, false, null,null,null);
				panel.popNow();
				response.setContentType("image/jpeg");
	            ImageIO.write(panel.getImage(), "jpg", response.getOutputStream());
	    		response.getOutputStream().flush();
	    		response.getOutputStream().close();
	            return ;				
			} catch (Exception e) {
				result=e;
				break;
			}finally {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doAny(request, response);
	}
	
}