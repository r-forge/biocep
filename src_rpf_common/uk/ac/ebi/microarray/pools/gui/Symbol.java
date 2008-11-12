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
package uk.ac.ebi.microarray.pools.gui;

import java.io.Serializable;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class Symbol {
	private String name;
	private Serializable value;
	private String servantName;

	public Symbol(String name, Serializable value, String servantName) {
		this.name = name;
		this.value = value;
		this.servantName = servantName;
	}

	public String getName() {
		return name;
	}

	public String getServantName() {
		return servantName;
	}

	public Serializable getValue() {
		return value;
	}

	public String toString() {
		return name + " from servant <" + servantName + ">";
	}
}