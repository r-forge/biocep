package graphics.rmi.action;

import graphics.pop.GDDevice;
import graphics.rmi.GUtils;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class FitDeviceAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;
	public FitDeviceAction(RGui rgui , GDDevice device) {
		super("Fit Device to Panel");
		_device=device;
		_rgui=rgui;
	}
	
	public void actionPerformed(ActionEvent e) {
		try  {						
			
			JGDPanelPop panel = (JGDPanelPop) GUtils
			.getComponentParent((Component) e.getSource(),
					JGDPanelPop.class);
			
			panel.fit();
		} catch (Exception ex) {
			ex.printStackTrace();	
		}
		
	}
	

	@Override
	public boolean isEnabled() {
		return _rgui.getR()!=null;
	}

}
