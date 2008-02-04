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
package graphics.rmi;

import graphics.pop.GDActionMarker;
import graphics.pop.GDDevice;
import graphics.pop.GDReset;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.GDState;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class JGDPanelPop extends JBufferedImagePanel {
	static final long serialVersionUID = 85376389L;
	private Vector<GDObject> _l;
	private static boolean _forceAntiAliasing = true;
	private GDState _gs;
	private Dimension _lastSize;
	private Dimension _prefSize;
	private Long _lastResizeTime = null;
	private GDDevice _gdDevice = null;
	private boolean _autoPop;
	private boolean _autoResize;
	private boolean _locatorActive;
	
	private AbstractAction[] _actions;
	private boolean mouseInside;
	private Point2D mouseLocation = null;
	private Point2D[] realLocations = null;
	private boolean trackMouse = false;
	private ReentrantLock _protectR = null;
	private ConsoleLogger _consoleLogger = null;
	
	private boolean _stopPopThread = false;	
	private boolean _stopResizeThread = false;
	private Thread _popThread = null;
	private Thread _resizeThread = null;
	
	public JGDPanelPop(GDDevice gdDevice, boolean autoPop, boolean autoResize, AbstractAction[] actions)
	throws RemoteException {
		this( gdDevice, autoPop, autoResize, actions, null,null);
	}

	public JGDPanelPop(GDDevice gdDevice, boolean autoPop, boolean autoResize, AbstractAction[] actions, ReentrantLock protectR, ConsoleLogger consoleLogger)
			throws RemoteException {
		_gdDevice = gdDevice;
		_protectR = protectR;
		_consoleLogger = consoleLogger;
		Dimension sz = null;
		try {
			sz = gdDevice.getSize();
		} catch (Exception e) {
		}

		if (sz != null)
			setSize(sz);

		_prefSize = getSize();
		_l = new Vector<GDObject>();
		_gs = new GDState();
		_gs.f = new Font(null, 0, 12);
		_gs.col = Color.black;
		_gs.fill = Color.white;
		_lastSize = getSize();
		setBackground(Color.white);
		setOpaque(true);
		_actions = actions;

		;
		
		this.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				mouseInside = true;
				if (trackMouse && bufferedImage != null && _autoResize) {
					updateRatios();
					repaint();
				}
			}

			public void mouseExited(MouseEvent e) {
				mouseInside = false;
				if (trackMouse && _autoResize) {
					realLocations = null;
					repaint();
				}
			}

			public void mousePressed(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseClicked(final MouseEvent e) {
				new Thread(new Runnable() {
					public void run() {
						if (_locatorActive) {
							try {
								_gdDevice.putLocation(new Point(e.getX(), e.getY()));
								if (_consoleLogger!=null) _consoleLogger.printAsOutput("Location saved\n");
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}).start();
			}

			public void mouseReleased(MouseEvent e) {
				checkPopup(e);
			}

			private void checkPopup(MouseEvent e) {
				if (e.isPopupTrigger() ) {
					JPopupMenu popupMenu = new JPopupMenu();
					if (_actions!=null) {
						for (int i = 0; i < _actions.length; ++i) {
							if (_actions[i]==null) popupMenu.addSeparator();
							else popupMenu.add(_actions[i]);
						}
					}
					
					popupMenu.show(JGDPanelPop.this, e.getX(), e.getY());
				}
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {

			}

			public void mouseMoved(MouseEvent e) {
				mouseLocation = e.getPoint();
				if (trackMouse) {
					repaint();
				}
			}
		});

		_autoPop = autoPop;
		_autoResize = autoResize;

		if (_autoPop) {
			_popThread=new Thread(new Runnable() {

				public void run() {

					while (true) {
						if (_stopPopThread) break;
						if (_autoPop)
							popNow();
						try {
							Thread.sleep(20);
						} catch (Exception e) {
						}
					}

				}
			});
			_popThread.start();
		}

		if (_autoResize) {
			
			_resizeThread=new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (_stopResizeThread) break;
						if (_autoResize) {
							if (_lastResizeTime != null && ((System.currentTimeMillis() - _lastResizeTime) > 50)) {

								if (_protectR!=null) _protectR.lock();								
								try {
									resizeNow();
								} finally {
									if (_protectR!=null) _protectR.unlock();
								}
																
								if (!_autoPop)
									popNow();
								_lastResizeTime = null;
							}
						}
						try {
							Thread.sleep(20);
						} catch (Exception e) {
						}
					}

				}
			});
			_resizeThread.start();
		}

	}

	public JGDPanelPop(GDDevice gdDevice) throws RemoteException {
		this(gdDevice, true, true, null,null,null);
	}
	
	private void updateRatios() {
		try {
			if (_protectR!=null) _protectR.lock();
			realLocations = _gdDevice.getRealPoints(new Point2D[] { new DoublePoint(0, 0), new DoublePoint(1, 1) });
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (_protectR!=null) _protectR.unlock();
		}		
	}

	public void setAutoModes(boolean autoPop, boolean autoResize) {
		_autoPop = autoPop;
		_autoResize = autoResize;
	}

	

	synchronized public void popNow() {

		try {
			Vector<GDObject> gdObjects = _gdDevice.popAllGraphicObjects();
			if (gdObjects != null && gdObjects.size() > 0) {

				synchronized (JGDPanelPop.this) {
					_l.addAll(gdObjects);

					Integer resetIdx = null;
					for (int i = _l.size() - 1; i >= 0; --i) {
						if (_l.elementAt(i) instanceof GDReset) {
							resetIdx = i;
							break;
						}
					}

					if (resetIdx != null) {

						Vector<GDObject> accurateEvents = new Vector<GDObject>();
						for (int i = resetIdx + 1; i < _l.size(); ++i) {
							GDObject gdObj = (GDObject) _l.elementAt(i);
							accurateEvents.add(gdObj);
						}
						_l = accurateEvents;
					}
				}

				if (getWidth() > 0 && getHeight() > 0) {
					bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics2D g2d = bufferedImage.createGraphics();
					p(g2d);
					g2d.dispose();
				} else {
					bufferedImage = null;
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						repaint();
					}
				});
			}
		} catch (RemoteException e) {

		}
	}

	synchronized public void resizeNow() {

		try {
			_gdDevice.fireSizeChangedEvent((int) getSize().getWidth(), (int) getSize().getHeight());
		} catch (Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					repaint();
				}
			});
		}
	}

	public synchronized Vector<GDObject> getGDOList() {
		return _l;
	}

	public Dimension getPreferredSize() {
		return new Dimension(_prefSize);
	}

	private void p(Graphics2D g) {
		if (_forceAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		g.setClip(0, 0, getWidth(), getHeight()); // reset clipping rect
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		while (i < j) {
			GDObject o = (GDObject) _l.elementAt(i++);
			if (o instanceof GDActionMarker) {

			} else {
				o.paint(this, _gs, g);
			}
		}
	}

	public synchronized void paintComponent(Graphics g) {

		Dimension d = getSize();

		if (!d.equals(_lastSize)) {
			trackMouse=false;			
			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;			
		}

		if (bufferedImage != null) {
			((Graphics2D) g).drawRenderedImage(bufferedImage, new AffineTransform());
		} else {
			((Graphics2D) g).setColor(Color.white);
			((Graphics2D) g).setBackground(Color.white);
			((Graphics2D) g).fillRect(0, 0, getWidth(), getHeight());
		}

		if (trackMouse && mouseInside && mouseLocation != null) {
			((Graphics2D) g).setColor(Color.red);
			((Graphics2D) g).drawLine(0, (int) mouseLocation.getY(), getWidth(), (int) mouseLocation.getY());
			((Graphics2D) g).drawLine((int) mouseLocation.getX(), 0, (int) mouseLocation.getX(), getHeight());
			if (realLocations != null) {
				double bx = realLocations[0].getX();
				double ax = realLocations[1].getX() - bx;
				double by = realLocations[0].getY();
				double ay = realLocations[1].getY() - by;

				((Graphics2D) g).setColor(Color.black);
				((Graphics2D) g).drawString("" + (ax * mouseLocation.getX() + bx), (int) mouseLocation.getX() + 16,
						(int) mouseLocation.getY() - 6);
				((Graphics2D) g).drawString("" + (ay * mouseLocation.getY() + by), (int) mouseLocation.getX() + 16,
						(int) mouseLocation.getY() + ((Graphics2D) g).getFontMetrics().getHeight() + 2);
			}
		}
	}

	public boolean isTrackMouse() {
		return trackMouse;
	}

	public void setTrackMouse(boolean tm) {
		trackMouse = tm;
		repaint();
	}

	public GDDevice getGdDevice() {
		return _gdDevice;
	}
	
	public void dispose() {
		_stopPopThread=true;
		_stopResizeThread=true;
		try {
			_popThread.join();
			System.out.println("pop thread ended");
			_resizeThread.join();
			System.out.println("resize thread ended");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			_gdDevice.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isLocatorActive() {
		return _locatorActive;
	}

	public void setLocatorActive(boolean active) {
		_locatorActive = active;
	}
}
