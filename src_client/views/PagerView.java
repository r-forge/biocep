/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package views;

import graphics.rmi.RGui;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.RandomAccessFile;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PagerView extends DynamicView {
	RGui _rgui;
	public PagerView(String title, Icon icon, int id, RGui rgui, String file, byte[] content,String header, boolean deleteFile ) {		
		super(title, icon, new JPanel(), id);
		_rgui=rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		
		JEditorPane pane = null;
		try {
			File f=new File(System.getProperty("java.io.tmpdir")+"/"+file);
			System.out.println(f.toURI().toURL());
			RandomAccessFile raf=new RandomAccessFile(f, "rw");
			raf.setLength(0);
			raf.write(content);
			raf.close();			
			pane=new JEditorPane(f.toURI().toURL());
			pane.setFont(new Font("Monospaced", Font.PLAIN, 12));

			pane.setEditable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		((JPanel) getComponent()).add(new JScrollPane(pane));
	}

}
