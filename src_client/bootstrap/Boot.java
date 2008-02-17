package bootstrap;
import java.net.URL;
import java.net.URLClassLoader;

public class Boot {
		
	public static void main(final String[] args) throws Exception{
		URLClassLoader cl=new URLClassLoader(new URL[]{ new URL("http://127.0.0.1:"+args[0]+"/classes/")}, Boot.class.getClassLoader());
		cl.loadClass("uk.ac.ebi.microarray.pools.PoolUtils").getMethod("startPortInUseDogwatcher", new Class<?>[]{int.class,int.class,int.class}).invoke(null, Integer.decode(args[0]), 3, 3);
		Class<?> mainClass=cl.loadClass("server.MainRServer");		
		mainClass.getMethod("main", new Class<?>[]{String[].class}).invoke(null, new Object[]{new String[]{}});		
	}
}
