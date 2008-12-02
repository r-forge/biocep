/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package client;

import java.rmi.registry.Registry;

import org.kchine.r.server.RServices;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.gui.InDialog;

import graphics.rmi.GDApplet;

public class R {
	public static  RServices getInstance() {
		if (GDApplet._instance==null) return null;
		else return GDApplet._instance.getR();
	}
	public static  Registry getRegistry() throws Exception {
		return ServerDefaults.getRmiRegistry();
	}
	public static String getUserInput(String label) {
		InDialog dialog=new InDialog(null,label,new String[]{""});
		dialog.setVisible(true);
		return dialog.getExpr();
	}
	
}
