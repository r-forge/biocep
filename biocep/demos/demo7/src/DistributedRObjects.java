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
import java.rmi.Naming;

import org.bioconductor.packages.rGlobalEnv.A;
import org.bioconductor.packages.rGlobalEnv.B;
import org.bioconductor.packages.rGlobalEnv.E;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RNumericRef;

import remoting.RServices;
import util.Utils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class DistributedRObjects {
	public static void main(String[] args) throws Exception {

		RServices r1 = ((RServices) Naming.lookup("RSERVANT_1"));
		RServices r2 = ((RServices) Naming.lookup("RSERVANT_2"));
		RServices r3 = ((RServices) Naming.lookup("RSERVANT_3"));

		E eref1 = (E) r1.evalAndGetObjectAsReference("new('E')");

		RNumericRef num = (RNumericRef) r3.evalAndGetObjectAsReference("c(100)");
		eref1.getC().setS(num);

		B nb = (B) r2.evalAndGetObjectAsReference("new('B')");
		nb.setV(new RNumeric(996));
		eref1.setB(nb);

		A na = (A) r3.evalAndGetObjectAsReference("new('A')");
		na.setX(new RChar("LLLLLLL"));
		nb.setQ(na);

		System.out.println("Reference Value :\n" + eref1);

		r2.call("print", eref1);
		System.out.println("Reference R print:\n" + Utils.indentS4Print(r2.getStatus()));

		System.exit(0);

	}

}
