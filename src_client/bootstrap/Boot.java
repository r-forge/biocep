package bootstrap;
import java.net.URL;
import java.net.URLClassLoader;

public class Boot {
		
	public static void main(final String[] args) throws Exception{
		URLClassLoader cl=new URLClassLoader(new URL[]{ new URL("http://"+args[0]+":"+args[1]+"/classes/")}, Boot.class.getClassLoader());
		cl.loadClass("uk.ac.ebi.microarray.pools.PoolUtils").getMethod("startPortInUseDogwatcher", new Class<?>[]{String.class,int.class,int.class,int.class}).invoke(null, args[0],Integer.decode(args[1]), 3, 3);
		Class<?> mainClass=cl.loadClass("server.MainRServer");		
		mainClass.getMethod("main", new Class<?>[]{String[].class}).invoke(null, new Object[]{new String[]{}});		
	}
}
