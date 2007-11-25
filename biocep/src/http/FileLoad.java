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
package http;

import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import remoting.RServices;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class FileLoad {

	private static final int BLOCK_SIZE_DOWNLOAD = 1024 * 16;
	private static final int BLOCK_SIZE_UPLOAD = 1024 * 16;

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
