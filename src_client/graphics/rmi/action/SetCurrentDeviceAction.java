package graphics.rmi.action;

import graphics.pop.GDDevice;
import graphics.rmi.RGui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class SetCurrentDeviceAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;

	public SetCurrentDeviceAction(RGui rgui, GDDevice device) {
		super("Set As Current Device");
		_device = device;
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			_device.setAsCurrentDevice();
			_rgui.setCurrentDevice(_device);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
