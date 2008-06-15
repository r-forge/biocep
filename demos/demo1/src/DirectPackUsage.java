/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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

import java.io.PrintStream;
import java.util.Arrays;
import org.bioconductor.packages.biobase.ExpressionSet;
import org.bioconductor.packages.rGlobalEnv.Point;
import org.bioconductor.packages.rGlobalEnv.rGlobalEnvFunction;
import org.bioconductor.packages.rservices.RNamedArgument;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.vsn.Vsn;
import org.bioconductor.packages.vsn.vsnFunction;
import remoting.RServices;
import server.DirectJNI;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class DirectPackUsage {

	public static void main(String args[]) throws Exception {

		RServices rs = DirectJNI.getInstance().getRServices();
		rs.sourceFromBuffer(new StringBuffer("print('aaa'"));
		System.out.println("status:"+rs.getStatus());		
		System.exit(0);
		
		
		
		
		
		
		
		
		
		System.setErr(new PrintStream(System.out));
		DirectJNI.init();
		RServices r = DirectJNI.getInstance().getRServices();
		
		r.evaluate("data(kidney)");
		
		//r.callAndGetObjectName(methodName, args)
		
		
		System.exit(0);
		
		
		System.out.println("Available Packages : " + Arrays.toString(r.listPackages()));
		rGlobalEnvFunction globalPack = ((rGlobalEnvFunction) r.getPackage("rGlobalEnvFunction"));
		vsnFunction vsnPack = (vsnFunction) r.getPackage("vsnFunction");

		try {

			Point o = new Point(new RNumeric(0), new RNumeric(0));
			Point p = new Point(new RNumeric(2), new RNumeric(3));
			RNumeric d = globalPack.distance(o, p);
			System.out.println(" distance : " + d);

			r.evaluate("data(kidney)");
			ExpressionSet kidney = (ExpressionSet) r.getReference("kidney");
			Vsn fit = vsnPack.vsn2(kidney);
			ExpressionSet normalizedKidney = (ExpressionSet) r.call("predict", fit, new RNamedArgument("newdata",
					kidney));
			System.out.println(" Normalized Kidney : " + normalizedKidney);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);
	}


}
