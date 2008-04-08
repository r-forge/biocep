package groovy.server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

public class GroovyInterpreterSingleton {

	private static GroovyInterpreter _groovy = null;
	private static Integer lock = new Integer(0);

	public static GroovyInterpreter getInstance() {
		if (_groovy != null)
			return _groovy;
		synchronized (lock) {
			if (_groovy == null) {
				
				try {
					final Class<?> GroovyShellClass=GroovyInterpreterSingleton.class.getClassLoader().loadClass("groovy.lang.GroovyShell");
					final Object groovyShell=GroovyShellClass.newInstance();
					_groovy = new GroovyInterpreter() {
							public String exec(String expression) throws Exception {
								ByteArrayOutputStream baos=new ByteArrayOutputStream();
								PrintStream saveOut=System.out;
								PrintStream saveErr=System.err;
								System.setOut(new PrintStream(baos));
								System.setErr(new PrintStream(baos));
								try {
									GroovyShellClass.getMethod("evaluate", String.class).invoke(groovyShell, expression);
								} catch (InvocationTargetException e){
									return e.getCause().getMessage()+"\n";
								} finally {
									System.setOut(saveOut);
									System.setErr(saveErr);			
								}
								return new String(baos.toByteArray(),"UTF-8");
							}
					};
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			return _groovy;
		}
	}
	
}
