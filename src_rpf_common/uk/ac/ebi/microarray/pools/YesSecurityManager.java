/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2007 - 2008  Karim Chine
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
package uk.ac.ebi.microarray.pools;

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
