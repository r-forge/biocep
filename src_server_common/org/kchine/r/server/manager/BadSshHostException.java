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
package org.kchine.r.server.manager;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class BadSshHostException extends Exception {

	public BadSshHostException() {
	}

	public BadSshHostException(String message) {
		super(message);
	}

	public BadSshHostException(Throwable cause) {
		super(cause);
	}

	public BadSshHostException(String message, Throwable cause) {
		super(message, cause);
	}

}
