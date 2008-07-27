package graphics.rmi.action;

import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class CoupleToCurrentDeviceAction extends AbstractAction implements LinkedToPanel {
	private JGDPanelPop _panel;
	private RGui _rgui;

	public CoupleToCurrentDeviceAction(RGui rgui) {
		super("Couple to Current Device");
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		if (_rgui.getCurrentJGPanelPop().isCoupledTo(_panel)) {
			_rgui.getCurrentJGPanelPop().removeCoupledTo(_panel);
		} else {
			_rgui.getCurrentJGPanelPop().addCoupledTo(_panel);
		}
	}

	public boolean isEnabled() {
		return _rgui.getR() != null && _rgui.getCurrentDevice() != _panel.getGdDevice();
	}

	public Object getValue(String key) {
		Object result = super.getValue(key);
		if (key.equals("Name")) {
			if (_rgui.getCurrentJGPanelPop().isCoupledTo(_panel)) {
				result = "Uncouple from Current Device";
			} else {
				result = "Couple with Current Device";
			}
		}
		return result;
	}

	public void setPanel(JGDPanelPop panel) {
		_panel = panel;

	}
}