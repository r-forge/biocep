/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server;


import java.awt.Dimension;
import java.awt.Point;
import java.rmi.RemoteException;

import org.kchine.r.server.graphics.GDContainer;
import org.kchine.r.server.graphics.GraphicNotifier;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JavaGD;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LocalGraphicNotifier implements GraphicNotifier {

	public void fireSizeChangedEvent(final int devNr) throws RemoteException {
		System.out.println(DirectJNI.getInstance().getRServices().evaluate("try(.C(\"javaGDresize\",as.integer(" + devNr + ")),silent=TRUE)"));
	}

	public void registerContainer(GDContainer container) throws RemoteException {
		JavaGD.setGDContainer(container);
		Dimension dim = container.getSize();
		System.out.println(DirectJNI.getInstance().getRServices().evaluate(
				"JavaGD(name='JavaGD', width=" + dim.getWidth() + ", height=" + dim.getHeight() + ", ps=12)"));
	}

	public void executeDevOff(int devNr) throws RemoteException {
		System.out.println(DirectJNI.getInstance().getRServices().evaluate(
				"try({ .PrivateEnv$dev.set(" + (devNr + 1) + "); .PrivateEnv$dev.off()},silent=TRUE)"));
	}

	public void putLocation(Point p) throws RemoteException {
		GDInterface.putLocatorLocation(p);
	}

};
