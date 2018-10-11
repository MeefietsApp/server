package nl.hypothermic.mfsrv.obj.event;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.codec.DecoderException;

import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;
import nl.hypothermic.mfsrv.obj.account.Account;

public abstract class Event implements Serializable {

	static final long serialVersionUID = 1L;

	public static Event fromFile(File path) throws ClassNotFoundException, IOException {
		return castFromObject(FileIO.deserialize(path));
	}

	public static Event fromSerializedString(String str) throws ClassNotFoundException, IOException, DecoderException {
		return castFromObject(FileIO.deserializeFromString(str));
	}
	
	private static Event castFromObject(Object obj) throws ClassNotFoundException {
		if (obj instanceof MeefietsEvent) {
			return (MeefietsEvent) obj;
		} else if (obj instanceof Event) {
			return (Event) obj;
		} else {
			throw new ClassNotFoundException("Event type not supported.");
		}
	}

	public int eventId;

	public Event() {

	}
	
	public String getIdentifier() {
		return eventId + "";
	}
	
	public abstract Event sanitize();

	public void toFile() throws IOException {
		// TODO fix hardcoded pad naar temp database!!!
		this.toFile(new File(new File(ConfigHandler.dbPath, "temp/"), "" + ".mfevt"));
	}

	public void toFile(File path) throws IOException {
		FileIO.serialize(path, this);
	}

	public String toSerializedString() throws IOException {
		return FileIO.serializeToString(this);
	}
}
