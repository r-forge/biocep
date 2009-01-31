import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;


public class BiocepNoServer {

	/**
	 * @param args
	 */
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

}
