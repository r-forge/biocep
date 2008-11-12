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
package bootstrap;

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
			boolean keepAlive = new Boolean(args[0]);
			URLClassLoader cl = null;

			System.out.println("Boot Args:" + Arrays.toString(args));
			Vector<URL> codeUrls=new Vector<URL>();
			codeUrls.add(new URL("http://" + args[1] + ":" + args[2] + "/classes/"));
			
			if (args.length > 3) {
				for (int i=3;i<args.length;++i) {
					codeUrls.add(new URL(args[i]));
				}
			}
			
			cl = new URLClassLoader( (URL[])codeUrls.toArray(new URL[0]), Boot.class.getClassLoader());

			if (!keepAlive) {
				cl.loadClass("server.ServerManager").getMethod("startPortInUseDogwatcher", new Class<?>[] { String.class, int.class, int.class, int.class })
						.invoke(null, args[1], Integer.decode(args[2]), 3, 3);
			}

			Class<?> mainClass = cl.loadClass("server.MainRServer");
			mainClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
			
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
