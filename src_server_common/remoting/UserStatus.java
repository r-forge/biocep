/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package remoting;

import java.io.Serializable;

public class UserStatus implements Serializable {
	
	private String uid;
	private String userName;
	private boolean typing;

	public UserStatus(String uid, String userName,boolean typing) {
		super();
		this.uid=uid;
		this.typing = typing;
		this.userName = userName;
	}

	public String getUid(){
		return this.uid;
	}
	
	public void setUid(String uid) {
		this.uid=uid;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean isTyping() {
		return typing;
	}
	public void setTyping(boolean typing) {
		this.typing = typing;
	}

	public String toString() {
		return userName;
	}
}
