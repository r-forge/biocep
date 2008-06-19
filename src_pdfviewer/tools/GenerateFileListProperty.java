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
