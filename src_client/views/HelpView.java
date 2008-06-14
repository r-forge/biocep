package views;

import graphics.rmi.GDHelpBrowser;

import javax.swing.Icon;

public class HelpView extends DynamicView {
	GDHelpBrowser _browser;

	public HelpView(String title, Icon icon, GDHelpBrowser browser, int id) {
		super(title, icon, browser, id);
		_browser = browser;
	}

	public GDHelpBrowser getBrowser() {
		return _browser;
	}
}
