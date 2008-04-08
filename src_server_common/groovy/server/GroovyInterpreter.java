package groovy.server;

import java.io.File;

public interface GroovyInterpreter {
	public String exec(String expression) throws Exception;
	public String execFromFile(File f) throws Exception;
}
