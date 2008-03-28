package bootstrap;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class Boot {

	public static void main(final String[] args) throws Exception {
		try {
			boolean keepAlive = new Boolean(args[0]);
			URLClassLoader cl = null;
			if (keepAlive) {
				String jarsUrlPrefix = args[3];
				cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/"), new URL(jarsUrlPrefix + "JRI.jar"),
						new URL(jarsUrlPrefix + "commons-logging-1.1.jar"), new URL(jarsUrlPrefix + "log4j-1.2.14.jar"),
						new URL(jarsUrlPrefix + "htmlparser.jar"), new URL(jarsUrlPrefix + "derbyclient.jar"), new URL(jarsUrlPrefix + "RJB.jar"),
						new URL(jarsUrlPrefix + "mapping.jar") }, Boot.class.getClassLoader());
			} else {
				cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/") }, Boot.class.getClassLoader());
				cl.loadClass("server.ServerLauncher").getMethod("startPortInUseDogwatcher",
						new Class<?>[] { String.class, int.class, int.class, int.class }).invoke(null, args[1], Integer.decode(args[2]), 3, 3);
			}
			Class<?> mainClass = cl.loadClass("server.MainRServer");
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
