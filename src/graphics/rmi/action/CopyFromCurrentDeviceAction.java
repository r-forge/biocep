package graphics.rmi.action;

import graphics.pop.GDDevice;
import graphics.rmi.RGui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class CopyFromCurrentDeviceAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;
	public CopyFromCurrentDeviceAction(RGui rgui , GDDevice device) {
		super("Copy From Current Device");
		_device=device;
		_rgui=rgui;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try  {						
			
			_rgui.getRLock().lock();
			int desinationDeviceNumber=_device.getDeviceNumber();
			int sourceDeviceNumber=_rgui.getCurrentDevice().getDeviceNumber();
			
			System.out.println(_rgui.getR().consoleSubmit(".PrivateEnv$dev.copy(which="+desinationDeviceNumber+");" +".PrivateEnv$dev.set("+sourceDeviceNumber+");"));
			
		} catch (Exception ex) {
			ex.printStackTrace();	
		} finally {
			_rgui.getRLock().unlock();
		}
		
	}
	

	@Override
	public boolean isEnabled() {
		return _rgui.getR()!=null && _rgui.getCurrentDevice()!=_device; 
	}

}