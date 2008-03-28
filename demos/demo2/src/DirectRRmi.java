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
import java.rmi.Naming;
import org.bioconductor.packages.biobase.ExpressionSet;
import org.bioconductor.packages.rGlobalEnv.rGlobalEnvFunction;
import org.bioconductor.packages.rservices.RNamedArgument;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.vsn.vsnFunction;
import org.bioconductor.packages.vsn.Vsn;
import remoting.RServices;
import util.Utils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class DirectRRmi {
	public static void main(String[] args) throws Throwable {
		final RServices r = ((RServices) Naming.lookup("RSERVANT_1"));
		System.out.println("Available Packages : " + Utils.flatArray(r.listPackages()));
		RNumeric squareOf4 = ((rGlobalEnvFunction) r.getPackage("rGlobalEnvFunction"))
				.squareAsReference(new RNumeric(4));
		System.out.println("square of 4 : " + squareOf4.getValue()[0]);
		r.evaluate("data(kidney)");
		ExpressionSet kidney = (ExpressionSet) r.getReference("kidney");
		Vsn fit = ((vsnFunction) r.getPackage("vsnFunction")).vsn2(kidney);
		ExpressionSet normalizedKidney = (ExpressionSet) r.call("predict", fit, new RNamedArgument("newdata", kidney));
		System.out.println(" Normalized Kidney : " + normalizedKidney);
	}

}