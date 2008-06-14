package views;

import graphics.rmi.RGui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;

public class SvgView extends DynamicView {
	private RGui _rgui;
	private JTextArea _area;
	private JScrollPane _scrollPane;
	private JSVGCanvas _svgCanvas = new JSVGCanvas();
	private File tempFile;

	private void showPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(new AbstractAction("Get Svg From Current Device") {
			public void actionPerformed(ActionEvent e) {

				try {
					Vector<String> svgbuffer = _rgui.getCurrentDevice().getSVG();
					tempFile = new File(System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg");
					
					PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
					for (int i = 0; i < svgbuffer.size(); ++i)
					pw.println(svgbuffer.elementAt(i));
					pw.close();

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								_svgCanvas.setURI(tempFile.toURL().toString());
								repaint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});

		popupMenu.addSeparator();

		popupMenu.add(new AbstractAction("Load Svg From File") {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(_svgCanvas);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								tempFile=chooser.getSelectedFile();
								_svgCanvas.setURI(tempFile.toURL().toString());
								repaint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		popupMenu.add(new AbstractAction("Save Svg To File") {
			public void actionPerformed(ActionEvent e) {

				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new Thread(new Runnable() {
						public void run() {
								PrintWriter pw=null;BufferedReader br=null;
								try {
									pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()));
									br= new BufferedReader(new FileReader(tempFile));
									String l;
									while ((l=br.readLine())!=null) {
										pw.println(l);
									}
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									try { br.close(); pw.close(); } catch (Exception e) {}
								}
						}
					}).start();
				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		popupMenu.show(_svgCanvas, e.getX(), e.getY());

	}

	public SvgView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);

		_rgui = rgui;
		_area = new JTextArea();
		_scrollPane = new JScrollPane(_area);
		_svgCanvas = new JSVGCanvas();
		_svgCanvas.setEnableZoomInteractor(true);

		_svgCanvas.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}

				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e);
				}

			};

			public void mouseExited(MouseEvent e) {
			};

			public void mousePressed(MouseEvent e) {
			};

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			};
		});

		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						if (_rgui.getRLock().isLocked()) {
							JOptionPane.showMessageDialog(null, "R is busy");
						} else {
							_rgui.getRLock().lock();
							try {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										_area.setEnabled(false);
									}
								});

								Vector<String> result = null;
								try {
									result = _rgui.getR().getSvg(_area.getText(), _svgCanvas.getWidth(), _svgCanvas.getHeight());
								} catch (RemoteException e) {
									JOptionPane.showMessageDialog(null, e.getCause().getMessage(), "R Error", JOptionPane.ERROR_MESSAGE);
									return;
								}

								if (result == null) {
									JOptionPane.showMessageDialog(null, _rgui.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								if (!"".equals(_rgui.getR().getStatus())) {
									JOptionPane.showMessageDialog(null, _rgui.getR().getStatus(), "R Info", JOptionPane.INFORMATION_MESSAGE);
								}
								// System.out.println("SVG RESULT:"+result);
								final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
								PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
								for (int i = 0; i < result.size(); ++i)
									pw.println(result.elementAt(i));
								pw.close();

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										try {
											_svgCanvas.setURI(new File(tempFile).toURL().toString());
											repaint();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								_rgui.getRLock().unlock();
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										_area.setEnabled(true);
									}
								});
							}
						}
					}
				}).start();
			}
		});

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel topwest = new JPanel(new BorderLayout());
		JPanel topcenter = new JPanel(new BorderLayout());
		JPanel topeast = new JPanel(new BorderLayout());
		topPanel.add(topwest, BorderLayout.WEST);
		topPanel.add(topcenter, BorderLayout.CENTER);
		topPanel.add(topeast, BorderLayout.EAST);

		topwest.add(new JLabel("Script:  "), BorderLayout.NORTH);
		topcenter.add(_scrollPane, BorderLayout.CENTER);
		topeast.add(submit, BorderLayout.NORTH);

		topPanel.setBorder(BorderFactory.createLineBorder(Color.white, 3));
		topwest.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		topwest.setBackground(Color.white);
		topcenter.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		topeast.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		topeast.setBackground(Color.white);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, _svgCanvas);
		splitPane.setDividerSize(3);

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).setBorder(BorderFactory.createLineBorder(Color.gray, 2));
		((JPanel) getComponent()).add(splitPane);

		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						splitPane.setDividerLocation((int) 60);
					}
				});
			}
		}).start();

	}

	public JTextArea getArea() {
		return _area;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	JSVGCanvas getSvgCanvas() {
		return _svgCanvas;
	}
}