package org.kchine.r.workbench.dialogs;

public class UrlLoginPwd {
	public UrlLoginPwd(String url, String login, String pwd) {
		super();
		this._url = url;
		this._login = login;
		this._pwd = pwd;
	}
	private String _url;
	private String _login;
	private String _pwd;
	
	
	public String getUrl() {
		return _url;
	}
	public String getLogin() {
		return _login;
	}
	public String getPwd() {
		return _pwd;
	}
	

}
