/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2007 - 2008  Karim Chine

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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RNamedArgument;
import org.bioconductor.packages.rservices.RObjectName;
import org.bioconductor.packages.rservices.RS3;

import remoting.RServices;
import server.DirectJNI;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class BridgeBasics {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(BridgeBasics.class);


	public static void main(String args[]) throws Exception {
			
		final RServices rs = DirectJNI.getInstance().getRServices();		
				
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
		
		
		System.exit(0);
		
	}
}