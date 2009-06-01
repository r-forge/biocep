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
package org.kchine.r.server.http;

import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;

import org.kchine.r.server.RServices;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class FileLoad {

	private static final int BLOCK_SIZE_DOWNLOAD = 1024 * 80;
	private static final int BLOCK_SIZE_UPLOAD = 1024 * 80;

	public static void download(String fileName, File toFile, RServices r) throws RemoteException {
		try {
			long fsize = r.getWorkingDirectoryFileDescription(fileName).getSize();
			System.out.println("fsize=" + fsize);
			System.out.println("tofile=" + toFile.getAbsolutePath());
			RandomAccessFile raf = new RandomAccessFile(toFile.getAbsolutePath(), "rw");
			raf.setLength(0);
			while (raf.length() < fsize) {
				byte[] block = r.readWorkingDirectoryFileBlock(fileName, raf.length(), BLOCK_SIZE_DOWNLOAD);
				raf.write(block);
			}
			raf.close();
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	public static void upload(File fromFile, String fileName, RServices r) throws RemoteException {
		try {
			r.createWorkingDirectoryFile(fileName);
			RandomAccessFile raf = new RandomAccessFile(fromFile.getAbsolutePath(), "r");
			long fsize = raf.length();
			raf.seek(0);
			while (raf.getFilePointer() < fsize) {
				byte[] block = new byte[BLOCK_SIZE_UPLOAD];
				int n = raf.read(block);
				if (n < block.length) {
					byte[] temp = new byte[n];
					System.arraycopy(block, 0, temp, 0, n);
					block = temp;
				}
				r.appendBlockToWorkingDirectoryFile(fileName, block);
			}
			raf.close();
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

}
