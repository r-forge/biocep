/*
 * Copyright (C) 2007 EMBL-EBI
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
package graphics.pop;

import graphics.rmi.JGDPanelPop;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.GDState;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class GDContainerBag implements GDContainer {

	private static final int MAX_NBR_EVENTS_ON_POP = 100000;
	private Vector<GDObject> _actions = new Vector<GDObject>();
	private Dimension _size = null;
	private GDState _gs;
	private int _devNr=-1;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(JGDPanelPop.class);

	public GDContainerBag(int w, int h) {
		// System.out.println("GDContainerBag");
		_size = new Dimension(w, h);
		_gs = new GDState();
		_gs.f = new Font(null, 0, 12);
	}

	synchronized public Vector<GDObject> popAllGraphicObjects() {
		// System.out.println("popAllGraphicObjects");
		if (_actions.size() == 0)
			return null;
		Vector<GDObject> result = (Vector<GDObject>) _actions.clone();
		if (result.size() > MAX_NBR_EVENTS_ON_POP) {
			int delta = result.size() - MAX_NBR_EVENTS_ON_POP;
			for (int i = 0; i < delta; ++i) {
				result.remove(result.size() - 1);
			}
		}
		for (int i = 0; i < result.size(); ++i)
			_actions.remove(0);
		return result;
	}
	
	public boolean hasGraphicObjects() {
		return _actions.size()>0;
	}
	
	public Dimension getSize() throws RemoteException {
		return _size;
	}

	public void setSize(int w, int h) {
		_size = new Dimension(w, h);
	}

	public void add(GDObject o) throws RemoteException {
		_actions.add(o);
	}

	public void closeDisplay() throws RemoteException {
		_actions.add(new GDCloseDisplay());
	}

	public int getDeviceNumber() throws RemoteException {
		return _devNr;
	}

	public Font getGFont() throws RemoteException {
		return _gs.f;
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		return null;
	}

	synchronized public void reset() throws RemoteException {
		_actions = new Vector<GDObject>();
		_actions.add(new GDReset());
	}

	public void setDeviceNumber(int dn) throws RemoteException {
		if (_devNr==-1) _devNr = dn;
		//_actions.add(new GDSetDeviceNumber(dn));
	}

	public void setGFont(Font f) throws RemoteException {
		_gs.f = f;
		_actions.add(new GDSetGFont(f));
	}

	public void syncDisplay(boolean finish) throws RemoteException {
		_actions.add(new GDSyncDisplay(finish));
	}

}
