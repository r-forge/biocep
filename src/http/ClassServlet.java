package http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ClassServlet extends HttpServlet {
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getRequestURL().toString();
		String resource = url.substring(url.indexOf("/classes") + "/classes".length());

		InputStream is = ClassServlet.class.getResourceAsStream(resource);
		System.out.println("url : " + url);
		System.out.println("requested resource : " + resource);
		System.out.println("is : " + is);

		if (is == null || resource.equals("")) {
			System.out.println("Going to send error");
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
			System.out.println("buffer length : " + buffer.length);
			if (url.endsWith(".class")) resp.setContentType("application/java");
			else resp.setContentType("text/plain");
			resp.getOutputStream().write(buffer);
			resp.getOutputStream().flush();
			resp.getOutputStream().close();
		}
	}

}