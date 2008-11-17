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

import graphics.rmi.AbstractDockingWindowListener;
import graphics.rmi.RGui;
import graphics.rmi.spreadsheet.SpreadsheetPanel;
import http.HttpMarker;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import model.AbstractSpreadsheetModel;
import model.ModelUtils;
import model.SpreadsheetModelRemote;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;

public class CollaborativeSpreadsheetView extends DynamicView {
	SpreadsheetModelRemote _spreadsheetModelRemote;
	
	SpreadsheetModelRemote getSpreadsheetModelRemote() {
		return _spreadsheetModelRemote;
	}
	
	DockingWindowListener l=new AbstractDockingWindowListener(){			
		@Override
		public void windowClosed(DockingWindow arg0) {
			try {
				if (_spreadsheetModelRemote instanceof HttpMarker) {												
					((HttpMarker)_spreadsheetModelRemote).stopThreads();
				} 					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};	
	
	public CollaborativeSpreadsheetView(int id, int rowCount, int colCount,  RGui rgui) {
		super("", null, new JPanel(), id);
		
		addListener(l);
		
		try {
			_spreadsheetModelRemote = rgui.getR().newSpreadsheetTableModelRemote(rowCount, colCount);
			final String spreadsheetModelId = _spreadsheetModelRemote.getSpreadsheetModelId();
			final AbstractSpreadsheetModel spreadsheetModel = ModelUtils.getSpreadsheetTableModelWrapper(_spreadsheetModelRemote);			
			((JPanel) getComponent()).setLayout(new BorderLayout());
			((JPanel) getComponent()).add(new SpreadsheetPanel(spreadsheetModel, rgui));
			getViewProperties().setTitle("Server-side Spreadsheet <" + spreadsheetModelId + ">");				
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		
	}
		
	public CollaborativeSpreadsheetView(int id, String spreadsheetModelId,  RGui rgui) {
		super("", null, new JPanel(), id);
		
		addListener(l);	
		
		try {
			System.out.println ("---Step 1");
			_spreadsheetModelRemote = rgui.getR().getSpreadsheetTableModelRemote(spreadsheetModelId);
			System.out.println ("---Step 2");
			final AbstractSpreadsheetModel spreadsheetModel = ModelUtils.getSpreadsheetTableModelWrapper(_spreadsheetModelRemote);
			System.out.println ("---Step 3");
			((JPanel) getComponent()).setLayout(new BorderLayout());
			((JPanel) getComponent()).add(new SpreadsheetPanel(spreadsheetModel, rgui));
			System.out.println ("---Step 4");
			getViewProperties().setTitle("Server-side Spreadsheet View <" + spreadsheetModelId + ">");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}			
	}

}

