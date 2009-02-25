package org.kchine.r.workbench.views;


import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.VariablesChangeEvent;
import org.kchine.r.workbench.VariablesChangeListener;
import org.kchine.r.workbench.WorkbenchApplet;

public class SliderView extends DynamicView implements ChangeListener, VariablesChangeListener {

	RGui _rgui;

	JTextField _var, _value, _min, _max, _init;
	JCheckBox _showLabels;
	JSlider slider;

	private String _uid = null;

	public String getUID() {
		if (_uid == null) {
			_uid = UUID.randomUUID().toString();
		}
		return _uid;
	}
	
	ImageIcon refreshIcon ;
	

	public SliderView(String title, Icon icon, int id, RGui rgui, int min, int max, int init) {
		super(title, icon, new JPanel(), id);
		try {
			refreshIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/views/icons/" + "refresh.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		_rgui=rgui;
		
		_var=new JTextField();
		_value=new JTextField(); _value.setText(new Integer(init).toString());_value.setEditable(false);
		_min=new JTextField(); _min.setText(new Integer(min).toString());
		_max=new JTextField();_max.setText(new Integer(max).toString());
		_init=new JTextField();_init.setText(new Integer(init).toString());
		_showLabels=new JCheckBox("L");_showLabels.setSelected(true);_showLabels.setHorizontalAlignment(SwingConstants.CENTER);
		
		JButton refreshButton=new JButton(refreshIcon);
		refreshButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateVars();
				if (getMin()!=null) slider.setMinimum(getMin());
				if (getMax()!=null) slider.setMaximum(getMax());
				if (getInit()!=null) slider.setValue(getInit());
				slider.setPaintLabels(_showLabels.isSelected());
			}
		});
		
		JPanel p1=new JPanel(new GridLayout(0,12));
		p1.add(new JLabel("Variable",SwingConstants.RIGHT));p1.add(_var);
		p1.add(new JLabel("Value",SwingConstants.RIGHT));p1.add(_value);
		p1.add(new JLabel("Min",SwingConstants.RIGHT));p1.add(_min);
		p1.add(new JLabel("Max",SwingConstants.RIGHT));p1.add(_max);
		p1.add(new JLabel("Init",SwingConstants.RIGHT));p1.add(_init);p1.add(_showLabels);
		p1.add(refreshButton);
		
		
    	slider = new JSlider(JSlider.HORIZONTAL, min, max, init);			
		slider.addChangeListener(this);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
			
		((JPanel) getComponent()).setLayout(new BorderLayout());		
		((JPanel) getComponent()).add(slider, BorderLayout.CENTER);
		((JPanel) getComponent()).add(p1, BorderLayout.SOUTH);

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

	String getVariableName() {
		return _var.getText().trim();
	}
	
	Integer getMin() {
		try {
			return Integer.decode(_min.getText().trim());
		} catch (Exception e) {
			return null;
		}
	}

	Integer getMax() {
		try {
			return Integer.decode(_max.getText().trim());
		} catch (Exception e) {
			return null;
		}
	}
	
	Integer getInit() {
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
								slider.removeChangeListener(SliderView.this);
								slider.setValue((int) x);
								_value.setText(new Integer(slider.getValue()).toString());
								slider.addChangeListener(SliderView.this);
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

}
