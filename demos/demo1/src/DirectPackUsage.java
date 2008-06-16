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

import java.awt.BorderLayout;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;

import org.rosuda.ibase.RemoteUtil;
import org.rosuda.ibase.SMarkerInterface;
import org.rosuda.ibase.SVarInterface;
import org.rosuda.ibase.SVarInterfaceRemote;
import org.rosuda.ibase.SVarInterfaceRemoteImpl;
import org.rosuda.ibase.SVarSetInterface;
import org.rosuda.ibase.plots.HistCanvas;
import org.rosuda.iplots.Framework;

import remoting.RServices;
import server.DirectJNI;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class DirectPackUsage {
	private static SVarInterfaceRemoteImpl svarImpl;
	public static void main(String args[]) throws Exception {

		RServices rs = DirectJNI.getInstance().getRServices();
		
		System.out.println(rs.consoleSubmit("data(iris)"));
		System.out.println(rs.consoleSubmit("e<-iset.new('iris',iris)"));
		
		SVarSetInterface set=org.rosuda.iplots.Framework.F.getCurrentSet();
		SVarInterface var=set.byName("Sepal.Length");
		
		JFrame f=new JFrame("test");
				
		SVarInterface varProxy=RemoteUtil.getSVarWrapper(var.getSVarRemote());
		SMarkerInterface markerProxy=RemoteUtil.getSMarkerWrapper(set.getMarkerRemote());
		
		HistCanvas histCanvas=new HistCanvas(0,f,varProxy,markerProxy);		
		f.add(histCanvas.getComponent());
		
		f.pack();
		f.setVisible(true);
		f.setSize(400,400);
		
		/*
		
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
		*/
	}


}
