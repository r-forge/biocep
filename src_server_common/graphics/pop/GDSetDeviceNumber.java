/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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
package graphics.pop;

import java.awt.Component;
import java.awt.Graphics;

import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.GDState;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class GDSetDeviceNumber extends GDObject implements GDActionMarker {
	private int _deviceNumber;

	public GDSetDeviceNumber(int deviceNumber) {
		_deviceNumber = deviceNumber;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		throw new RuntimeException("shouldn't be called");
	}
}
