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

import graphics.pop.GDDevice;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;


import remoting.RServices;
/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public interface RGui {	
	public RServices getR();
	public ReentrantLock getRLock();	
	public ConsoleLogger getConsoleLogger();
	public Container createView(JPanel panel,String title);
	public void setCurrentDevice(GDDevice device);
	public Component getRootComponent();
	
}
