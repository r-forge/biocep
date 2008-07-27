package graphics.rmi.action;

import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SnapshotDeviceSvgAction extends AbstractAction {
	RGui _rgui;

	public SnapshotDeviceSvgAction(RGui rgui) {
		super("Create SVG Snapshot");
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

					JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);

					Vector<String> result = panel.getGdDevice().getSVG();
					final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
					PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
					for (int i = 0; i < result.size(); ++i)
						pw.println(result.elementAt(i));
					pw.close();

					final JSVGCanvas svgCanvas = new JSVGCanvas();
					svgCanvas.setEnableZoomInteractor(true);

					_rgui.createView(svgCanvas, "SVG Snapshot");

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								svgCanvas.setURI(new File(tempFile).toURL().toString());
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
