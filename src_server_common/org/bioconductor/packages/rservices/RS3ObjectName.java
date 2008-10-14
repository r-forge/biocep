package org.bioconductor.packages.rservices;

public class RS3ObjectName extends RS3 implements ObjectNameInterface{
	private String _name; 
	private String _env;
	
	public RS3ObjectName() {
	}

	public RS3ObjectName(String name) {
		this._name = name;
		this._env = ".GlobalEnv";
	}

	public RS3ObjectName(String environment, String name) {
		this._name = name;
		this._env = environment;
	}

	
	public String getRObjectName() {return _name;}
	public void setRObjectName(String _name) {this._name = _name;}
	public String getRObjectEnvironment() {return _env;}
	public void setRObjectEnvironment(String _env) {this._env = _env;}


	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ObjectNameInterface) )	return false;
		return (((ObjectNameInterface) obj).getRObjectName().equals(this._name)) && (((ObjectNameInterface) obj).getRObjectEnvironment().equals(_env));
	}
	
	public String toString() {
		return "RS3ObjectName:"+_env+"$"+_name;
	}

    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
    	out.writeUTF(_env);
    	out.writeUTF(_name);
    }

    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException {
    	_env=in.readUTF();
    	_name=in.readUTF();
    }
}