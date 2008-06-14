package graphics.rmi.action;

import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.PDFPanel;
import graphics.rmi.RGui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class SnapshotDevicePdfAction extends AbstractAction {
	RGui _rgui;

	public SnapshotDevicePdfAction(RGui rgui) {
		super("Create PDF Snapshot");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		new Thread(new Runnable() {
			public void run() {

				try {
					_rgui.getRLock().lock();

					final JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
					final PDFPanel pdfPanel=new PDFPanel();					
					_rgui.createView(pdfPanel, "PDF Snapshot");
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								pdfPanel.setPDFContent(panel.getGdDevice().getPdf());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					_rgui.getRLock().unlock();
				}

			}
		}).start();

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}
}
