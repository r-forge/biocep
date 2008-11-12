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
package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;

public class FillInFileTemplate {

	public static String replaceAll(String input, String replaceWhat, String replaceWith) throws Exception {
		int p;
		int bindex = 0;
		while ((p = input.indexOf(replaceWhat, bindex)) != -1) {
			input = input.substring(0, p) + replaceWith + input.substring(p + replaceWhat.length());
			bindex = p + replaceWith.length();
		}
		return input;
	}
		
	public static void main(String[] args) throws Exception{		
		String template=System.getProperty("template");
		String dest=System.getProperty("dest");
		String pattern=System.getProperty("pattern");
		String replacement=System.getProperty("replacement");		
		BufferedReader br=new BufferedReader(new FileReader(template));
		String l; Vector<String> lignes=new Vector<String>();
		while ((l=br.readLine())!=null) lignes.add(l);		
		PrintWriter pw=new PrintWriter(new File(dest));
		for (int i=0; i<lignes.size(); ++i) {
			pw.println(replaceAll(lignes.elementAt(i), pattern, replacement));
		}
		pw.close();		
	}	

}
