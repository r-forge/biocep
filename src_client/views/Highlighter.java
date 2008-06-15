/*
 * Syntax Highlighting Test using a JTextPane
 *
 * By: Frank Hale <frankhale@gmail.com>
 * Date: 20 November 2006
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * A full copy of this license is at: http://www.gnu.org/licenses/gpl.txt
 *  
 */

package views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.Utils;
import views.highlighting.HighlightKit;
import views.highlighting.NonWrappingTextPane;

public class Highlighter {

	
	private Vector<String> keyword1Vector=new Vector<String>();
	private Vector<String> keyword2Vector=new Vector<String>();
	private Vector<String> keyword3Vector=new Vector<String>();
	
	
	public Vector<String> getKeyword1Vector() {
		return keyword1Vector;
	}
	
	public Vector<String> getKeyword2Vector() {
		return keyword2Vector;
	}
	
	public Vector<String> getKeyword3Vector() {
		return keyword3Vector;
	}
	
	private static Highlighter _highlighter = null;
	private static Integer lock = new Integer(0);

	private Highlighter() throws Exception {
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false);
		domFactory.setValidating(false);
		DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				if (systemId.endsWith("xmode.dtd"))
					// this deactivates the open office DTD
					return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
				else
					return null;
			}
		});
		Document document = documentBuilder.parse(Highlighter.class.getResourceAsStream("/modes/r.xml"));

		Node keywordsNode=Utils.catchNode(document.getDocumentElement(), "KEYWORDS");
		Vector<Node> v1 = new Vector<Node>();
		Utils.catchNodes(keywordsNode, "KEYWORD1", v1);
		for (Node n:v1) keyword1Vector.add(n.getTextContent());
		Collections.sort(keyword1Vector, new Comparator<String>(){
			public int compare(String o1, String o2) {
				if (o1.length()!=o2.length()) {
					return o1.length()-o2.length();
				} else {
					return o1.compareTo(o2);
				}
				
			}
		});
		
		Vector<Node> v2 = new Vector<Node>();
		Utils.catchNodes(keywordsNode, "KEYWORD2", v2);
		for (Node n:v2) keyword2Vector.add(n.getTextContent());
		Collections.sort(keyword2Vector, new Comparator<String>(){
			public int compare(String o1, String o2) {
				if (o1.length()!=o2.length()) {
					return o1.length()-o2.length();
				} else {
					return o1.compareTo(o2);
				}
				
			}
		});
		
		Vector<Node> v3 = new Vector<Node>();
		Utils.catchNodes(keywordsNode, "KEYWORD3", v3);
		for (Node n:v3) keyword3Vector.add(n.getTextContent());
		Collections.sort(keyword3Vector, new Comparator<String>(){
			public int compare(String o1, String o2) {
				if (o1.length()!=o2.length()) {
					return o1.length()-o2.length();
				} else {
					return o1.compareTo(o2);
				}				
			}
		});
	}
	
	
	public static Highlighter getInstance() {
		if (_highlighter != null)
			return _highlighter;
		synchronized (lock) {
			if (_highlighter == null) {
				try {
				_highlighter = new Highlighter();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _highlighter;
		}
	}

	
	public static void main(String args[]) throws Exception {
		Vector<String> v=new Vector<String>();
		v.add("ab");v.add("bbdb");v.add("a");v.add("b");
		Collections.sort(v, new Comparator<String>(){
			public int compare(String o1, String o2) {
				if (o1.length()!=o2.length()) {
					return o1.length()-o2.length();
				} else {
					return o1.compareTo(o2);
				}
				
			}
		});
		System.out.println(v);

		System.exit(0);

		JFrame frame = new JFrame("Syntax Highlighting Test <Use Java Keywords>");
		NonWrappingTextPane textPane = new NonWrappingTextPane();
		//textPane.setFont(new Font("Monospaced", Font.PLAIN, 20));
		JScrollPane scroll = new JScrollPane(textPane);
		textPane.setEditorKit(new HighlightKit());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scroll);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}
}
