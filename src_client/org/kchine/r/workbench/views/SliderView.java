package org.kchine.r.workbench.views;


import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.kchine.r.workbench.RGui;

public class SliderView extends DynamicView {
	SliderBean sliderPanel;
	public SliderView(String title, Icon icon, int id, RGui rgui, int min, int max, int init) {
		super(title, icon, new JPanel(), id);
		setLayout(new BorderLayout());		
		sliderPanel=new SliderBean();
		sliderPanel.setView(this);
		sliderPanel.init(rgui,min,max,init,true,"",true);
		add(sliderPanel);
	}
	public void destroy() {
		sliderPanel.destroy();
	}
}
