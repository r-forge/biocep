package views;

import graphics.rmi.RGui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JPanel;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

public class UnsafeEvaluatorView extends DynamicView {
	ConsolePanel _consolePanel;
	RGui _rgui;

	public UnsafeEvaluatorView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);
		_rgui = rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_consolePanel = new ConsolePanel(new SubmitInterface() {
			public String submit(final String expression) {
				try {
					return _rgui.getR().unsafeGetObjectAsString(expression)+"\n";
				} catch (Exception e) {
					return PoolUtils.getStackTraceAsString(e);
				}
			}
		}, "R Expression", Color.red, true, null);
		((JPanel) getComponent()).add(_consolePanel);
	}

	public ConsolePanel getConsolePanel() {
		return _consolePanel;
	}
}
