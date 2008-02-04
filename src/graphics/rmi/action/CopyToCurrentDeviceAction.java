package graphics.rmi.action;

import graphics.pop.GDDevice;
import graphics.rmi.RGui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class CopyToCurrentDeviceAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;
	public CopyToCurrentDeviceAction(RGui rgui , GDDevice device) {
		super("Copy To Current Device");
		_device=device;
		_rgui=rgui;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try  {						
			
			_rgui.getRLock().lock();
			int desinationDeviceNumber=_device.getDeviceNumber();
			int sourceDeviceNumber=_rgui.getCurrentDevice().getDeviceNumber();
			System.out.println(_rgui.getR().consoleSubmit(".PrivateEnv$dev.set("+desinationDeviceNumber+");"+".PrivateEnv$dev.copy(which="+sourceDeviceNumber+");" ));
			
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