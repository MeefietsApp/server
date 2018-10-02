package nl.hypothermic.mfsrv.obj;

import java.io.Serializable;

public class TelephoneNum implements Serializable {
	
	public final int country;
	
	public final int number;
	
	public TelephoneNum(int country, int number) {
		this.country = country;
		this.number = number;
	}

	@Override public String toString() {
		return "TelephoneNum [country=" + this.country + ", number=" + this.number + "]";
	}
}
