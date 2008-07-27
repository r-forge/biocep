package graphics.rmi.action;

import graphics.pop.GDDevice;
import graphics.rmi.RGui;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class ShowDeviceInfoAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;

	public ShowDeviceInfoAction(RGui rgui, GDDevice device) {
		super("Show Device Info");
		_device = device;
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			System.out.println("device number :" + _device.getDeviceNumber());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
