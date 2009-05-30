
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import org.kchine.r.server.RServices;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.db.ServantProviderDB;
import org.kchine.scilab.server.ScilabServices;

public class BiocepNoServer {

	
	public static void main(String[] args) throws Exception{
		
		String stub="ACED00057372002A6F72672E6B6368696E652E722E7365727665722E696D706C2E5253657276616E74496D706C5F5374756200000000000000020200007872001A6A6176612E726D692E7365727665722E52656D6F746553747562E9FEDCC98BE1651A0200007872001C6A6176612E726D692E7365727665722E52656D6F74654F626A656374D361B4910C61331E03000078707735000A556E6963617374526566000C36372E3230322E32332E31340000C561262EA04B177EB15B40A7B1220000012190E24A4D80010078";
		final ScilabServices sci = (ScilabServices)PoolUtils.hexToStub(stub, new URLClassLoader(new URL[]{new File("E:/workspace/biocep/bin/").toURI().toURL()},BiocepNoServer.class.getClassLoader()));
		final RServices r =(RServices)sci;
		System.out.println("sci="+sci);		
		//r.consoleSubmit("4+5");System.out.println(r.getStatus());
		System.out.println(sci.scilabConsoleSubmit("disp(4+5)"));
		
		
		
		/*
		final RServices r = ServerManager.createR("toto");
		((ScilabServices)r).scilabExec("disp(7+9)");
		r.die();
		
		*/
		
		
		/*
		String session=RHttpProxy.logOn("http://10.0.1.6:8080/rvirtual/cmd", "", "guest", "guest", new String[]{"privatename=toto"});
		System.out.println (Arrays.toString(RHttpProxy.listWorkers("http://10.0.1.6:8080/rvirtual/cmd", session)));
		*/
	}
	public static void main__(String[] args) throws Exception{
		System.out.println("------------------");
		Properties props=new Properties();		
		//props.setProperty("pools.dbmode.type","derby");
		//props.setProperty("pools.dbmode.host","localhost");
		//props.setProperty("pools.dbmode.port","1527");		
		//props.setProperty("pools.dbmode.name","DWEP");
		//props.setProperty("pools.dbmode.user","DWEP");
		//props.setProperty("pools.dbmode.password","DWEP");			
		//props.setProperty("pools.dbmode.defaultpoolname","R");		
		ServantProvider provider=new ServantProviderDB(props);
		RServices r=null;
		try {
			r=(RServices)provider.borrowServantProxyNoWait("R");
			r.consoleSubmit("56+77");
			System.out.println(r.getStatus());
			Thread.sleep(2000);
		} catch (Exception e) {
			provider.returnServantProxy(r);
		}
	}
		
	/**
	 * @param args
	 */
	/*
	public static void main(String[] args) throws Exception{
		System.out.println("hello");
		final RServices r = DirectJNI.getInstance().getRServices();
		r.consoleSubmit("x=c(2,6);y=serialize(x,NULL)");
		System.out.println(r.getStatus());
		r.consoleSubmit("y");
		System.out.println(r.getStatus());
		
		System.out.println("%"+r.getObject("y"));
		
		System.exit(0);
	}
	*/

}
