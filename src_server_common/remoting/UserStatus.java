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
