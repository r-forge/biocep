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
import java.rmi.Naming;

import org.kchine.r.packages.rGlobalEnv.A;
import org.kchine.r.packages.rGlobalEnv.B;
import org.kchine.r.packages.rGlobalEnv.E;
import org.kchine.r.server.RServices;
import org.kchine.r.server.Utils;
import org.kchine.r.RChar;
import org.kchine.r.RNumeric;
import org.kchine.r.RNumericRef;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DistributedRObjects {
	public static void main(String[] args) throws Exception {

		RServices r1 = ((RServices) Naming.lookup("RSERVANT_1"));
		RServices r2 = ((RServices) Naming.lookup("RSERVANT_2"));
		RServices r3 = ((RServices) Naming.lookup("RSERVANT_3"));

		E eref1 = (E) r1.getReference("new('E')");

		RNumericRef num = (RNumericRef) r3.getReference("c(100)");
		eref1.getC().setS(num);

		B nb = (B) r2.getReference("new('B')");
		nb.setV(new RNumeric(996));
		eref1.setB(nb);

		A na = (A) r3.getReference("new('A')");
		na.setX(new RChar("LLLLLLL"));
		nb.setQ(na);

		System.out.println("Reference Value :\n" + eref1);

		r2.call("print", eref1);
		System.out.println("Reference R print:\n" + Utils.indentS4Print(r2.getStatus()));

		System.exit(0);

	}

}
