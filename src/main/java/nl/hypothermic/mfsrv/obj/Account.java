package nl.hypothermic.mfsrv.obj;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;

public class Account implements Serializable {
	
	public static Account fromFile(File path) throws ClassNotFoundException, IOException {
		return (Account) FileIO.deserialize(path);
	}
	
	public static Account fromSerializedString(String str) throws ClassNotFoundException, IOException {
		return (Account) FileIO.deserializeFromString(str);
	}
	
	public TelephoneNum num;
	
	public String userName;
	
	public Account(TelephoneNum num, String userName) {
		this.num = num;
		this.userName = userName;
	}

	@Override public String toString() {
		return "Account [num=" + this.num + ", userName=" + this.userName + "]";
	}
	
	public void toFile() throws IOException {
		// TODO fix hardcoded pad naar temp database!!!
		this.toFile(new File(new File(ConfigHandler.dbPath, "temp/"), num.country + "/" + num.number + ".acc"));
	}
	
	public void toFile(File path) throws IOException {
		FileIO.serialize(path, this);
	}

	public String toSerializedString() throws IOException {
		return FileIO.serializeToString(this);
	}
}
