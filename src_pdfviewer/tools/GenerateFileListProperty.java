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

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Vector;

public class GenerateFileListProperty {

	public static void main(String[] args) throws Exception{		
		String dir=System.getProperty("dir");
		final String extension=System.getProperty("extension");
		String file=System.getProperty("file");
		String propertyname=System.getProperty("propertyname");
		String result="";
		
		String[] list=new File(dir).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(extension);
			}
		});		
		
		for(int i=0; i<list.length; ++i) {
			int idx=list[i].lastIndexOf(extension);
			result=result+(i==0?"":":")+list[i].substring(0,idx);
		}		
		Vector<String> lignes=new Vector<String>();
		lignes.add(propertyname+"="+result);		
		PrintWriter pw=new PrintWriter(new File(file));
		for (int i=0; i<lignes.size(); ++i) {
			pw.println(lignes.elementAt(i));
		}
		pw.close();		
	}	

}
