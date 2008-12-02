import static org.kchine.rpf.PoolUtils.getHostIp;
public class HttpServerLight {

	public static void main(String[] args) throws Exception {
		int port=8080;
		try {
			if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) {
				port=Integer.decode(System.getProperty("port"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("R-HTTP URL: http://"+getHostIp()+":"+port+"/rvirtual/cmd");
		System.out.println("--> From the Virtual R Workbench, in Http mode, connect via the following URL:"+ "http://"+getHostIp()+":"+port+"/rvirtual/cmd");	
	}
}
