package graphics.rmi.action;

import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.RGui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class SnapshotDeviceAction extends AbstractAction {
	RGui _rgui;
	
	public SnapshotDeviceAction(RGui rgui) {
		super("Create Snapshot");
		_rgui=rgui;
	}
	
	
	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {

								JBufferedImagePanel bufferedImagePanel = null;
								
									bufferedImagePanel = (JBufferedImagePanel) GDApplet
										.getComponentParent((Component) e.getSource(),
												JBufferedImagePanel.class);
								
								
								final JBufferedImagePanel panelclone = new JBufferedImagePanel(bufferedImagePanel.getImage());
								
								
								final AbstractAction[] actions = new AbstractAction[] { new SnapshotDeviceAction(_rgui) ,
										new SaveDeviceAsPngAction(_rgui), new SaveDeviceAsJpgAction(_rgui) };
								
								
								
								panelclone.addMouseListener(new MouseAdapter() {
									public void mousePressed(MouseEvent e) {
										checkPopup(e);
									}

									public void mouseClicked(MouseEvent e) {
										checkPopup(e);
									}

									public void mouseReleased(MouseEvent e) {
										checkPopup(e);
									}

									private void checkPopup(MouseEvent e) {
										if (e.isPopupTrigger() && actions != null) {
											JPopupMenu popupMenu = new JPopupMenu();
											for (int i = 0; i < actions.length; ++i) {
												popupMenu.add(actions[i]);
											}
											popupMenu.show(panelclone, e.getX(), e.getY());
										}
									}
								});

								_rgui.createView(new JScrollPane(panelclone), "Snapshot");


							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
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
