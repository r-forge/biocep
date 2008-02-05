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
import java.awt.event.ActionEvent;
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
	
	private AbstractAction[] _actions;
	private boolean mouseInside;
	private Point2D mouseLocation = null;
	private Point2D[] realLocations = null;
	private ReentrantLock _protectR = null;
	private ConsoleLogger _consoleLogger = null;
	
	private boolean _stopPopThread = false;	
	private boolean _stopResizeThread = false;
	private Thread _popThread = null;
	private Thread _resizeThread = null;
	
	private double w=Double.NaN;
	private double h=Double.NaN;	
	private double x0=Double.NaN;
	private double y0=Double.NaN;
	private double fx=Double.NaN;
	private double fy=Double.NaN;
	
	private double z=2;
	
	public static final int INTERACTOR_NULL=0;
	
	public static final int INTERACTOR_ZOOM_IN=1;
	public static final int INTERACTOR_ZOOM_OUT=2;
	
	public static final int INTERACTOR_ZOOM_IN_X=3;
	public static final int INTERACTOR_ZOOM_OUT_X=4;
	
	public static final int INTERACTOR_ZOOM_IN_Y=5;
	public static final int INTERACTOR_ZOOM_OUT_Y=6;
	
	public static final int INTERACTOR_ZOOM_IN_SELECT=7;	
	public static final int INTERACTOR_ZOOM_OUT_SELECT=8;
	
	public static final int INTERACTOR_ZOOM_IN_X_SELECT=9;
	public static final int INTERACTOR_ZOOM_OUT_X__SELECT=10;
	
	public static final int INTERACTOR_ZOOM_IN_Y_SELECT=11;
	public static final int INTERACTOR_ZOOM_OUT_Y__SELECT=12;

	public static final int INTERACTOR_TRACKER=13;
	
	private int _interactor=INTERACTOR_ZOOM_IN_SELECT;
	private Point _mouseStartPosition=null;
	
	
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

		
		x0=getWidth()/2;
		y0=getHeight()/2;
		fx=1;
		fy=1;
		w=getWidth();
		h=getHeight();
		
		
		
		this.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				mouseInside = true;
				if (_interactor==INTERACTOR_TRACKER && bufferedImage != null && _autoResize) {
					updateRatios();
					repaint();
				}
			}

			public void mouseExited(MouseEvent e) {
				mouseInside = false;
				if (_interactor==INTERACTOR_TRACKER && _autoResize) {
					realLocations = null;
					repaint();
				} if (_interactor==INTERACTOR_ZOOM_IN_SELECT) {
					repaint();
				}
				
			}

			public void mousePressed(MouseEvent e) {
				System.out.println("mouse pressed");
				
				if (_interactor==INTERACTOR_NULL) {checkPopup(e);}
				else if (e.getButton()==MouseEvent.BUTTON1) 
					if (_interactor==INTERACTOR_ZOOM_IN_SELECT) {
					_mouseStartPosition=e.getPoint();
				}
				
				
			}

			public void mouseClicked(final MouseEvent e) {
				if (_interactor==INTERACTOR_TRACKER) {
					new Thread(new Runnable() {
						public void run() {						
								try {
									_gdDevice.putLocation(new Point(e.getX(), e.getY()));
									if (_consoleLogger!=null) _consoleLogger.printAsOutput("Location saved\n");
								} catch (Exception ex) {
									ex.printStackTrace();
								}						
						}
					}).start();
				}
			}

			public void mouseReleased(MouseEvent e) {
				System.out.println("mouse released");
				if (_interactor==INTERACTOR_NULL) checkPopup(e); 
				else if (e.getButton()==MouseEvent.BUTTON1)  {
					if (_interactor==INTERACTOR_ZOOM_IN_SELECT && _mouseStartPosition!=null) {
				
					double x1=	(_mouseStartPosition.getX()+e.getPoint().getX())/2;
					double y1=	(_mouseStartPosition.getY()+e.getPoint().getY())/2;
						
					x0=x0-(w/2-x1);
					y0=y0-(h/2-y1);
					
					double zx=w/Math.abs(_mouseStartPosition.getX()-e.getPoint().getX());
					double zy=h/Math.abs(_mouseStartPosition.getY()-e.getPoint().getY());					
					fx=fx*zx;		
					x0=x0*zx;
					fy=fy*zy;
					y0=y0*zy;					
					_mouseStartPosition=null;
					resizeNow();
					repaint();
					
					}
				}
			}

			private void checkPopup(MouseEvent e) {
				if (e.isPopupTrigger() ) {
					JPopupMenu popupMenu = new JPopupMenu();
					
					popupMenu.add(new AbstractAction("Zoom X"){
						@Override
						public void actionPerformed(ActionEvent e) {														
							fx=fx*z;		
							x0=x0*z;
							resizeNow();
							repaint();
						}
					});
					popupMenu.add(new AbstractAction("Unzoom X"){
						@Override
						public void actionPerformed(ActionEvent e) {
							fx=fx/z;							
							x0=x0/z;
							
							if (fx<1) {
								fx=1;
								x0=w/2;
							}
							resizeNow();
							repaint();
						}
					});
					popupMenu.add(new AbstractAction("Zoom Y"){
						@Override
						public void actionPerformed(ActionEvent e) {
							fy=fy*z;
							y0=y0*z;
							resizeNow();
							repaint();
						}
					});
					popupMenu.add(new AbstractAction("Unzoom Y"){
						@Override
						public void actionPerformed(ActionEvent e) {
							fy=fy/z;
							y0=y0/z;
							
							if (fy<1) {
								fy=1;
								y0=h/2;
							}
							
							resizeNow();
							repaint();
						}
					});
					
					popupMenu.add(new AbstractAction("Standard Size"){
						@Override
						public void actionPerformed(ActionEvent e) {
							x0=getWidth()/2;
							y0=getHeight()/2;
							fx=1;
							fy=1;
							resizeNow();
							repaint();
						}
					});
					
					popupMenu.add(new AbstractAction("Scroll X Left"){
						@Override
						public void actionPerformed(ActionEvent e) {														
							double deltax=w*(fx-1)/10;	
							x0=x0-deltax;
							if (x0 < w/2) {
								x0=w/2;
							}
							repaint();
						}
					});
					
					popupMenu.add(new AbstractAction("Scroll X Right"){
						@Override
						public void actionPerformed(ActionEvent e) {		
							double deltax=w*(fx-1)/10;	
							x0=x0+deltax;
							if (x0 > (w*fx)-w/2) {
								x0=(w*fx)-w/2;
							}
							repaint();
						}
					});
					
					popupMenu.add(new AbstractAction("Scroll Y Up"){
						@Override
						public void actionPerformed(ActionEvent e) {														
							double deltay=h*(fy-1)/10;	
							y0=y0-deltay;
							if (y0 < h/2) {
								y0=h/2;
							}
							repaint();
						}
					});
					
					popupMenu.add(new AbstractAction("Scroll Y Down"){
						@Override
						public void actionPerformed(ActionEvent e) {		
							double deltay=h*(fy-1)/10;	
							y0=y0+deltay;
							if (y0 > (h*fy)-h/2) {
								y0=(h*fy)-h/2;
							}
							repaint();
						}
					});

					
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
				mouseLocation = e.getPoint();
				System.out.println(_mouseStartPosition);
				if ((_interactor==INTERACTOR_ZOOM_IN_SELECT && _mouseStartPosition!=null) ) {
					repaint();
				}
			}

			
			public void mouseMoved(MouseEvent e) {
				mouseLocation = e.getPoint();
				if (_interactor==INTERACTOR_TRACKER ) {
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
					bufferedImage = new BufferedImage((int)(getWidth()*fx), (int)(getHeight()*fy), BufferedImage.TYPE_INT_RGB);
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
			
			
			if (getWidth()!=w) {
				double px=getWidth()/w;
				x0=x0*px;
			}
			
			if (getHeight()!=h) {
				double py=getHeight()/h;
				y0=y0*py;
			}
			
			
			_gdDevice.fireSizeChangedEvent((int)(getWidth()*fx), (int)(getHeight()*fy));
			
			w=getWidth();
			h=getHeight();
			
			
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
		g.setClip(0, 0, (int)(getWidth()*fx), (int)(getHeight()*fy)); // reset clipping rect
		g.setColor(Color.white);
		g.fillRect(0, 0, (int)(getWidth()*fx), (int)(getHeight()*fy));
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
			_interactor=INTERACTOR_NULL;			
			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;			
		}

		if (bufferedImage != null) {	
			((Graphics2D) g).drawRenderedImage(bufferedImage, new AffineTransform(1,0,0,1,-(x0-getWidth()/2),-(y0-getHeight()/2)));
		} else {
			((Graphics2D) g).setColor(Color.white);
			((Graphics2D) g).setBackground(Color.white);
			((Graphics2D) g).fillRect(0, 0, getWidth(), getHeight());
		}

		if (mouseInside && mouseLocation != null) {
			if (_interactor==INTERACTOR_TRACKER ) {
				((Graphics2D) g).setColor(Color.red);
				((Graphics2D) g).drawLine(0, (int) mouseLocation.getY(), getWidth(), (int) mouseLocation.getY());
				((Graphics2D) g).drawLine((int) mouseLocation.getX(), 0, (int) mouseLocation.getX(), getHeight());
				if (realLocations != null) {
					double bx = realLocations[0].getX();
					double ax = realLocations[1].getX() - bx;
					double by = realLocations[0].getY();
					double ay = realLocations[1].getY() - by;
	
					((Graphics2D) g).setColor(Color.black);
					((Graphics2D) g).drawString("" + (ax * (mouseLocation.getX()-w/2+x0) + bx), (int) mouseLocation.getX() + 16,
							(int) mouseLocation.getY() - 6);
					((Graphics2D) g).drawString("" + (ay * (mouseLocation.getY()-h/2+y0) + by), (int) mouseLocation.getX() + 16,
							(int) mouseLocation.getY() + ((Graphics2D) g).getFontMetrics().getHeight() + 2);
				}
			} else if (_mouseStartPosition!=null) {
				if (_interactor==INTERACTOR_ZOOM_IN_SELECT) {
					((Graphics2D) g).setColor(Color.black);					
					int x1=(int)Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					int y1=(int)Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					int w1=(int)Math.abs(_mouseStartPosition.getX()-mouseLocation.getX());
					int h1=(int)Math.abs(_mouseStartPosition.getY()-mouseLocation.getY());					
					((Graphics2D) g).drawRect(x1,y1,w1,h1);
				}
			}
		}
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
	
	public int getInteractor() {
		return _interactor;
	}
	public void setInteractor(int interactor) {
		_interactor=interactor;
		_mouseStartPosition=null;
		repaint();
	}

}
