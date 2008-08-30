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
package server;

import java.io.IOException;
import java.io.OutputStream;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ScanStream extends OutputStream {
	private byte[] _continuePattern;
	private byte[] _promptPattern;
	private OutputStream _o;
	private Boolean[] _scanResultHolder;

	public ScanStream(OutputStream o, byte[] continuePattern, byte[] promptPattern, Boolean[] scanResultHolder) {
		_o = o;
		_continuePattern = continuePattern;
		_promptPattern = promptPattern;
		_scanResultHolder = scanResultHolder;
	}

	public void close() throws IOException {
		_o.close();
	}

	public void flush() throws IOException {
		_o.flush();
	}

	public void write(final byte[] b) throws IOException {
		if (b.equals(_continuePattern))
			_scanResultHolder[0] = true;
		_o.write(b);
	}

	boolean isPattern(final byte[] b, final int off, final int len, byte[] pattern) {
		if (pattern.length != len)
			return false;
		for (int i = 0; i < len; ++i) {
			if (pattern[i] != b[off + i])
				return false;
		}
		return true;
	}

	public void write(final byte[] b, final int off, final int len) throws IOException {

		if (isPattern(b, off, len, _continuePattern)) {
			_scanResultHolder[0] = true;
		} else if (isPattern(b, off, len, _promptPattern)) {
		} else {
			_o.write(b, off, len);
		}

	}

	public void write(final int b) throws IOException {
		_o.write(b);
	}
	
	public static void main(String[] args) {
		byte[] b=PoolUtils.hexToBytes("2B202B20");
		String str=new String(b,0,2);
		System.out.println("str="+str);
		
	}
}
