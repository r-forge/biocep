package http;

import java.io.Serializable;

public class RResponse implements Serializable{
	public RResponse() {}
	public RResponse(Object value, String status) {
		super();
		this.value = value;
		this.status = status;
	}
	private Object value;
	private String status;
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}	
}
