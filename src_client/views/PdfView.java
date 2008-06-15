package views;

import graphics.rmi.PDFPanel;
import graphics.rmi.RGui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
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
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import views.highlighting.HighlightDocument;
import views.highlighting.NonWrappingTextPane;

import com.sun.pdfview.PagePanel;

public class PdfView extends DynamicView {
	private RGui _rgui;
	private JTextPane _area;
	private JScrollPane _scrollPane;
	private PDFPanel _svgCanvas = new PDFPanel();
	JPanel bottompanel;

	private void showPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new AbstractAction("Get Pdf From Current Device") {
			public void actionPerformed(ActionEvent e) {
				try {
					byte[] buffer = _rgui.getCurrentDevice().getPdf();
					_svgCanvas.setPDFContent(buffer);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		popupMenu.addSeparator();

		popupMenu.add(new AbstractAction("Load Pdf From File") {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(_svgCanvas);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					_svgCanvas.setPDFContent(chooser.getSelectedFile());
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
								_svgCanvas.savePDFContent(chooser.getSelectedFile());
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

		popupMenu.show(_svgCanvas, e.getX(), e.getY());

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

								_svgCanvas.setPDFContent(result);
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

		bottompanel = new JPanel(new BorderLayout());
		bottompanel.setBackground(Color.white);
		bottompanel.add(_svgCanvas, BorderLayout.CENTER);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottompanel);
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

	public JTextPane getArea() {
		return _area;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	PagePanel getSvgCanvas() {
		return _svgCanvas;
	}
}