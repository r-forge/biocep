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
package org.kchine.rpf;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class YesSecurityManager extends SecurityManager {

	@Override
	public void checkAccept(String host, int port) {
	}

	@Override
	public void checkAccess(Thread t) {
	}

	@Override
	public void checkAccess(ThreadGroup g) {
	}

	@Override
	public void checkAwtEventQueueAccess() {
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
	}

	@Override
	public void checkConnect(String host, int port) {
	}

	@Override
	public void checkCreateClassLoader() {

	}

	@Override
	public void checkDelete(String file) {

	}

	@Override
	public void checkExec(String cmd) {

	}

	@Override
	public void checkExit(int status) {

	}

	@Override
	public void checkLink(String lib) {

	}

	@Override
	public void checkListen(int port) {
	}

	@Override
	public void checkMemberAccess(Class<?> clazz, int which) {
	}

	@Override
	public void checkMulticast(InetAddress maddr) {
	}

	@Override
	public void checkPackageAccess(String pkg) {
	}

	@Override
	public void checkPackageDefinition(String pkg) {
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
	}

	@Override
	public void checkPermission(Permission perm) {
	}

	@Override
	public void checkPrintJobAccess() {
	}

	@Override
	public void checkPropertiesAccess() {
	}

	@Override
	public void checkPropertyAccess(String key) {
	}

	@Override
	public void checkRead(FileDescriptor fd) {
	}

	@Override
	public void checkRead(String file, Object context) {
	}

	@Override
	public void checkRead(String file) {
	}

	@Override
	public void checkSecurityAccess(String target) {
	}

	@Override
	public void checkSetFactory() {
	}

	@Override
	public void checkSystemClipboardAccess() {
	}

	@Override
	public boolean checkTopLevelWindow(Object window) {
		return true;
	}

	@Override
	public void checkWrite(FileDescriptor fd) {
	}

	@Override
	public void checkWrite(String file) {

	}

}
