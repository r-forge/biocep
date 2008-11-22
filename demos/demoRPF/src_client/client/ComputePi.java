package client;

import java.math.BigDecimal;
import org.kchine.rpf.ServantProviderFactory;
import compute.Compute;
import org.kchine.rpf.YesSecurityManager;

/**
 * @author Karim Chine kchine@m4x.org
 */
public class ComputePi {
	public static void main(String args[]) throws Exception {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}

		for (int i = 0; i < 5; ++i) {

			new Thread(new Runnable() {
				public void run() {
					Compute comp = null;
					try {
						comp = (Compute) ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();
						System.out.println(comp.getServantName());
						Pi task = new Pi((int) (Math.random() * 16));
						BigDecimal pi = comp.executeTask(task);
						System.out.println(Thread.currentThread().getName() + " --> " + pi);

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(comp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();

		}
	}

}
