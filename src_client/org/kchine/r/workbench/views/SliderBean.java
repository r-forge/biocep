package org.kchine.r.workbench.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.infonode.docking.View;

import org.kchine.r.workbench.RConnectionListener;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.VariablesChangeEvent;
import org.kchine.r.workbench.VariablesChangeListener;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.dialogs.GetExprDialog;
import org.kchine.r.workbench.spreadsheet.EmbeddedPanelDescription;

public class SliderBean extends JPanel implements ChangeListener, VariablesChangeListener , RConnectionListener {
	
	RGui _rgui;
	JTextField _var, _value, _min, _max, _init;
	JButton _refreshButton;
	JCheckBox _showLabels;
	JSlider _slider;
	JPanel _controlsPanel;
	boolean _showControls=true;
	
	JPanel root;
	EmbeddedPanelDescription embeddedPanelDescritption=null; 
	View view;
	
	String[] rangeExpr_save=new String[]{""};
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	static ImageIcon refreshIcon ;
	static {
		try {
			refreshIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/views/icons/" + "refresh.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	private String _uid = null;

	public SliderBean() {setBackground(Color.white);}

	public void init(RGui rgui, int min, int max, int init, boolean showLabel, String var, boolean showControls) {
		_rgui=rgui;		
		_rgui.addRConnectionListener(this);
		
		_showControls=showControls;
		_var=new JTextField(); _var.setText(var);updateVars();
		_value=new JTextField(); _value.setText(new Integer(init).toString());_value.setEditable(false);
		_min=new JTextField(); _min.setText(new Integer(min).toString());
		_max=new JTextField();_max.setText(new Integer(max).toString());
		_init=new JTextField();_init.setText(new Integer(init).toString());
		_showLabels=new JCheckBox("L");_showLabels.setSelected(showLabel);_showLabels.setHorizontalAlignment(SwingConstants.CENTER);		
		_refreshButton=new JButton(refreshIcon);
		_refreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateVars();
				if (getMin()!=null) _slider.setMinimum(getMin());
				if (getMax()!=null) _slider.setMaximum(getMax());
				if (getInit()!=null) _slider.setValue(getInit());
				_slider.setPaintLabels(_showLabels.isSelected());
			}
		});
		
		_controlsPanel=new JPanel(new GridLayout(0,12));
		if (_showControls) {
			initControlPanel();
		}
				
    	_slider = new JSlider(JSlider.HORIZONTAL, min, max, init);			
		_slider.addChangeListener(this);
		_slider.setMajorTickSpacing(10);
		_slider.setMinorTickSpacing(1);
		_slider.setPaintTicks(true);
		_slider.setPaintLabels(true);
		_slider.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				
			}
			public void mouseEntered(MouseEvent e) {
				
			}
			public void mouseExited(MouseEvent e) {
				
			}
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}				
			}
			
			private void showPopup(MouseEvent e) {
				JPopupMenu popupMenu = new JPopupMenu();

				popupMenu.add(new AbstractAction(_showControls?"Hide Controls":"Show Controls") {
					public void actionPerformed(ActionEvent e) {
						_showControls=!_showControls;
						_controlsPanel.removeAll();
						if (_showControls) initControlPanel();
						_controlsPanel.updateUI();
						SliderBean.this.repaint();						
					}					
				});
				popupMenu.addSeparator();
				popupMenu.add(new AbstractAction("Dock") {
					public void actionPerformed(ActionEvent e) {
						
						GetExprDialog dialog=new GetExprDialog(root,"Docking Range",rangeExpr_save);
						dialog.setVisible(true);
						if (dialog.getExpr()!=null) {
							view.close();
							embeddedPanelDescritption=new EmbeddedPanelDescription("SS_0", dialog.getExpr(), root);
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
						add(root, BorderLayout.CENTER);
						view=_rgui.createView(SliderBean.this, "Slider View");								
					}
					
					public boolean isEnabled() {
						return embeddedPanelDescritption!=null;
					}
				});	
				

				popupMenu.show(_slider, e.getX(), e.getY());

			}
			
		});
		
		
		setLayout(new BorderLayout());		
		
		root=new JPanel(new BorderLayout()); root.add(_slider);
		add(root, BorderLayout.CENTER);
		add(_controlsPanel, BorderLayout.SOUTH);

	}

	void initControlPanel() {
		_controlsPanel.add(new JLabel("Variable",SwingConstants.RIGHT));_controlsPanel.add(_var);
		_controlsPanel.add(new JLabel("Value",SwingConstants.RIGHT));_controlsPanel.add(_value);
		_controlsPanel.add(new JLabel("Min",SwingConstants.RIGHT));_controlsPanel.add(_min);
		_controlsPanel.add(new JLabel("Max",SwingConstants.RIGHT));_controlsPanel.add(_max);
		_controlsPanel.add(new JLabel("Init",SwingConstants.RIGHT));_controlsPanel.add(_init);_controlsPanel.add(_showLabels);
		_controlsPanel.add(_refreshButton);		
	}
	
	public void updateVars() {		
		if (!getVariableName().equals("")) {
			if (_rgui.getR() != null) {
				try {
					_rgui.getR().addProbeOnVariables(new String[] {getVariableName()});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			_rgui.removeVariablesChangeListener(this);
			_rgui.addVariablesChangeListener(this);
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		_value.setText(new Integer(source.getValue()).toString());
		if (!source.getValueIsAdjusting() &&!getVariableName().equals("")) {
			int fps = (int) source.getValue();
			try {
				_rgui.getRLock().lock();
				HashMap<String, Object> props = new HashMap<String, Object>();
				props.put("Scroller", getUID());
				_rgui.getR().consoleSubmit(getVariableName()+"<-" + fps, props);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				_rgui.getRLock().unlock();
			}
		}
	}

	public String getVariableName() {
		return _var.getText().trim();
	}

	public void setMin(Integer min) {
		_min.setText(new Integer(min).toString());
	}

	public Integer getMin() {
		try {
			return Integer.decode(_min.getText().trim());
		} catch (Exception e) {
			return null;
		}
	}

	public void setMax(Integer max) {
		_max.setText(new Integer(max).toString());
	}
	
	public Integer getMax() {
		try {
			return Integer.decode(_max.getText().trim());
		} catch (Exception e) {
			return null;
		}
	}
	
	public Integer getInit() {
		try {
			return Integer.decode(_init.getText().trim());
		} catch (Exception e) {
			return null;
		}
	}

	public void variablesChanged(VariablesChangeEvent event) {

		if (!getVariableName().equals("")) {
			if (event.getVariablesHashSet().contains(getVariableName())) {

				if (event.getClientProperties() == null || !getUID().equals(event.getClientProperties().get("Scroller"))) {

					try {

						Object xObject = _rgui.getR().getObjectConverted(getVariableName());
						final double x = xObject instanceof Double ? (Double) xObject : (double) (Integer) xObject;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								_slider.removeChangeListener(SliderBean.this);
								_slider.setValue((int) x);
								_value.setText(new Integer(_slider.getValue()).toString());
								_slider.addChangeListener(SliderBean.this);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		}
	}
	
	public void destroy() {
		_rgui.removeVariablesChangeListener(this);
	}
	
	
	public void connected() {
		updateVars();		
	}
	
	public void connecting() {
	}
	
	public void disconnected() {
	}
	
	public void disconnecting() {
	}
	
	public String getUID() {
		if (_uid == null) {
			_uid = UUID.randomUUID().toString();
		}
		return _uid;
	}
}
