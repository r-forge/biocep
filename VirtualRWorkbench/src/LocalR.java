import java.io.PrintStream;

import remoting.RServices;
import server.DirectJNI;


public class LocalR {
	public static void main(String args[]) throws Exception {

		System.setErr(new PrintStream(System.out));
		DirectJNI.init();
		RServices r = DirectJNI.getInstance().getRServices();
	}
}
