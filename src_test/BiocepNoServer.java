import java.util.Properties;
import org.kchine.r.server.RServices;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.db.ServantProviderDB;

public class BiocepNoServer {

	public static void main(String[] args) throws Exception{
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
