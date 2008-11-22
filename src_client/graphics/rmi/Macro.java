package graphics.rmi;

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

import model.ImportInfo;
import net.java.dev.jspreadsheet.CellRange;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import remoting.RServices;
import server.ServerManager;
import util.Utils;

public class Macro {
	public Macro() {
	}

	public Macro(String _name, String _label, Vector<VariablesChangeListener> listeners, Vector<CellsChangeListener> listeners2, Vector<MacroScript> _scripts,
			HashSet<String> probes) {
		this._name = _name;
		if (_label == null || _label.equals(""))
			this._label = _name;
		else
			this._label = _label;
		_varsListeners = listeners;
		_cellsListeners = listeners2;
		this._scripts = _scripts;
		this._probes = probes;
	}

	String _name;
	String _label;
	Vector<VariablesChangeListener> _varsListeners = new Vector<VariablesChangeListener>();
	Vector<CellsChangeListener> _cellsListeners = new Vector<CellsChangeListener>();
	Vector<MacroScript> _scripts = new Vector<MacroScript>();
	HashSet<String> _probes = new HashSet<String>();

	public String[] getProbes() {
		String[] result = new String[_probes.size()];
		int i = 0;
		for (String s : _probes)
			result[i++] = s;
		return result;
	}

	public void sourceAll(RGui rgui) {

		try {
			
			//for (Macro M:rgui.getMacros())
			Macro M=this;
			{
				for (VariablesChangeListener v : M.getVarsListeners())
					((MacroVariablesChangeListener) v).setEnabled(false);
				for (CellsChangeListener v : M.getCellsListeners())
					((MacroCellsChangeListener) v).setEnabled(false);
			}
			
			

			for (MacroScript s : _scripts) {
				try {
					s.sourceScript(rgui.getR());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			//for (Macro M:rgui.getMacros())
			M=this;
			{
				for (VariablesChangeListener v : M.getVarsListeners())
					((MacroVariablesChangeListener) v).setEnabled(true);
				for (CellsChangeListener v : M.getCellsListeners())
					((MacroCellsChangeListener) v).setEnabled(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class MacroVariablesChangeListener implements VariablesChangeListener {

		Vector<String> variables;
		boolean enabled = true;

		public MacroVariablesChangeListener(Vector<String> variables) {
			this.variables = variables;
		}

		public void variablesChanged(VariablesChangeEvent event) {
			if (!enabled)
				return;
			for (int i = 0; i < variables.size(); ++i) {
				if (!event.getVariablesHashSet().contains(variables.elementAt(i)))
					return;
			}
			sourceAll(event.getRGui());
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String toString() {
			return variables.toString();
		}

	}

	class MacroCellsChangeListener implements CellsChangeListener {
		CellRange range;
		boolean enabled = true;

		public MacroCellsChangeListener(CellRange range) {
			this.range = range;
		}

		public void cellsChanged(CellsChangeEvent event) {
			if (!enabled)
				return;
			
			CellRange eventRange=event.getRange();
			if ( (eventRange.getStartCol() >= range.getStartCol() && eventRange.getStartCol() <= range.getEndCol()
				 && eventRange.getStartRow() >= range.getStartRow() && eventRange.getStartRow() <= range.getEndRow())
				 ||
				 (eventRange.getEndCol() >= range.getStartCol() && eventRange.getEndCol() <= range.getEndCol()
						 && eventRange.getEndRow() >= range.getStartRow() && eventRange.getEndRow() <= range.getEndRow())
						 
				||
				 (eventRange.getEndCol() >= range.getStartCol() && eventRange.getEndCol() <= range.getEndCol()
						 && eventRange.getStartRow() >= range.getStartRow() && eventRange.getStartRow() <= range.getEndRow())
						 
				||
				 (eventRange.getStartCol() >= range.getStartCol() && eventRange.getStartCol() <= range.getEndCol()
						 && eventRange.getEndRow() >= range.getStartRow() && eventRange.getEndRow() <= range.getEndRow())		 
						 
				||		 				 
				 (range.getStartCol() >= eventRange.getStartCol() && range.getStartCol() <= eventRange.getEndCol()
						 && range.getStartRow() >= eventRange.getStartRow() && range.getStartRow() <= eventRange.getEndRow()) 
			    ||		 				 
				 (range.getEndCol() >= eventRange.getStartCol() && range.getEndCol() <= eventRange.getEndCol()
						 && range.getEndRow() >= eventRange.getStartRow() && range.getEndRow() <= eventRange.getEndRow())
				||		 				 
				 (range.getEndCol() >= eventRange.getStartCol() && range.getEndCol() <= eventRange.getEndCol()
						 && range.getStartRow() >= eventRange.getStartRow() && range.getStartRow() <= eventRange.getEndRow())
						 
				||		 				 
				 (range.getStartCol() >= eventRange.getStartCol() && range.getStartCol() <= eventRange.getEndCol()
						 && range.getEndRow() >= eventRange.getStartRow() && range.getEndRow() <= eventRange.getEndRow())		 
			
				) {
			
				sourceAll(event.getRGui());
				
			}
				 
			
			
			
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String toString() {
			return range.toString();
		}
	}

	public interface MacroScript {
		public void sourceScript(RServices r) throws Exception;
	};

	private static class RScript implements MacroScript {
		private String script;

		public RScript(String script) {
			this.script = script;
		}

		public void sourceScript(RServices r) throws Exception {
			r.sourceFromBuffer(script);
		}

		public String toString() {
			return script;
		}
	}

	private static class GroovyScript implements MacroScript {
		private String script;

		public GroovyScript(String script) {
			this.script = script;
		}

		public void sourceScript(RServices r) throws Exception {
			r.groovyExecFromBuffer(script);
		}

		public String toString() {
			return script;
		}
	}

	private static class PythonScript implements MacroScript {
		private String script;

		public PythonScript(String script) {
			this.script = script;
		}

		public void sourceScript(RServices r) throws Exception {
			r.pythonExecFromBuffer(script);
		}

		public String toString() {
			return script;
		}
	}

	public static Vector<Macro> getMacros(String installDir) throws Exception {

		File macrosFile = new File(installDir + "/macros.xml");
		if (!macrosFile.exists()) {
			PrintWriter pw = new PrintWriter(macrosFile);
			pw.println("<macros></macros>");
			pw.close();
		}

		Vector<Macro> macros = new Vector<Macro>();
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
			Vector<VariablesChangeListener> varsListeners = new Vector<VariablesChangeListener>();
			Vector<CellsChangeListener> cellsListeners = new Vector<CellsChangeListener>();
			Vector<MacroScript> scripts = new Vector<MacroScript>();
			HashSet<String> probes = new HashSet<String>();
			try {
				NodeList listenerNodes = Utils.catchNode(macroNodes.elementAt(i), "listeners").getChildNodes();
				for (int j = 0; j < listenerNodes.getLength(); ++j) {
					if (listenerNodes.item(j).getNodeName().equals("variables")) {
						String variableNames = listenerNodes.item(j).getAttributes().getNamedItem("list").getNodeValue();
						Vector<String> list = new Vector<String>();
						StringTokenizer st = new StringTokenizer(variableNames, ",");
						while (st.hasMoreElements())
							list.add(st.nextToken());
						probes.addAll(list);
						varsListeners.add(m.new MacroVariablesChangeListener(list));
					} else if (listenerNodes.item(j).getNodeName().equals("cells")) {
						String range = listenerNodes.item(j).getAttributes().getNamedItem("range").getNodeValue();
						CellRange cellrange = ImportInfo.getRange(range);
						cellsListeners.add(m.new MacroCellsChangeListener(cellrange));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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

			}

			m.setName(name);
			m.setLabel(label);
			m.setVarsListeners(varsListeners);
			m.setCellsListeners(cellsListeners);
			m.setScripts(scripts);
			m.setProbes(probes);
			macros.add(m);

			//System.out.println("name:" + name);
			//System.out.println("label:" + label);
			//System.out.println("varsListeners:" + varsListeners);
			//System.out.println("cellsListeners:" + cellsListeners);
			//System.out.println("scripts:" + scripts);
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

	public Vector<MacroScript> getScripts() {
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

	public void setScripts(Vector<MacroScript> _scripts) {
		this._scripts = _scripts;
	}

	public void setProbes(HashSet<String> _probes) {
		this._probes = _probes;
	}

}
