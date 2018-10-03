package nl.hypothermic.mfsrv.obj;

import java.io.Serializable;

public class Account implements Serializable {
	
	public TelephoneNum num;
	
	public String userName;
	
	public Account(TelephoneNum num, String userName) {
		this.num = num;
		this.userName = userName;
	}

	@Override public String toString() {
		return "Account [num=" + this.num + ", userName=" + this.userName + "]";
	}
}
