package http;

import uk.ac.ebi.microarray.pools.SSHTunnelingProxy;
import uk.ac.ebi.microarray.pools.db.DBLayerInterface;
import uk.ac.ebi.microarray.pools.db.DBLayerProvider;

public class SSHDBLayerProvider implements DBLayerProvider {
	public DBLayerInterface getDBLayer() throws Exception {
		DBLayerInterface dbLayer = (DBLayerInterface) SSHTunnelingProxy.getDynamicProxy(System.getProperty("submit.ssh.host"), Integer.decode(System
				.getProperty("submit.ssh.port")), System.getProperty("submit.ssh.user"), System.getProperty("submit.ssh.password"), System
				.getProperty("submit.ssh.biocep.home"), "java -Dpools.provider.factory=uk.ac.ebi.microarray.pools.db.ServantsProviderFactoryDB -Dpools.dbmode.defaultpoolname=R -cp %{install.dir}/biocep-core.jar uk.ac.ebi.microarray.pools.SSHTunnelingWorker %{file}", "db", new Class<?>[] { DBLayerInterface.class });
		return dbLayer;
	}
}
