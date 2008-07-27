package bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class Boot {

	public static void main(final String[] args) throws Exception {
		try {
			boolean keepAlive = new Boolean(args[0]);
			URLClassLoader cl = null;

			System.out.println("Boot Args:" + Arrays.toString(args));
			Vector<URL> codeUrls=new Vector<URL>();
			codeUrls.add(new URL("http://" + args[1] + ":" + args[2] + "/classes/"));
			
			if (args.length > 3) {
				for (int i=3;i<args.length;++i) {
					codeUrls.add(new URL(args[i]));
				}
			}
			
			cl = new URLClassLoader( (URL[])codeUrls.toArray(new URL[0]), Boot.class.getClassLoader());

			if (!keepAlive) {
				cl.loadClass("server.ServerManager").getMethod("startPortInUseDogwatcher", new Class<?>[] { String.class, int.class, int.class, int.class })
						.invoke(null, args[1], Integer.decode(args[2]), 3, 3);
			}

			Class<?> mainClass = cl.loadClass("server.MainRServer");
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
			
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
