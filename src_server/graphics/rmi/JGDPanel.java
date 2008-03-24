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
package graphics.rmi;

import java.awt.*;
import java.awt.event.*;
import server.LocalGraphicNotifier;
import uk.ac.ebi.microarray.pools.RemotePanel;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.GDState;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class JGDPanel extends RemotePanel implements GDContainer {
	static final long serialVersionUID = 85376389L;
	private Vector<GDObject> _l;
	public static boolean forceAntiAliasing = true;
	private GDState _gs;
	private Dimension _lastSize;
	private int _devNr = -1;
	private Dimension _prefSize;
	private GraphicNotifier _graphicNotifier;
	private Long _lastResizeTime = null;
	private GDContainerAdapterImpl _gdcServer = null;
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(JGDPanel.class);

	public JGDPanel(double w, double h, GraphicNotifier graphicNotifier) {
		this((int) w, (int) h, graphicNotifier);
	}

	public JGDPanel(int w, int h, GraphicNotifier graphicNotifier) {
		super(true);
		setOpaque(true);
		setSize(w, h);
		_prefSize = new Dimension(w, h);
		_l = new Vector<GDObject>();
		_gs = new GDState();
		_gs.f = new Font(null, 0, 12);
		setSize(w, h);
		_lastSize = getSize();
		setBackground(Color.white);
		_graphicNotifier = graphicNotifier;

	}

	public void init() {
		try {
			if (!(_graphicNotifier instanceof LocalGraphicNotifier)) {
				_gdcServer = new GDContainerAdapterImpl(this);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				_graphicNotifier.registerContainer(_gdcServer);
			} else {
				_graphicNotifier.registerContainer(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (_lastResizeTime != null && ((System.currentTimeMillis() - _lastResizeTime) > 1000)) {
						try {
							_graphicNotifier.fireSizeChangedEvent(_devNr);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						_lastResizeTime = null;
					}

					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}
				}

			}
		}).start();

		addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					new Thread(new Runnable() {
						public void run() {
							try {
								_graphicNotifier.putLocation(new Point(e.getX(), e.getY()));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}).start();
				}

			}

			public void mouseReleased(MouseEvent e) {
			}

		});

	}

	public void dispose() throws RemoteException {
		_graphicNotifier.executeDevOff(_devNr);
		if (_gdcServer != null) {
			UnicastRemoteObject.unexportObject((Remote) _gdcServer, true);
		}
	}

	public void setGFont(Font f) {
		_gs.f = f;
	}

	public Font getGFont() throws RemoteException {
		return _gs.f;
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		if (getGraphics() == null)
			return null;
		return getGraphics().getFontMetrics(_gs.f);
	}

	public void setDeviceNumber(int dn) {
		this._devNr = dn;
	}

	public int getDeviceNumber() {
		return this._devNr;
	}

	public void closeDisplay() {
	}

	public void syncDisplay(boolean finish) {
		repaint();
	}

	public synchronized Vector getGDOList() {
		return _l;
	}

	public synchronized void add(GDObject o) {
		_l.add(o);
	}

	public synchronized void reset() {
		_l.removeAllElements();
	}

	public Dimension getPreferredSize() {
		return new Dimension(_prefSize);
	}

	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension d = getSize();

		if (!d.equals(_lastSize)) {
			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;
			return;
		} else {

		}

		if (forceAntiAliasing) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		g.setClip(0, 0, d.width, d.height); // reset clipping rect
		g.setColor(Color.white);
		g.fillRect(0, 0, d.width, d.height);
		while (i < j) {
			GDObject o = (GDObject) _l.elementAt(i++);
			o.paint(this, _gs, g);
		}
	}

}
