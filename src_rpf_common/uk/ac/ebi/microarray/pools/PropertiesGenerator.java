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
package uk.ac.ebi.microarray.pools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class PropertiesGenerator {

	public static void main(String[] args) throws Exception {
		//System.out.println("args:"+Arrays.toString(args));
		String fileName = args[0];
		String[][] key_value = new String[args.length - 1][2];
		for (int i = 1; i < args.length; ++i) {
			int eqIdx = args[i].indexOf('=');
			key_value[i - 1][0] = args[i].substring(0, eqIdx);
			key_value[i - 1][1] = args[i].substring(eqIdx + 1);
		}
		File f = new File(fileName);
		f.mkdirs();
		if (f.exists())
			f.delete();
		Properties props = new Properties();
		for (int i = 0; i < key_value.length; ++i) {
			props.put(key_value[i][0], key_value[i][1]);
		}
		props.storeToXML(new FileOutputStream(f), null);
	}
}
