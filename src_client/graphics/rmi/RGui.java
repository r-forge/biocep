package graphics.rmi;

import graphics.pop.GDDevice;
import java.awt.Component;
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
}
