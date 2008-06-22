package graphics.rmi;

import graphics.pop.GDDevice;
import groovy.GroovyInterpreter;

import java.awt.Component;
import java.io.File;

import net.infonode.docking.View;
import remoting.RKit;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface RGui extends RKit {
	public ConsoleLogger getConsoleLogger();

	public View createView(Component panel, String title);

	public void setCurrentDevice(GDDevice device);

	public Component getRootComponent();

	public GDDevice getCurrentDevice();

	public JGDPanelPop getCurrentJGPanelPop();
	
	public void upload(File localFile, String fileName) throws Exception;
	
	public GroovyInterpreter getGroovyInterpreter() ;
	
	public String getUserName(); 
	
	public String getUID();
}
