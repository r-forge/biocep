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
package org.kchine.r.server;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RAction implements Serializable {

	private String _actionName;
	private HashMap<String, Object> _actionAttributes = null;
	private HashMap<String, Object> _clientProperties = null;

	public RAction() {

	}

	public RAction(String actionName) {
		_actionName = actionName;
	}

	public RAction(String actionName, HashMap<String, Object> attributes) {
		_actionName = actionName;
		_actionAttributes = attributes;
	}
	
	public RAction(String actionName, HashMap<String, Object> attributes, HashMap<String, Object> clientProperties) {
		_actionName = actionName;
		_actionAttributes = attributes;
		_clientProperties = clientProperties;
	}
	
	public void setActionName(String name) {
		_actionName = name;
	}

	public String getActionName() {
		return _actionName;
	}

	public void setAttributes(HashMap<String, Object> attributes) {
		_actionAttributes = attributes;
	}

	public HashMap<String, Object> getAttributes() {
		return _actionAttributes;
	}
	
	public String toString() {
		return "[action name="+_actionName+" attributes="+_actionAttributes+" client properties :"+_clientProperties+"]";
	}

	public HashMap<String, Object> getClientProperties() {
		return _clientProperties;
	}

	public void setClientProperties(HashMap<String, Object> properties) {
		_clientProperties = properties;
	}

}