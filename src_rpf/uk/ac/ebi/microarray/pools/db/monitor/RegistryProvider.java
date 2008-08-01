package uk.ac.ebi.microarray.pools.db.monitor;

import java.rmi.registry.Registry;

public interface RegistryProvider {
	Registry getRegistry() throws Exception;
}
