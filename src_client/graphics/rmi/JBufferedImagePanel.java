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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.JPanel;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class JBufferedImagePanel extends JPanel {
	protected BufferedImage bufferedImage = null;
	private boolean isSnapshot=false;

	public JBufferedImagePanel() {
		super();
	}

	public JBufferedImagePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public JBufferedImagePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public JBufferedImagePanel(LayoutManager layout) {
		super(layout);
	}

	public JBufferedImagePanel(BufferedImage image) {
		super();
		bufferedImage = image;
		isSnapshot=true;
	}

	public BufferedImage getImage() {
		return bufferedImage == null ? new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB)
				: bufferedImage;
	}

	public synchronized void paintComponent(Graphics g) {
		if (bufferedImage != null) {
			((Graphics2D) g).drawRenderedImage(bufferedImage, new AffineTransform());
		}

	}
	
	@Override
	public int getWidth() {
		if (isSnapshot) {return bufferedImage.getWidth();} else return super.getWidth();
	}
	
	@Override
	public int getHeight() {
		if (isSnapshot) {return bufferedImage.getHeight();} else return super.getHeight();
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (isSnapshot) {return new Dimension(bufferedImage.getWidth(),bufferedImage.getHeight());} else return super.getPreferredSize();	}
	
}
