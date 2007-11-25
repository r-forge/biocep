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

import java.awt.Container;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.infonode.docking.TabWindow;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class NewWindow {
	public static GDApplet _applet;

	public static Container create(final JPanel panel, final String title) {
		final Container[] result = new Container[1];
		Runnable createRunnable = new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(_applet.getLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				SwingUtilities.updateComponentTreeUI(panel);

				int id = _applet.getDynamicViewId();
				graphics.rmi.GDApplet.DynamicView v = new graphics.rmi.GDApplet.DynamicView(title, null, panel, id);
				((TabWindow) _applet.views[2].getWindowParent()).addTab(v);
				result[0] = v;
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			createRunnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(createRunnable);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return result[0];

	}
}
