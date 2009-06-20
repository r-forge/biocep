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
package org.kchine.r.workbench.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import net.infonode.docking.View;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.dialogs.GetExprDialog;
import org.kchine.r.workbench.spreadsheet.EmbeddedPanelDescription;
import org.kchine.r.workbench.views.highlighting.HighlightDocument;
import org.kchine.r.workbench.views.highlighting.NonWrappingTextPane;

public class ButtonView extends DynamicView{
	private RGui _rgui;
	private JTextPane _area;
	private JScrollPane _scrollPane;
	private JButton refreshButton;
	
	JTextField buttonLabelTextField;
	JButton buttonLabelRefreshButton;
	
	private JPanel buttonPanel;

	JPanel bottompanel;
	JPanel controlPanel;
	
	JPanel root;
	EmbeddedPanelDescription embeddedPanelDescritption=null; 
	View view=this;	
	String[] rangeExpr_save=new String[]{""};
	ImageIcon refreshIcon ;
	
	String buttonLabel="Run"; 
	JButton button;
	
	
	void submitToR(boolean manual) {
		if (manual && _rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
		} else {
			_rgui.getRLock().lock();
			try {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_area.setEnabled(false);
					}
				});

				try {
					_rgui.getR().sourceFromBuffer(_area.getText());
				} catch (RemoteException e) {
					JOptionPane.showMessageDialog(null, e.getCause().getMessage(), "R Error", JOptionPane.ERROR_MESSAGE);
					return;
				}


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

	private void showPopup(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(new AbstractAction("Dock") {
			public void actionPerformed(ActionEvent e) {
				
				GetExprDialog dialog=new GetExprDialog(root,"Docking Range",rangeExpr_save);
				dialog.setVisible(true);
				if (dialog.getExpr()!=null) {
					view.close();
					embeddedPanelDescritption=new EmbeddedPanelDescription("SS_0", dialog.getExpr(), buttonPanel);
					_rgui.addEmbeddedPanelDescription(embeddedPanelDescritption);			
					
				}
				
			}
			
			public boolean isEnabled() {
				return embeddedPanelDescritption==null;
			}
		});
		
		
		popupMenu.add(new AbstractAction("Undock") {
			public void actionPerformed(ActionEvent e) {
				_rgui.removeEmbeddedPanelDescription(embeddedPanelDescritption);
				embeddedPanelDescritption=null;
				bottompanel.add(buttonPanel, BorderLayout.CENTER);
				view=_rgui.createView(root, "Button View");								
			}
			
			public boolean isEnabled() {
				return embeddedPanelDescritption!=null;
			}
		});	
		

		popupMenu.show(buttonPanel, e.getX(), e.getY());

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

	public ButtonView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);

		_rgui = rgui;
		
		try {
			refreshIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/views/icons/" + "refresh.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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

		buttonPanel=new JPanel(new BorderLayout());
		

		JButton submit = new JButton(refreshIcon);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						submitToR(true);
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


		
		
		
		
		bottompanel=new JPanel(new BorderLayout());
		
		bottompanel.add(buttonPanel, BorderLayout.CENTER);
		bottompanel.setBorder(BorderFactory.createEtchedBorder());

		controlPanel = new JPanel(new BorderLayout());
		controlPanel.setLayout(new GridLayout(1, 2));
		controlPanel.setBackground(Color.white);
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));


		controlPanel.add(p1);
		controlPanel.add(p2);


		buttonLabelTextField = new JTextField("");
		buttonLabelTextField.setPreferredSize(new Dimension(30,buttonLabelTextField.getHeight()));
		
		
		buttonLabelRefreshButton = new JButton(refreshIcon);
		buttonLabelRefreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonLabel=buttonLabelTextField.getText();
				button.setText(buttonLabel);
			}
		});

		buttonLabelTextField.setEnabled(true);
		buttonLabelRefreshButton.setEnabled(true);

		p1.add(new JLabel("Label"));
		p2.add(getRefreshPanel(buttonLabelTextField,buttonLabelRefreshButton));
		

		JPanel leftWrapper = new JPanel(new BorderLayout());
		leftWrapper.add(controlPanel, BorderLayout.NORTH);

		JPanel pb = new JPanel(new BorderLayout());
		pb.add(leftWrapper, BorderLayout.WEST);
		pb.add(bottompanel, BorderLayout.CENTER);

		
		button=new JButton(buttonLabel);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						submitToR(true);
					}
				}).start();
			}
		});
		
		button.addMouseListener(new MouseListener() {
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
		
		buttonPanel.add(button, BorderLayout.CENTER);
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel,pb);
		splitPane.setDividerSize(3);
		
		root=new JPanel(new BorderLayout());root.add(splitPane, BorderLayout.CENTER);
		

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).setBorder(BorderFactory.createLineBorder(Color.gray, 2));
		((JPanel) getComponent()).add(root);

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


	JPanel getRefreshPanel(JComponent c1, JButton button) {
		JPanel result=new JPanel(new BorderLayout());
		result.add(c1,BorderLayout.CENTER);
		result.add(button,BorderLayout.EAST);
		return result;
	}
	
}