package bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class Boot {

	public static void main(final String[] args) throws Exception {
		try {
			boolean keepAlive = new Boolean(args[0]);
			URLClassLoader cl = null;
			
			System.out.println("Boot Args:"+Arrays.toString(args));
			URL codeUrl = args.length > 3 ? new URL(args[3]) : null ;  
			if (keepAlive) {				
				if (codeUrl!=null) {
					cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/"), codeUrl}, Boot.class.getClassLoader());
				} else {
					cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/")}, Boot.class.getClassLoader());					
				}
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
