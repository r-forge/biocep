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
package org.kchine.r.server.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import org.kchine.r.server.graphics.primitive.GDObject;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface GDDevice extends Remote {
	public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException;
	public boolean hasGraphicObjects() throws RemoteException;
	void fireSizeChangedEvent(int w, int h) throws RemoteException;
	public void dispose() throws RemoteException;
	public Dimension getSize() throws RemoteException;
	public void putLocation(Point2D p) throws RemoteException;
	public boolean hasLocations() throws RemoteException;
	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException;
	public int getDeviceNumber() throws RemoteException;
	public boolean isCurrentDevice() throws RemoteException;
	public void setAsCurrentDevice() throws RemoteException;
	public Vector<String> getSVGAsText() throws RemoteException;
	public Vector<String> getSVGAsText(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException;
	public byte[] getSVG() throws RemoteException;
	public byte[] getSVG(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException;
	public byte[] getPostscript() throws RemoteException;
	public byte[] getPostscript(Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg, Integer width, Integer height, Boolean horizontal, Integer pointsize, String paper , Boolean pagecentre, String colormodel) throws RemoteException;	
	public byte[] getPdf() throws RemoteException;
	public byte[] getPdf(Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper, 
			String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException;	
	public byte[] getPictex() throws RemoteException;
	public byte[] getPictex(Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException;	
	public byte[] getBmp() throws RemoteException;
	public byte[] getBmp(Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException;		     	     
	public byte[] getJpeg() throws RemoteException;
	public byte[] getJpeg(Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg,Integer res) throws RemoteException ;	
	public byte[] getPng() throws RemoteException;
	public byte[] getPng(Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException;
	public byte[] getTiff() throws RemoteException;
	public byte[] getTiff(Integer width, Integer height, String units, Integer pointsize, String compression, String bg,Integer res) throws RemoteException;	
	public byte[] getXfig() throws RemoteException;
	public byte[] getXfig( Boolean onefile, String encoding , String paper, Boolean horizontal, 
			Integer width, Integer height, String family , Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException;	
	public byte[] getWmf(boolean useserver) throws RemoteException;
	public byte[] getEmf(boolean useserver) throws RemoteException;	
	public byte[] getOdg() throws RemoteException;		
	public String getId() throws RemoteException;	
	public boolean isBroadcasted() throws RemoteException;	
	public void broadcast() throws RemoteException;
}
