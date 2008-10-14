import uk.ac.ebi.microarray.pools.PoolUtils;


public class RmiRegistry {

	public static void main(String[] args) throws Exception{
		String port=new Integer(PoolUtils.DEFAULT_REGISTRY_PORT).toString();
		if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) {
			port=System.getProperty("port");
		}		
		System.setProperty("port", port);
		uk.ac.ebi.microarray.pools.MainRegistry.main(new String[0]);
	}

}
