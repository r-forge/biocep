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

import graphics.rmi.PDFPanel;
import graphics.rmi.RGui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import views.highlighting.HighlightDocument;
import views.highlighting.NonWrappingTextPane;

import com.sun.pdfview.PagePanel;

public class PdfView extends DynamicView {
	private RGui _rgui;
	private JTextPane _area;
	private JScrollPane _scrollPane;
	private JScrollPane _pdfCanvasScrollPane;
	private PDFPanel _pdfCanvas = new PDFPanel();

	private double _ratioX = 1;
	private double _ratioY = 1;

	private JTextField ratioX;
	private JTextField ratioY;
	private JLabel ratioYLabel;
	private JCheckBox coupled;
	private JButton refreshButton;

	JPanel bottompanel;
	JPanel controlPanel;

	private void showPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(new AbstractAction("Fit To Panel") {
			public void actionPerformed(ActionEvent e) {
				ratioX.setText("1");
				if (!coupled.isSelected()) ratioX.setText("1");
				resetPdfCanvasSize();
			}

			public boolean isEnabled() {
				return true;
			}
		});

		popupMenu.add(new AbstractAction("Get Pdf From Current Device") {
			public void actionPerformed(ActionEvent e) {
				try {
					byte[] buffer = _rgui.getCurrentDevice().getPdf();
					_pdfCanvas.setPDFContent(buffer);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		popupMenu.addSeparator();

		popupMenu.add(new AbstractAction("Load Pdf From File") {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(_pdfCanvas);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					_pdfCanvas.setPDFContent(chooser.getSelectedFile());
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		popupMenu.add(new AbstractAction("Save Pdf To File") {
			public void actionPerformed(ActionEvent e) {

				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new Thread(new Runnable() {
						public void run() {
							try {
								_pdfCanvas.savePDFContent(chooser.getSelectedFile());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		popupMenu.show(_pdfCanvas, e.getX(), e.getY());

	}

	private void showAreaPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new AbstractAction("Highlight") {
			public void actionPerformed(ActionEvent e) {
				final HighlightDocument document = ((HighlightDocument) _area.getDocument());
				new Thread(new Runnable() {
					public void run() {
						try {

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									_area.setEnabled(false);
								}
							});

							document.processChangedLines(0, document.getLength());
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									_area.setEnabled(true);
								}
							});
						}

					}
				}).start();
			}

			public boolean isEnabled() {
				return true;
			}
		});
		popupMenu.show(_area, e.getX(), e.getY());
	}

	public PdfView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);

		_rgui = rgui;
		_area = new NonWrappingTextPane();
		_scrollPane = new JScrollPane(_area);
		_area.setDocument(new HighlightDocument(false));
		_area.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showAreaPopup(e);
				}

				if (e.getButton() == MouseEvent.BUTTON3) {
					showAreaPopup(e);
				}

			};

			public void mouseExited(MouseEvent e) {
			};

			public void mousePressed(MouseEvent e) {
			};

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showAreaPopup(e);
				}
			};
		});

		_pdfCanvas.addMouseListener(new MouseListener() {
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

								byte[] result = null;
								try {
									result = _rgui.getR().getPdf(_area.getText(), bottompanel.getWidth(), bottompanel.getHeight());
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

								
								
								final double xPos=(double)_pdfCanvasScrollPane.getHorizontalScrollBar().getValue()/(double)_pdfCanvasScrollPane.getHorizontalScrollBar().getMaximum();
								final double yPos=(double)_pdfCanvasScrollPane.getVerticalScrollBar().getValue()/(double)_pdfCanvasScrollPane.getVerticalScrollBar().getMaximum();
								
								resetPdfCanvasSize();
								_pdfCanvas.setPDFContent(result);
								
								new Thread(new Runnable(){
									public void run() {
										SwingUtilities.invokeLater(new Runnable(){
											public void run() {
												_pdfCanvasScrollPane.getHorizontalScrollBar().setValue((int)(xPos*_pdfCanvasScrollPane.getHorizontalScrollBar().getMaximum()));
												_pdfCanvasScrollPane.getVerticalScrollBar().setValue((int)(yPos*_pdfCanvasScrollPane.getVerticalScrollBar().getMaximum()));
												_pdfCanvasScrollPane.revalidate();
											}
										});
										
										
									}
								}).start();
								
								
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

		_pdfCanvasScrollPane = new JScrollPane(_pdfCanvas);
		bottompanel = new JPanel(new BorderLayout());
		bottompanel.setBackground(Color.white);
		bottompanel.add(_pdfCanvasScrollPane, BorderLayout.CENTER);

		controlPanel = new JPanel(new BorderLayout());
		controlPanel.setLayout(new GridLayout(1, 2));
		controlPanel.setBackground(Color.white);
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		controlPanel.add(p1);
		controlPanel.add(p2);

		ratioX = new JTextField(new Double(_ratioX).toString());
		ratioY = new JTextField(new Double(_ratioY).toString());
		ratioYLabel = new JLabel("Y Ratio");
		coupled = new JCheckBox("Coupled");
		coupled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (coupled.isSelected()) {
					ratioY.setText("");
					ratioY.setEnabled(false);
					ratioYLabel.setEnabled(false);
				} else {
					_ratioY=_ratioX;					
					ratioY.setText(new Double(_ratioY).toString());
					ratioY.setEnabled(true);
					ratioYLabel.setEnabled(true);
				}				
			}
		});
		
		coupled.setSelected(true);
		
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final double xPos=(double)_pdfCanvasScrollPane.getHorizontalScrollBar().getValue()/(double)_pdfCanvasScrollPane.getHorizontalScrollBar().getMaximum();
				final double yPos=(double)_pdfCanvasScrollPane.getVerticalScrollBar().getValue()/(double)_pdfCanvasScrollPane.getVerticalScrollBar().getMaximum();
				
				resetPdfCanvasSize();
				
				new Thread(new Runnable(){
					public void run() {
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {
								_pdfCanvasScrollPane.getHorizontalScrollBar().setValue((int)(xPos*_pdfCanvasScrollPane.getHorizontalScrollBar().getMaximum()));
								_pdfCanvasScrollPane.getVerticalScrollBar().setValue((int)(yPos*_pdfCanvasScrollPane.getVerticalScrollBar().getMaximum()));
								_pdfCanvasScrollPane.revalidate();
							}
						});
						
						
					}
				}).start();


			}
		});

		p1.add(new JLabel("Zoom Ratio")); p2.add(ratioX);
		//p1.add(ratioYLabel); p2.add(ratioY);
		//p1.add(coupled);p2.add(new JLabel(""));
		p1.add(new JLabel(""));p2.add(new JLabel(""));
		p1.add(new JLabel(""));	p2.add(refreshButton);

		JPanel leftWrapper = new JPanel(new BorderLayout());
		leftWrapper.add(controlPanel, BorderLayout.NORTH);

		JPanel pb = new JPanel(new BorderLayout());
		pb.add(leftWrapper, BorderLayout.WEST);
		pb.add(bottompanel, BorderLayout.CENTER);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, pb);
		splitPane.setDividerSize(3);

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).setBorder(BorderFactory.createLineBorder(Color.gray, 2));
		((JPanel) getComponent()).add(splitPane);

		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						splitPane.setDividerLocation((int) 60);
						_pdfCanvas.setPreferredSize(new Dimension(bottompanel.getWidth() - 10, bottompanel.getHeight() - 10));
						_pdfCanvas.revalidate();
					}
				});
			}
		}).start();

	}

	public JTextPane getArea() {
		return _area;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	private void resetPdfCanvasSize() {
		try {
			_ratioX = new Double(ratioX.getText());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (coupled.isSelected()) {
			_ratioY = _ratioX;
		} else {
			try {
				_ratioY = new Double(ratioY.getText());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		_pdfCanvas.setPreferredSize(new Dimension((int) (bottompanel.getWidth() * _ratioX) - 10, (int) (bottompanel.getHeight() * _ratioY) - 10));
		_pdfCanvas.revalidate();
	}

	PagePanel getSvgCanvas() {
		return _pdfCanvas;
	}
}