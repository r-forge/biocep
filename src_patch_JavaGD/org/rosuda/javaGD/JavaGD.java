package org.rosuda.javaGD;

import org.kchine.r.server.graphics.GDContainer;

public class JavaGD extends GDInterface {
	private static GDContainer _gdContainer;

	public static void setGDContainer(GDContainer container) {
		_gdContainer = container;
	}

	public JavaGD() {
		super();
	}

	public void gdOpen(double w, double h) {
		//System.out.println("gdOpen called");        
		c = _gdContainer;
	}
}
