package views;

import graphics.rmi.RGui;
import graphics.rmi.RGuiReentrantLock;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;

import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

public class ChatConsoleView extends DynamicView {
	ConsolePanel _consolePanel;
	RGui _rgui;
	public ChatConsoleView(String title, Icon icon, int id, RGui rgui) {		
		super(title, icon, new JPanel(), id);
		_rgui=rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_consolePanel = new ConsolePanel(new SubmitInterface() {
			public String submit(final String expression) {

				new Thread(new Runnable() {
					public void run() {
						try {
							_rgui.getRLock().lock();
							_rgui.getR().chat(_rgui.getUID(),_rgui.getUserName(), expression);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							((RGuiReentrantLock) _rgui.getRLock()).unlockNoBroadcast();
						}
					}
				}).start();

				return "";
			}
		});
		((JPanel) getComponent()).add(_consolePanel);
	}

	public ConsolePanel getConsolePanel() {
		return _consolePanel;
	}
}

