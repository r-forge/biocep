package org.kchine.r.workbench.macros;

import http.HttpMarker;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.kchine.r.server.Utils;
import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.server.spreadsheet.Formula;
import org.kchine.r.server.spreadsheet.ImportInfo;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemoteImpl;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.CellsChangeListener;

import org.kchine.r.workbench.VariablesChangeListener;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import server.ServerManager;

public class Macro implements MacroInterface{
	public Macro() {
	}

	public Macro(String _name, String _label, boolean show, Vector<VariablesChangeListener> listeners, Vector<CellsChangeListener> listeners2,
			Vector<Script> _scripts, HashSet<String> probes) {
		this._name = _name;
		if (_label == null || _label.equals(""))
			this._label = _name;
		else
			this._label = _label;

		this._show = show;
		this._varsListeners = listeners;
		this._cellsListeners = listeners2;
		this._scripts = _scripts;
		this._probes = probes;
	}

	protected String _name;
	protected String _label;
	protected boolean _show;
	protected Vector<VariablesChangeListener> _varsListeners = new Vector<VariablesChangeListener>();
	protected Vector<CellsChangeListener> _cellsListeners = new Vector<CellsChangeListener>();
	protected Vector<Script> _scripts = new Vector<Script>();
	protected HashSet<String> _probes = new HashSet<String>();

	public String[] getProbes() {
		String[] result = new String[_probes.size()];
		int i = 0;
		for (String s : _probes)
			result[i++] = s;
		return result;
	}
	
	public void setEnabled(boolean enabled) {
		for (VariablesChangeListener v : getVarsListeners())
			((MacroVariablesChangeListener) v).setEnabled(enabled);
		for (CellsChangeListener v : getCellsListeners())
			((MacroCellsChangeListener) v).setEnabled(enabled);			
	}

	public void sourceAll(final RGui rgui) {
		rgui.pushTask(new Runnable() {
			public void run() {
				try {
					rgui.getRLock().lock();
					for (MacroInterface M : rgui.getMacros()) M.setEnabled(false);

					for (Script s : _scripts) {
						try {
							s.sourceScript(rgui.getR());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (rgui.getR() instanceof HttpMarker) ((HttpMarker)rgui.getR()).popActions();
					
					for (MacroInterface M : rgui.getMacros()) M.setEnabled(true);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					rgui.getRLock().unlock();
				}
			}
		});

	}

	
	public static Vector<MacroInterface> getMacros(String installDir) throws Exception {

		File macrosFile = new File(installDir + "/macros.xml");
		if (!macrosFile.exists()) {
			PrintWriter pw = new PrintWriter(macrosFile);
			pw.println("<macros></macros>");
			pw.close();
		}

		Vector<MacroInterface> macros = new Vector<MacroInterface>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(new FileInputStream(macrosFile));

		Vector<Node> macroNodes = new Vector<Node>();
		Utils.catchNodes(document.getDocumentElement(), "macro", macroNodes);
		for (int i = 0; i < macroNodes.size(); ++i) {

			Macro m = new Macro();

			NamedNodeMap attrs = macroNodes.elementAt(i).getAttributes();
			String name = null;
			if (attrs.getNamedItem("name") != null)
				name = attrs.getNamedItem("name").getNodeValue();
			String label = null;
			if (attrs.getNamedItem("label") != null)
				label = attrs.getNamedItem("label").getNodeValue();

			boolean show = true;
			if (attrs.getNamedItem("show") != null) {
				try {
					show = new Boolean(attrs.getNamedItem("show").getNodeValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Vector<VariablesChangeListener> varsListeners = new Vector<VariablesChangeListener>();
			Vector<CellsChangeListener> cellsListeners = new Vector<CellsChangeListener>();
			Vector<Script> scripts = new Vector<Script>();
			HashSet<String> probes = new HashSet<String>();

			Node listenersRoot = Utils.catchNode(macroNodes.elementAt(i), "listeners");
			if (listenersRoot != null) {
				try {
					NodeList listenerNodes = listenersRoot.getChildNodes();
					for (int j = 0; j < listenerNodes.getLength(); ++j) {
						if (listenerNodes.item(j).getNodeName().equals("variables")) {
							String variableNames = listenerNodes.item(j).getAttributes().getNamedItem("list").getNodeValue();
							Vector<String> list = new Vector<String>();
							StringTokenizer st = new StringTokenizer(variableNames, ",");
							while (st.hasMoreElements())
								list.add(st.nextToken());
							probes.addAll(list);
							varsListeners.add(new MacroVariablesChangeListener(list,m));
						} else if (listenerNodes.item(j).getNodeName().equals("cells")) {
							NamedNodeMap attributesMap=listenerNodes.item(j).getAttributes();
							String range = attributesMap.getNamedItem("range").getNodeValue();
							Color color = (attributesMap.getNamedItem("color")==null ? null : getColor((String)attributesMap.getNamedItem("color").getNodeValue()));
							String ssname = null;
							if (attributesMap.getNamedItem("name") != null)
								ssname = attributesMap.getNamedItem("name").getNodeValue();
							else 
								ssname=SpreadsheetModelRemoteImpl.SPREADSHEET_NAME_PREFIX+"0";							
							CellRange cellrange = ImportInfo.getRange(range);
							cellsListeners.add(new MacroCellsChangeListener(ssname, cellrange,color,m));
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			try {
				NodeList scriptNodes = Utils.catchNode(macroNodes.elementAt(i), "actions").getChildNodes();
				for (int j = 0; j < scriptNodes.getLength(); ++j) {
					if (scriptNodes.item(j).getNodeName().equals("r")) {
						scripts.add(new RScript(getScriptValue(scriptNodes.item(j))));
					} else if (scriptNodes.item(j).getNodeName().equals("groovy")) {
						scripts.add(new GroovyScript(getScriptValue(scriptNodes.item(j))));
					} else if (scriptNodes.item(j).getNodeName().equals("python")) {
						scripts.add(new PythonScript(getScriptValue(scriptNodes.item(j))));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			m.setName(name);
			m.setLabel(label);
			m.setVarsListeners(varsListeners);
			m.setCellsListeners(cellsListeners);
			m.setScripts(scripts);
			m.setProbes(probes);
			m.setShow(show);
			macros.add(m);

		}
		
		
		
		Vector<Node> dataLinksNodes = new Vector<Node>();
		Utils.catchNodes(document.getDocumentElement(), "datalink", dataLinksNodes);
		for (int i = 0; i < dataLinksNodes.size(); ++i) {


			NamedNodeMap attrs = dataLinksNodes.elementAt(i).getAttributes();
			
			String name = null;
			if (attrs.getNamedItem("name") != null)
				name = attrs.getNamedItem("name").getNodeValue();
			else 
				name=SpreadsheetModelRemoteImpl.SPREADSHEET_NAME_PREFIX+"0";
			
			String variable = attrs.getNamedItem("variable").getNodeValue();
			
			String range = attrs.getNamedItem("range").getNodeValue();
			CellRange cellrange=ImportInfo.getRange(range);

			Color color = (attrs.getNamedItem("color")==null ? null : getColor((String)attrs.getNamedItem("color").getNodeValue()));
			
			
			String type = "numeric";
			if (attrs.getNamedItem("type") != null)
				type = attrs.getNamedItem("type").getNodeValue();
			
			Macro m1 = new DataLinkMacro();
			Macro m2 = new DataLinkMacro();
			
			Vector<VariablesChangeListener> varsListeners1 = new Vector<VariablesChangeListener>();
			Vector<CellsChangeListener> cellsListeners1 = new Vector<CellsChangeListener>();
			Vector<Script> scripts1 = new Vector<Script>();
			HashSet<String> probes1 = new HashSet<String>();
			probes1.add(variable);
			
			varsListeners1.add(new MacroVariablesChangeListener(new String[]{variable},m1));
			
			String rg=null;
			if (type.equals("data.frame.auto.row")) {
				rg=Formula.getCellString(cellrange.getStartRow(), cellrange.getStartCol()-1);
			} else if (type.equals("data.frame.auto.col")) {
				rg=Formula.getCellString(cellrange.getStartRow()-1, cellrange.getStartCol());
			} else if (type.equals("data.frame.auto.row.col")) {
				rg=Formula.getCellString(cellrange.getStartRow()-1, cellrange.getStartCol()-1);
			} else {
				rg=range.substring(0, range.indexOf(':'));
			}
			scripts1.add(new RScript("cells.put("+variable+",'"+rg+"'"+(name==null?"":",name='"+name+"'")+")"));

			Vector<VariablesChangeListener> varsListeners2 = new Vector<VariablesChangeListener>();
			Vector<CellsChangeListener> cellsListeners2 = new Vector<CellsChangeListener>();
			Vector<Script> scripts2 = new Vector<Script>();
			HashSet<String> probes2 = new HashSet<String>();
			cellsListeners2.add(new MacroCellsChangeListener(name,cellrange,color,m2));
			scripts2.add(new RScript(variable+"=cells.get('"+range+"',type='"+type+"'"+(name==null?"":",name='"+name+"'")+")"));
			
			m1.setName(null);
			m1.setLabel(null);
			m1.setVarsListeners(varsListeners1);
			m1.setCellsListeners(cellsListeners1);
			m1.setScripts(scripts1);
			m1.setProbes(probes1);
			m1.setShow(false);
			
			m2.setName(null);
			m2.setLabel(null);
			m2.setVarsListeners(varsListeners2);
			m2.setCellsListeners(cellsListeners2);
			m2.setScripts(scripts2);
			m2.setProbes(probes2);
			m2.setShow(false);
			
			
			macros.add(m1);
			macros.add(m2);
		}

		
		
		
		return macros;
	}

	private static String getScriptValue(Node node) throws Exception {

		if (node.getAttributes().getNamedItem("script") != null) {
			File f = new File(ServerManager.INSTALL_DIR + "/resources/" + node.getAttributes().getNamedItem("script").getNodeValue());

			StringBuffer result = new StringBuffer();

			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = br.readLine()) != null) {
				result.append(line + "\n");
			}
			return result.toString();

		} else if (node.getAttributes().getNamedItem("file") != null) {
			File f = new File(node.getAttributes().getNamedItem("file").getNodeValue());

			StringBuffer result = new StringBuffer();

			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = br.readLine()) != null) {
				result.append(line + "\n");
			}
			return result.toString();
		} else {
			String result = node.getChildNodes().item(0).getNodeValue().trim();
			return result;
		}

	}

	public String getName() {
		return _name;
	}

	public String getLabel() {
		return _label;
	}

	public Vector<VariablesChangeListener> getVarsListeners() {
		return _varsListeners;
	}

	public Vector<CellsChangeListener> getCellsListeners() {
		return _cellsListeners;
	}

	public Vector<Script> getScripts() {
		return _scripts;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public void setLabel(String _label) {
		if (_label == null || _label.equals(""))
			this._label = _name;
		else
			this._label = _label;
	}

	public void setVarsListeners(Vector<VariablesChangeListener> listeners) {
		_varsListeners = listeners;
	}

	public void setCellsListeners(Vector<CellsChangeListener> listeners) {
		_cellsListeners = listeners;
	}

	public void setScripts(Vector<Script> _scripts) {
		this._scripts = _scripts;
	}

	public void setProbes(HashSet<String> _probes) {
		this._probes = _probes;
	}

	public boolean isShow() {
		return _show;
	}

	public void setShow(boolean _show) {
		this._show = _show;
	}
	
	public static String getHelloWorldAction() {
		return 
		 "<macro name=\"Hello\" label=\"Say Hello Twice\" >\n"						
		+"  <actions>\n"		
		+"    <r >\n"
		+"    	print(\"Hello\")\n"
		+" 	 </r>\n"
		+" 	 <groovy>\n"
		+"	    r=server.R.getInstance()\n"
		+"	    r.consoleSubmit(\"print('Hello')\")\n"
		+" 	 </groovy>\n"
		+"  </actions>\n"
	    +"</macro>\n";		
	}
	
	public static String getHelloWorldVars() {
		return 
		 "<macro name=\"Hello\" label=\"Say Hello If x or y Changes \" show= \"false\" >\n"
		+"  <listeners>\n"
		+"     <variables list=\"x\"/>\n"
		+"     <variables list=\"y\"/>\n"
	    +"  </listeners>\n"
		+"  <actions>\n"		
		+"    <r >\n"
		+"    	print(\"Hello\")\n"
		+" 	 </r>\n"
		+" 	 <groovy>\n"
		+"	    r=server.R.getInstance()\n"
		+"	    r.consoleSubmit(\"print('Hello')\")\n"
		+" 	 </groovy>\n"
		+"  </actions>\n"
	    +"</macro>\n";		
	}

	public static String getHelloWorldCells() {
		return 
		 "<macro name=\"Hello\" label=\"Say Hello Twice If A1:C3 Changes\" show= \"false\" >\n"
		+"  <listeners>\n"
		+"    <cells range=\"A1:C3\"/>\n"				
	    +"  </listeners>\n"
		+"  <actions>\n"		
		+"    <r >\n"
		+"    	print(\"Hello\")\n"
		+" 	 </r>\n"
		+" 	 <groovy>\n"
		+"	    r=server.R.getInstance()\n"
		+"	    r.consoleSubmit(\"print('Hello')\")\n"
		+" 	 </groovy>\n"
		+"  </actions>\n"
	    +"</macro>\n";		
	}

	public static String getHelloWorldDataLink() {
		return 
		 "<datalink variable=\"x\" range=\"A1:B2\" />\n";
	}
	
	static Color getColor(String color) {
		Color result=null;
		if (color.equalsIgnoreCase("red")) result=Color.red; 
		else if (color.equalsIgnoreCase("green")) result=Color.green;
		else if (color.equalsIgnoreCase("blue")) result=Color.blue;
		else if (color.equalsIgnoreCase("yellow")) result=Color.yellow;
		else if (color.equalsIgnoreCase("cyan")) result=Color.cyan;
		else if (color.equalsIgnoreCase("white")) result=Color.white;
		else if (color.equalsIgnoreCase("black")) result=Color.black;
		else if (color.equalsIgnoreCase("orange")) result=Color.orange;
		else if (color.equalsIgnoreCase("pink")) result=Color.pink;
		else if (color.equalsIgnoreCase("gray")) result=Color.gray;
		if (color.startsWith("#")) {
			result= Color.decode(color);
		}
		return result;		
	}
	
	public static void main(String[] args) {
		System.out.println(Color.decode("#FF0000"));
	}
}
