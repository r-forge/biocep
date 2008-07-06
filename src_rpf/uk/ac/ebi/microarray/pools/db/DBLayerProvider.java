package uk.ac.ebi.microarray.pools.db;

public interface DBLayerProvider {
	DBLayerInterface getDBLayer() throws Exception ;
}
