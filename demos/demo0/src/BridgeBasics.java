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
import java.io.RandomAccessFile;
import org.apache.commons.logging.Log;
import org.kchine.r.RList;
import org.kchine.r.RObject;
import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.scilab.server.ScilabServices;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class BridgeBasics {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(BridgeBasics.class);


	public static void main(String args[]) throws Exception {
		RServices rs = DirectJNI.getInstance().getRServices();
		RList ro1 = 
			  
			(RList)rs.getObject("structure(list(1,2), caption = 'foo')");
			RObject ro2 = 
			   rs.getReference("structure(list(1,2), caption = 'foo')");
			System.out.println(rs.callAndConvert("str", ro1));
			System.out.println(rs.callAndConvert("str", ro2));
			
		
		
		/*
		
		RServices rs =
			ServerManager.createR("toto"); 
		
			RObject ro1 = 
			   rs.getObject("structure(list(1,2), caption = \"foo\")");
			RObject ro2 = 
			   rs.getReference("structure(list(1,2), caption = \"foo\")");
			System.out.println(rs.callAndConvert("str", ro1));
			System.out.println(rs.callAndConvert("str", ro2));
		
		
		rs.die();
		
		if (true) return;
		*/
		
		rs = DirectJNI.getInstance().getRServices();
		GDDevice device=rs.newDevice(400, 400);		
		rs.consoleSubmit("plot(pressure)");
		System.out.println(rs.getStatus());
		byte[] buffer=device.getPng();
		RandomAccessFile raf=new RandomAccessFile("c:/te.png","rw");
		raf.setLength(0);
		raf.write(buffer);
		raf.close();
		
		
		//rs.evaluate("x=2;y=8",2);
		
		/*
		RS3 s3=(RS3)rs.getReference("packageDescription('stats')");
		System.out.println("s="+Arrays.toString(s3.getClassAttribute()));
		s3.setClassAttribute(new String[] {s3.getClassAttribute()[0], "aaa"});
		rs.assignReference("f",s3);
		//rs.call("print",new RObjectName("f"));
		//System.out.println("log=" + rs.getStatus());
		
		rs.consoleSubmit("print(class(f))");
		System.out.println("log=" + rs.getStatus());
		
		RChar s = (RChar) rs.call("paste", new RChar("str1"), new RChar("str2"), new RNamedArgument("sep", new RChar(
				"--")));
		System.out.println("s=" + s);
		*/
		
		System.exit(0);
		
	}
}