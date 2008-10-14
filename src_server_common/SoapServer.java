public class SoapServer {
	public static void main(String args[]) throws Exception{
		System.setProperty("soapenabled","true");
		HttpServer.main(new String[0]);
	}
}
