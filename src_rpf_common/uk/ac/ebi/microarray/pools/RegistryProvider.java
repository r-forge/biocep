package uk.ac.ebi.microarray.pools;

import java.rmi.registry.Registry;

public interface RegistryProvider {
	Registry getRegistry() throws Exception;
}
