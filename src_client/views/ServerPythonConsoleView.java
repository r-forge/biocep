package views;

import graphics.rmi.RGui;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

public class ServerPythonConsoleView extends DynamicView {
	ConsolePanel _consolePanel;
	RGui _rgui;

	public ServerPythonConsoleView(String title, Icon icon, int id, RGui rgui) {
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
					final String log = _rgui.getR().pythonExec(expression);
					return log;
				} catch (Exception e) {
					return PoolUtils.getStackTraceAsString(e);
				} finally {
					_rgui.getRLock().unlock();
				}
			}
		},"Python Expression", true, null);
		((JPanel) getComponent()).add(_consolePanel);
	}

	public ConsolePanel getConsolePanel() {
		return _consolePanel;
	}
}

