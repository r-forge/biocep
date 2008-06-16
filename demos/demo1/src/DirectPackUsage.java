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
import java.awt.MenuBar;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.rosuda.ibase.RemoteUtil;
import org.rosuda.ibase.SMarkerInterface;
import org.rosuda.ibase.SVarInterface;
import org.rosuda.ibase.SVarInterfaceRemote;
import org.rosuda.ibase.SVarInterfaceRemoteImpl;
import org.rosuda.ibase.SVarSetInterface;
import org.rosuda.ibase.plots.HistCanvas;
import org.rosuda.ibase.plots.ScatterCanvas;
import org.rosuda.ibase.toolkit.FrameDevice;
import org.rosuda.ibase.toolkit.TFrame;
import org.rosuda.iplots.Framework;

import com.sun.codemodel.JLabel;

import remoting.RServices;
import server.DirectJNI;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class DirectPackUsage {
	private static SVarInterfaceRemoteImpl svarImpl;
	public static void main(String args[]) throws Exception {

		RServices rs = DirectJNI.getInstance().getRServices();


		System.out.println(rs.consoleSubmit("s <- iset.new('iris',list(a=rnorm(1000), b=rnorm(1000)))"));
		//System.out.println(rs.consoleSubmit("ihist(iris$Species)"));
		//System.out.println(rs.consoleSubmit("iplot(iris$Species)"));
		
		
	
		SVarSetInterface set=org.rosuda.iplots.Framework.F.getCurrentSet();
		SVarInterface var=set.byName("a");
		SVarInterface var2=set.byName("b");
		
		
				
		SVarInterface varProxy=RemoteUtil.getSVarWrapper(var.getRemote());
		SVarInterface varProxy2=RemoteUtil.getSVarWrapper(var2.getRemote());
		SMarkerInterface markerProxy=RemoteUtil.getSMarkerWrapper(set.getMarker().getRemote());
		
		JFrame f=new JFrame("test");

		
		//HistCanvas histCanvas=new HistCanvas(HistCanvas.SWINGGrDevID,new JFrame(),varProxy,markerProxy);				
		ScatterCanvas histCanvas=new ScatterCanvas(HistCanvas.SWINGGrDevID,new JFrame(),varProxy,varProxy2,markerProxy);
		
		
		/*
		MenuBar mb=histCanvas.getMenuBar();
		JMenuBar jmb=new JMenuBar();
		for (int i=0; i<mb.getMenuCount(); ++i) {
			JMenu m=new JMenu(mb.getMenu(i).getLabel());
			for (int j=0; j<mb.getMenu(i).getItemCount(); ++j) {
				JMenuItem item=new JMenuItem(mb.getMenu(i).getItem(j).getLabel());				
				ActionListener[] listeners=mb.getMenu(i).getItem(j).getActionListeners();
				if (listeners.length>0){
					item.addActionListener(listeners[0]);
				}				
				item.setActionCommand(mb.getMenu(i).getItem(j).getActionCommand());				
				
				m.add(item);
			
			}
			jmb.add(m);
		}
		*/
		
		JPanel pa=new JPanel(new BorderLayout());
		pa.add(histCanvas.getComponent(),BorderLayout.CENTER);
		//pa.add(jmb,BorderLayout.NORTH);
		f.add(pa);		
		histCanvas.updateObjects();
		markerProxy.addDepend(histCanvas);
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
