package graphics.rmi;

import java.util.Vector;
import remoting.RServices;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface RClustserInterface {
	public Vector<RServices> createRs(int n, String nodeName) throws Exception;

	public void releaseRs(Vector<RServices> rs, int n, String nodeName) throws Exception;
}
