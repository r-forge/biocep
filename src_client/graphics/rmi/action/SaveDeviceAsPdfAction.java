package graphics.rmi.action;

import graphics.rmi.RGui;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import java.awt.Component;
import java.io.RandomAccessFile;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SaveDeviceAsPdfAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsPdfAction(RGui rgui) {
		super("Save as PDF");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		final JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			new Thread(new Runnable() {
				public void run() {
					try {
						_rgui.getRLock().lock();
						JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
						byte[] result = panel.getGdDevice().getPdf();
						RandomAccessFile raf = new RandomAccessFile(chooser.getSelectedFile(), "rw");
						raf.setLength(0);
						raf.write(result);
						raf.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						_rgui.getRLock().unlock();
					}
				}
			}).start();
		}
	}

	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
