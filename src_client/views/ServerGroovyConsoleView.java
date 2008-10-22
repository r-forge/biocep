package views;

import graphics.rmi.RGui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JPanel;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

public 	class ServerGroovyConsoleView extends DynamicView {
	ConsolePanel _consolePanel;
	RGui _rgui;
	public ServerGroovyConsoleView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);
		_rgui=rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_consolePanel = new ConsolePanel(new SubmitInterface() {
			public String submit(final String expression) {
				if (_rgui.getRLock().isLocked()) {
					return "R is busy, please retry\n";
				}
				try {
					_rgui.getRLock().lock();
					final String log = _rgui.getR().groovyExec(expression);
					return log+"\n";
				} catch (Exception e) {
					return PoolUtils.getStackTraceAsString(e)+"\n";
				} finally {
					_rgui.getRLock().unlock();
				}
			}
		}, "Groovy Expression", Color.magenta, true, null);
		((JPanel) getComponent()).add(_consolePanel);
	}

	public ConsolePanel getConsolePanel() {
		return _consolePanel;
	}
}
