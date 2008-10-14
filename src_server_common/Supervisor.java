public class Supervisor {
	public static void main(String[] args) {		
		System.setProperty("naming.mode","db");
		uk.ac.ebi.microarray.pools.db.monitor.Supervisor.main(new String[0]);
	}
}