package org.kchine.r.server.scripting;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import server.ServerManager;

public class GroovyInterpreterSingleton {

	private static GroovyInterpreter _groovy = null;
	private static Integer lock = new Integer(0);

	public static GroovyInterpreter getInstance() {
		if (_groovy != null)
			return _groovy;
		synchronized (lock) {
			if (_groovy == null) {
				
				try {
					

					File[] extraJarFiles=new File(ServerManager.INSTALL_DIR).listFiles(new FilenameFilter(){
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar");
						}
					});
					
					Arrays.sort(extraJarFiles);
					URL[] urls=new URL[extraJarFiles.length];
					for (int i=0; i<extraJarFiles.length;++i) {
						urls[i]=extraJarFiles[i].toURI().toURL();
					}
					
					final Class<?> GroovyShellClass=new URLClassLoader(urls, GroovyInterpreterSingleton.class.getClassLoader()).loadClass("groovy.lang.GroovyShell");
									
					final Object groovyShell=GroovyShellClass.newInstance();
					_groovy = new GroovyInterpreter() {
						    private String _status;
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
								_status= new String(baos.toByteArray(),"UTF-8");
								return _status;
							}
							
							public String execFromFile(File f) throws Exception {
								ByteArrayOutputStream baos=new ByteArrayOutputStream();
								PrintStream saveOut=System.out;
								PrintStream saveErr=System.err;
								System.setOut(new PrintStream(baos));
								System.setErr(new PrintStream(baos));
								try {
									System.out.println("going to evaluate from file :"+f);
									GroovyShellClass.getMethod("evaluate", File.class).invoke(groovyShell, f);
								} catch (InvocationTargetException e){
									return e.getCause().getMessage()+"\n";
								} finally {
									System.setOut(saveOut);
									System.setErr(saveErr);			
								}
								_status=new String(baos.toByteArray(),"UTF-8");
								return _status;
							}
							
							public String getStatus() throws Exception {
								return _status;
							}
							
							public String execFromBuffer(String buffer) throws Exception {
								
								File tempFile = null;
								tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + "biocep_temp_"+System.currentTimeMillis()).getCanonicalFile();
								
								if (tempFile.exists())tempFile.delete();								
							
								BufferedReader breader = new BufferedReader(new StringReader(buffer));
								PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
								String line;
								boolean rscriptingOn=false;
								int rLinesCounter=0;
								while ((line=breader.readLine())!=null) {
									if (line.trim().equals("<R>")) {
										pwriter.print("String log_R_sourceFromBuffer=");
										if (server.R.getInstance()!=null) pwriter.print("server."); else pwriter.print("client."); 
										pwriter.print("R.getInstance().sourceFromBuffer(new StringBuffer(\"");
										rscriptingOn=true;rLinesCounter=0;
									}
									else if (line.trim().equals("</R>")) {
										pwriter.println("\"));System.out.println(\"\\n\"+log_R_sourceFromBuffer+ \"\\n\" );");
										
										for (int i=0; i<rLinesCounter-1; ++i) pwriter.println();
										rscriptingOn=false;rLinesCounter=0;
									}
									else {
										if (rscriptingOn) {
											pwriter.print(line+"\\n");rLinesCounter++;
										} else {
											pwriter.println(line);
										}
									}
								}
								pwriter.close();
									
								ByteArrayOutputStream baos=new ByteArrayOutputStream();
								PrintStream saveOut=System.out;
								PrintStream saveErr=System.err;
								System.setOut(new PrintStream(baos));
								System.setErr(new PrintStream(baos));
								try {
									GroovyShellClass.getMethod("evaluate", File.class).invoke(groovyShell, tempFile);
								} catch (InvocationTargetException e){
									return e.getCause().getMessage()+"\n";
								} finally {
									System.setOut(saveOut);
									System.setErr(saveErr);
									//if (tempFile!=null) tempFile.delete();
								}
								_status=new String(baos.toByteArray(),"UTF-8");
								return _status;
							}
					};
					
					
				} catch (Throwable e) {
					//e.printStackTrace();
				}
			}
			return _groovy;
		}
	}
	
}