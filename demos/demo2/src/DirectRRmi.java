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
import java.rmi.Naming;
import java.util.Arrays;
import java.util.Properties;

import org.kchine.r.packages.biobase.ExpressionSet;
import org.kchine.r.packages.rGlobalEnv.rGlobalEnvFunction;
import org.kchine.r.RNamedArgument;
import org.kchine.r.RNumeric;
import org.kchine.r.packages.vsn.vsnFunction;
import org.kchine.r.packages.vsn.Vsn;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.rpf.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectRRmi {
	public static void main(String[] args) throws Throwable {
		
		
		final RServices r = ((RServices) Naming.lookup("RSERVANT_1"));
		GDDevice  device = r.newDevice(400, 400);
		r.consoleSubmit("library(vsn);data(kidney);plot(exprs(kidney));");
		byte[] pdf=device.getEmf();
		
		
		RandomAccessFile raf=new RandomAccessFile("c:/tt.emf","rw");
		raf.setLength(0);
		raf.write(pdf);
		raf.close();
		System.out.println("Available Packages : " + Arrays.toString(r.listPackages()));
		
		
		System.exit(0);
		
		Properties props=new Properties();
		props.put("naming.mode", "db");
		r.export(props, "Hey", false);
		
		System.exit(0);
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