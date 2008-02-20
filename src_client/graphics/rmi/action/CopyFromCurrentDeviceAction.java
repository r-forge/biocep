package graphics.rmi.action;

import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class CopyFromCurrentDeviceAction extends AbstractAction implements LinkedToPanel{
	private RGui _rgui;
	private JGDPanelPop _panel;
	
	public CopyFromCurrentDeviceAction(RGui rgui) {
		super("Copy From Current Device");
		_rgui=rgui;
	}
	
	public void actionPerformed(ActionEvent e) {
		try  {						
			_rgui.getCurrentJGPanelPop().removeCoupledTo(_panel);			
			_rgui.getRLock().lock();
			int desinationDeviceNumber=_panel.getGdDevice().getDeviceNumber();
			int sourceDeviceNumber=_rgui.getCurrentDevice().getDeviceNumber();
			
			System.out.println(_rgui.getR().consoleSubmit(".PrivateEnv$dev.copy(which="+desinationDeviceNumber+");" +".PrivateEnv$dev.set("+sourceDeviceNumber+");"));
			
		} catch (Exception ex) {
			ex.printStackTrace();	
		} finally {
			_rgui.getRLock().unlock();
		}
		
	}
	
	public boolean isEnabled() {
		return _rgui.getR()!=null && _rgui.getCurrentDevice()!=_panel.getGdDevice(); 
	}
	
	public void setPanel(JGDPanelPop panel) {
		_panel=panel;
		
	}
}