package org.kchine.r.workbench.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.RandomAccessFile;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JBufferedImagePanel;
import org.kchine.r.workbench.graphics.JGDPanelPop;

public class SaveDeviceAsPictexAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsPictexAction(RGui rgui) {
		super("Save as PICTEX");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		final JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save Graphics as PICTEX");
		int returnVal = chooser.showSaveDialog(_rgui.getRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			new Thread(new Runnable() {
				public void run() {
					try {
						_rgui.getRLock().lock();
						JGDPanelPop panel = (JGDPanelPop) WorkbenchApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
						byte[] result = panel.getGdDevice().getPictex();
						RandomAccessFile raf = new RandomAccessFile(org.kchine.rpf.PoolUtils.fixExtension(chooser.getSelectedFile(),"tex"), "rw");
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