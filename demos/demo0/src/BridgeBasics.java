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
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.kchine.r.RChar;
import org.kchine.r.RNamedArgument;
import org.kchine.r.RObjectName;
import org.kchine.r.RS3;
import org.kchine.r.server.RConsoleAction;
import org.kchine.r.server.RConsoleActionListener;
import org.kchine.r.server.RServices;

import server.DirectJNI;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class BridgeBasics {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(BridgeBasics.class);


	public static void main(String args[]) throws Exception {
			
		final RServices rs = DirectJNI.getInstance().getRServices();		
		rs.addRConsoleActionListener(new RConsoleActionListener(){
			public void rConsoleActionPerformed(RConsoleAction consoleAction) throws RemoteException {
				System.out.println(consoleAction);				
			}
		});		
		
		rs.addProbeOnVariables(new String[]{"x","y"});
		rs.consoleSubmit("y=1");
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