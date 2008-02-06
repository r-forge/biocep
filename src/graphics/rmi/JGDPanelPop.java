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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
	final private static double fx_MAX=8;
	final private static double fy_MAX=8;	
	private double w=Double.NaN;
	private double h=Double.NaN;	
	private double x0=Double.NaN;
	private double y0=Double.NaN;
	private double fx=Double.NaN;
	private double fy=Double.NaN;	
	private double zoomPower=1.5;
	
	public static final int INTERACTOR_NULL=0;	
	public static final int INTERACTOR_ZOOM_IN_OUT=1;	
	public static final int INTERACTOR_ZOOM_IN_OUT_X=2;	
	public static final int INTERACTOR_ZOOM_IN_OUT_Y=3;	
	public static final int INTERACTOR_ZOOM_IN_OUT_SELECT=4;
	public static final int INTERACTOR_ZOOM_IN_OUT_X_SELECT=5;	
	public static final int INTERACTOR_ZOOM_IN_OUT_Y_SELECT=6;	
	public static final int INTERACTOR_SCROLL_LEFT_RIGHT=7;
	public static final int INTERACTOR_SCROLL_UP_DOWN=8;
	public static final int INTERACTOR_SCROLL=9;
	public static final int INTERACTOR_TRACKER=10;
	
	private int _interactor=INTERACTOR_NULL;
	private Point _mouseStartPosition=null;
	private double _x0Start;
	private double _y0Start;
	
	private Cursor _zoomCursor=null;
	
	
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

		if (sz != null) setSize(sz);

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
				
		try {
			_zoomCursor=Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(GDApplet.class.getResource("/graphics/rmi/icons/" + "zoom.png")),
					 new Point(10,10),"zoom");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				mouseInside = true;
				if (_interactor==INTERACTOR_TRACKER && bufferedImage != null && _autoResize) {
					updateRatios();
					repaint();
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT) {
					//setCursor(_zoomCursor);
				}
			}

			public void mouseExited(MouseEvent e) {
				mouseInside = false;
				if (_interactor==INTERACTOR_TRACKER && _autoResize) {
					realLocations = null;
					repaint();
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT ) {					
					repaint();
				} 
			}

			public void mousePressed(MouseEvent e) {
				
				if (_interactor==INTERACTOR_NULL) {checkPopup(e);}
				
				else if (e.getButton()==MouseEvent.BUTTON1) { 
					if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT ) {
						_mouseStartPosition=e.getPoint();
					} else if (_interactor==INTERACTOR_SCROLL) {
						_mouseStartPosition=e.getPoint();
						_x0Start=x0;
						_y0Start=y0;
					}
				} else if (e.getButton()==MouseEvent.BUTTON3) { 
					if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
						_mouseStartPosition=e.getPoint();
					}
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
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT) {
					if (e.getButton()==MouseEvent.BUTTON1) {
						
						if (fx==fx_MAX && fy==fy_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double w1=w/zoomPower;
							double h1=h/zoomPower;
							selectZoomX(e.getX()-w1/2,e.getX()+w1/2);							
							selectZoomY(e.getY()-h1/2,e.getY()+h1/2);							
							resizeNow();							
						}
						
					} else if (e.getButton()==MouseEvent.BUTTON3) {
						if (fx==1 && fy==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double w1=w/zoomPower;
							double h1=h/zoomPower;
							selectUnzoomX(e.getX()-w1/2,e.getX()+w1/2);							
							selectUnzoomY(e.getY()-h1/2,e.getY()+h1/2);							
							resizeNow();
						}
					}					
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_X) {
					
					if (e.getButton()==MouseEvent.BUTTON1) {
						if (fx==fx_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double w1=w/zoomPower;
							selectZoomX(e.getX()-w1/2,e.getX()+w1/2);
							resizeNow();
						}
					} else if (e.getButton()==MouseEvent.BUTTON3) {
						if (fx==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double w1=w/zoomPower;
							selectUnzoomX(e.getX()-w1/2,e.getX()+w1/2);
							resizeNow();
						}
					}		
					
				}else if (_interactor==INTERACTOR_ZOOM_IN_OUT_Y) {
					if (e.getButton()==MouseEvent.BUTTON1) {
						if (fy==fy_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double h1=h/zoomPower;
							selectZoomY(e.getY()-h1/2,e.getY()+h1/2);
							resizeNow();
						}
					} else if (e.getButton()==MouseEvent.BUTTON3) {
						if (fy==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							double h1=h/zoomPower;
							selectUnzoomY(e.getY()-h1/2,e.getY()+h1/2);
							resizeNow();
						}
					}					
				} else if (_interactor==INTERACTOR_SCROLL_LEFT_RIGHT) {
					if (e.getButton()==MouseEvent.BUTTON1) {
						if (x0==w/2) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							scrollXLeft();	
						}						
					} else if (e.getButton()==MouseEvent.BUTTON3) {
						if (x0==((w*fx)-w/2)) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							scrollXRight();
						}
					}					
				} else if (_interactor==INTERACTOR_SCROLL_UP_DOWN) {
					if (e.getButton()==MouseEvent.BUTTON1) {
						if (y0==h/2) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							scrollYUp();
						}
					} else if (e.getButton()==MouseEvent.BUTTON3) {
						if ( y0==((h*fy)-h/2)) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							scrollYDown();
						}
					}					
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (_interactor==INTERACTOR_NULL) checkPopup(e); 
				else if (e.getButton()==MouseEvent.BUTTON1 && _mouseStartPosition!=null)  {
					
					
					
					if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT ) {

						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();

						if (fx==fx_MAX && fy==fy_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectZoomX(startPosition.getX(), e.getX());
							selectZoomY(startPosition.getY(), e.getY());						
							resizeNow();							
						}
					
					} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT ) {

						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();

						if (fx==fx_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectZoomX(startPosition.getX(), e.getX());
							resizeNow();
						}					
					} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();
						if (fy==fy_MAX) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectZoomY(startPosition.getY(), e.getY());
							resizeNow();
						}					
					}
					
					
				} else if (e.getButton()==MouseEvent.BUTTON3 && _mouseStartPosition!=null)  {
					if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT ) {
						
						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();

						if (fx==1 && fy==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectUnzoomX(startPosition.getX(), e.getX());
							selectUnzoomY(startPosition.getY(), e.getY());																
							resizeNow();
						}

					} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT ) {
						
						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();

						if (fx==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectUnzoomX(startPosition.getX(), e.getX());								
							resizeNow();
						}

					} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT ) {
						
						Point startPosition=_mouseStartPosition;
						_mouseStartPosition=null;
						repaint();

						if (fy==1) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							selectUnzoomY(startPosition.getY(), e.getY());								
							resizeNow();
						}

					}
					
				}
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
				mouseLocation = e.getPoint();
				if (((_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT || _interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT) && _mouseStartPosition!=null) ) {
					repaint();
				}
				if (_interactor==INTERACTOR_SCROLL && _mouseStartPosition!=null && mouseInside) {
					x0=_x0Start+(_mouseStartPosition.getX()-e.getX());
					if (x0<w/2) x0=w/2;
					if (x0>((w*fx)-w/2)) x0=((w*fx)-w/2);
					
					y0=_y0Start+(_mouseStartPosition.getY()-e.getY());
					if (y0<h/2) y0=h/2;
					if (y0>((h*fy)-h/2)) y0=((h*fy)-h/2);
					
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
	
	private void selectZoomX(double startX,double endX) {
		double zx=w/Math.abs(startX-endX);
		double x1=(startX+endX)/2;
		if (fx*zx>fx_MAX) {
			zx=fx_MAX/fx;
			fx=fx_MAX;
		} else {
			fx=fx*zx;
		}
		
		x0=x0-(w/2-x1);
		x0=x0*zx;
		if (x0<w/2) x0=w/2;
		if (x0>((w*fx)-w/2)) x0=((w*fx)-w/2);
	}
	
	private void selectZoomY(double startY,double endY) {
		double zy=h/Math.abs(startY-endY);
		double y1=	(startY+endY)/2;
		if (fy*zy>fy_MAX) {
			zy=fy_MAX/fy;
			fy=fy_MAX;
		} else {
			fy=fy*zy;	
		}							
		y0=y0-(h/2-y1);
		y0=y0*zy;
		if (y0<h/2) y0=h/2;
		if (y0>((h*fy)-h/2)) y0=((h*fy)-h/2);
	}
	
	private void selectUnzoomX(double startX,double endX) {
		double zx=w/Math.abs(startX-endX);
		double x1=(startX+endX)/2;
		if (fx/zx<1) {
			fx=1;
			x0=w/2;
		} else {
			//x0=x0-(w/2-x1);
			fx=fx/zx;
			x0=x0/zx;
		}
		if (x0<w/2) x0=w/2;
		if (x0>((w*fx)-w/2)) x0=((w*fx)-w/2);							
	}
	
	
	private void selectUnzoomY(double startY,double endY) {
		double zy=h/Math.abs(startY-endY);
		double y1=	(startY+endY)/2;
		if (fy/zy<1) {
			fy=1;
			y0=h/2;
		} else {
			//y0=y0-(h/2-y1);							
			fy=fy/zy;
			y0=y0/zy;
		}
		
		if (y0<h/2) y0=h/2;
		if (y0>((h*fy)-h/2)) y0=((h*fy)-h/2);
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
			
			if (x0<w/2) x0=w/2;
			if (x0>((w*fx)-w/2)) x0=((w*fx)-w/2);	
			
			if (y0<h/2) y0=h/2;
			if (y0>((h*fy)-h/2)) y0=((h*fy)-h/2);
			
			
		} catch (Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					repaint();
				}
			});
		}
	}

		
	public void standardSize() {		
			x0=getWidth()/2;
			y0=getHeight()/2;
			fx=1;
			fy=1;
			resizeNow();
			repaint();
		}
	
	public void scrollXLeft(){
																
			double deltax=w*(fx-1)/10;	
			x0=x0-deltax;
			if (x0 < w/2) {
				x0=w/2;
			}
			repaint();
		}
	
	public void scrollXRight(){
			double deltax=w*(fx-1)/10;	
			x0=x0+deltax;
			if (x0 > (w*fx)-w/2) {
				x0=(w*fx)-w/2;
			}
			repaint();
		}
	
	public void scrollYUp(){
																
			double deltay=h*(fy-1)/10;	
			y0=y0-deltay;
			if (y0 < h/2) {
				y0=h/2;
			}
			repaint();
		}
	
	public void scrollYDown(){
			double deltay=h*(fy-1)/10;	
			y0=y0+deltay;
			if (y0 > (h*fy)-h/2) {
				y0=(h*fy)-h/2;
			}
			repaint();
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


	Color _transparentGray=new Color(Color.black.getColorSpace(),new float[]{Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue()},(float)0.2);
	
	public synchronized void paintComponent(Graphics g) {

		Dimension d = getSize();

		if (!d.equals(_lastSize)) {
			if (_interactor==INTERACTOR_TRACKER) _interactor=INTERACTOR_NULL;			
			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;			
		}

		if (bufferedImage != null) {
			((Graphics2D) g).setColor(Color.white);
			((Graphics2D) g).setBackground(Color.white);
			((Graphics2D) g).fillRect(0, 0, getWidth(), getHeight());

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
				if (_interactor==INTERACTOR_ZOOM_IN_OUT_SELECT) {
					int x1=(int)Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					int y1=(int)Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					int w1=(int)Math.abs(_mouseStartPosition.getX()-mouseLocation.getX());
					int h1=(int)Math.abs(_mouseStartPosition.getY()-mouseLocation.getY());
					((Graphics2D) g).setColor(Color.black);
					((Graphics2D) g).setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,0,new float[]{4,4}, 20));
					((Graphics2D) g).drawRect(x1,y1,w1,h1);
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_X_SELECT) {
					int x1=(int)Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					int y1=-1;
					int w1=(int)Math.abs(_mouseStartPosition.getX()-mouseLocation.getX());
					int h1=(int)h+1;
					((Graphics2D) g).setColor(_transparentGray);
					((Graphics2D) g).setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,0,new float[]{4,4}, 20));
					((Graphics2D) g).fillRect(x1,y1,w1,h1);
					((Graphics2D) g).drawRect(x1,y1,w1,h1);
					
				} else if (_interactor==INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
					int x1=-1;
					int y1=(int)Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					int w1=(int)w+1;
					int h1=(int)Math.abs(_mouseStartPosition.getY()-mouseLocation.getY());
					((Graphics2D) g).setColor(_transparentGray);
					((Graphics2D) g).setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,0,new float[]{4,4}, 20));
					((Graphics2D) g).fillRect(x1,y1,w1,h1);
					((Graphics2D) g).drawRect(x1,y1,w1,h1);
				} 
			}
		}
		
		/*
		if (fx!=1 || fy!=1) {
			
			
		}
		
		*/
		
	}

	public GDDevice getGdDevice() {
		return _gdDevice;
	}
	
	public void dispose() {
		_stopPopThread=true;
		_stopResizeThread=true;
		try {
			_popThread.join();
			_resizeThread.join();
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
