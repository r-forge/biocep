package graphics.rmi.action;
import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.RGui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class SaveDeviceAsPngAction extends AbstractAction{
	
	RGui _rgui;		
	
	public SaveDeviceAsPngAction(RGui rgui) {
		super("Save as PNG");
		_rgui=rgui;
	}
	

	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {

					final JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						JBufferedImagePanel bufferedImagePanel = (JBufferedImagePanel) GDApplet
								.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
						
						ImageIO.write(bufferedImagePanel.getImage(), "png", chooser.getSelectedFile());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();
		
	}
	
	@Override
	public boolean isEnabled() {
		return _rgui.getR()!=null;
	}

}
