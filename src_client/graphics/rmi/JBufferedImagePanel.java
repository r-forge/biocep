/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
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
package graphics.rmi;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class JBufferedImagePanel extends JPanel {
	protected BufferedImage bufferedImage = null;
	private boolean isSnapshot = false;

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
		isSnapshot = true;
	}

	public BufferedImage getImage() {
		return bufferedImage == null ? new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB) : bufferedImage;
	}

	public synchronized void paintComponent(Graphics g) {
		if (bufferedImage != null) {
			((Graphics2D) g).drawRenderedImage(bufferedImage, new AffineTransform());
		}

	}

	@Override
	public int getWidth() {
		if (isSnapshot) {
			return bufferedImage.getWidth();
		} else
			return super.getWidth();
	}

	@Override
	public int getHeight() {
		if (isSnapshot) {
			return bufferedImage.getHeight();
		} else
			return super.getHeight();
	}

	@Override
	public Dimension getPreferredSize() {
		if (isSnapshot) {
			return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
		} else
			return super.getPreferredSize();
	}

}
