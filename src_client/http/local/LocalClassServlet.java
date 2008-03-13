package http.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalClassServlet extends HttpServlet {
	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getRequestURL().toString();
		String resource = url.substring(url.indexOf("/classes") + "/classes".length());
		if (resource.equals("")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);

		}
		
		InputStream is = LocalClassServlet.class.getResourceAsStream(resource);
		if (is==null && resource.endsWith(".class")) {
			System.out.println("--> trying to load missing class :"+resource);
			String className=resource.substring(1, resource.indexOf(".class")).replace('/', '.');
			System.out.println("--> class name:"+className);
			try {
				LocalClassServlet.class.getClassLoader().loadClass(className);
				is = LocalClassServlet.class.getResourceAsStream(resource);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//System.out.println("url : " + url);
		//System.out.println("requested resource stream: " + resource);
		//System.out.println("requested resource url: " + LocalClassServlet.class.getResource(resource));
		//System.out.println("is : " + is);

		if (is == null || resource.equals("")) {
			//System.out.println("Going to send error");
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);

		} else {
			byte[] buffer = new byte[is.available()];
			try {
				
				for (int i = 0; i < buffer.length; ++i) {
					int b = is.read();
					buffer[i] = (byte) b;
				}
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			//System.out.println("buffer length : " + buffer.length);
			if (url.endsWith(".class")) resp.setContentType("application/java");
			else resp.setContentType("text/plain");
			//resp.setContentLength(buffer.length);
			//resp.getOutputStream().flush();
			resp.getOutputStream().write(buffer);
			resp.getOutputStream().flush();
			resp.getOutputStream().close();
		}
	}

	public static void main(String[] args) throws Exception {
		URLClassLoader cl=new URLClassLoader(new URL[]{new URL("http://127.0.0.1:9999/classes/")},null);
		System.out.println(cl.getResource("rjbmaps.properties"));		
	}
}