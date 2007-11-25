/*
 * Copyright (C) 2007 EMBL-EBI
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class PropertiesGenerator {

	public static void main(String[] args) throws Exception {
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
