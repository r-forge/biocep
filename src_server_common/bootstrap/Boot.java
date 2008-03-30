package bootstrap;

import java.io.File;
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
				cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/"), new URL(jarsUrlPrefix + "biocep.jar")
				}, Boot.class.getClassLoader());
			} else {
				cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/") }, Boot.class.getClassLoader());
				cl.loadClass("server.ServerManager").getMethod("startPortInUseDogwatcher",
						new Class<?>[] { String.class, int.class, int.class, int.class }).invoke(null, args[1], Integer.decode(args[2]), 3, 3);
			}
			Class<?> mainClass = cl.loadClass("server.MainRServer");
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
