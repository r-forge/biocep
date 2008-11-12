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
package graphics.rmi;

import java.awt.HeadlessException;
import java.util.HashMap;

import javax.swing.JApplet;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDAppletBase extends JApplet {
	private HashMap<String, String> _customParams = null;

	public GDAppletBase(HashMap<String, String> customParams) {
		_customParams = customParams;
	}

	@Override
	public String getParameter(String name) {
		if (_customParams != null) {
			String result = _customParams.get(name);
			if (result != null)
				return result;
		}
		try {
			return super.getParameter(name);
		} catch (Exception e) {
			return null;
		}
	}

	public GDAppletBase() throws HeadlessException {
		super();
	}

}
