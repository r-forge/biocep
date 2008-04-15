package groovy;

import java.io.File;

public interface GroovyInterpreter {
	public String exec(String expression) throws Exception;
	public String execFromFile(File f) throws Exception;
	public String execFromBuffer(StringBuffer buffer) throws Exception;
	public String getStatus() throws Exception ;
}
