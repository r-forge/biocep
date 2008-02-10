package bootstrap;
import java.net.URL;
import java.net.URLClassLoader;

public class Boot {
	public static void main(String[] args) throws Exception{		
		URLClassLoader cl=new URLClassLoader(new URL[]{ new URL("http://127.0.0.1:"+args[0]+"/classes/")}, Boot.class.getClassLoader());
		Class<?> mainClass=cl.loadClass("uk.ac.ebi.microarray.pools.MainServer");		
		mainClass.getMethod("main", new Class<?>[]{String[].class}).invoke(null, new Object[]{new String[]{}});		
	}
}
