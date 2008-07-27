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
			String jar=getClass().getResource("/maps/rjbmaps.xml").toString();
			jar=jar.substring("jar:".length(), jar.length()- "/maps/rjbmaps.xml".length()-1);
			_mappingClassLoader=new URLClassLoader(new URL[]{new URL(jar)},null);			
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