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
import java.io.PrintStream;
import java.util.Arrays;
import org.kchine.r.packages.biobase.ExpressionSet;
import org.kchine.r.packages.rGlobalEnv.Point;
import org.kchine.r.packages.rGlobalEnv.rGlobalEnvFunction;
import org.kchine.r.RNamedArgument;
import org.kchine.r.RNumeric;
import org.kchine.r.packages.vsn.Vsn;
import org.kchine.r.packages.vsn.vsnFunction;
import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectPackUsage {
	public static void main(String args[]) throws Exception {
		System.setErr(new PrintStream(System.out));
		DirectJNI.init();
		RServices r = DirectJNI.getInstance().getRServices();
		
		
		r.consoleSubmit("library(vsn);data(kidney)");
		ExpressionSet k=(ExpressionSet)r.getObject("kidney");		
		System.out.println(k);
		
		r.putAndAssign(k, "x");
		
		r.consoleSubmit("x");
		System.out.println(r.getStatus());
		
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
