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
package org.kchine.r.server.manager.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class Boot {
	public static void main(final String[] args) throws Exception {
		try {
			URLClassLoader cl = null;
			System.out.println("Boot Args:" + Arrays.toString(args));
			Vector<URL> codeUrls=new Vector<URL>();
			for (int i=0;i<args.length;++i) {
				codeUrls.add(new URL(args[i]));
			}
			cl = new URLClassLoader( (URL[])codeUrls.toArray(new URL[0]), Boot.class.getClassLoader());
			Class<?> mainClass = cl.loadClass("org.kchine.r.server.MainRServer");
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
