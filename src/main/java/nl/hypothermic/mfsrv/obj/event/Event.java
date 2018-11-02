package nl.hypothermic.mfsrv.obj.event;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.codec.DecoderException;

import io.javalin.Context;
import nl.hypothermic.mfsrv.MFLogger;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;

public abstract class Event implements Serializable {

	static final long serialVersionUID = 1L;

	public static Event fromFile(File path) throws ClassNotFoundException, IOException {
		return castFromObject(FileIO.deserialize(path));
	}

	public static Event fromSerializedString(String str) throws ClassNotFoundException, IOException, DecoderException {
		return castFromObject(FileIO.deserializeFromString(str));
	}
	
	public static Event fromType(EventType type) throws InstantiationException, IllegalAccessException {
		return type.getEventClass().newInstance();
	}
	
	public abstract Event fromJavalinCtx(Context ctx) throws InstantiationException, IllegalAccessException, NumberFormatException;
	
	private static Event castFromObject(Object obj) throws ClassNotFoundException {
		if (obj instanceof ParticipatableMeefietsEvent) {
			return (ParticipatableMeefietsEvent) obj;
		} else if (obj instanceof MeefietsEvent) {
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

	@Deprecated // TODO: to be removed in 1.07
	public void toFile() throws IOException {
		MFLogger.err(this, "Warning: you are using a deprecated method which will be removed in 1.07: Event.toFile()");
		this.toFile(new File(new File(ConfigHandler.dbPath, "temp/"), "" + ".mfevt"));
	}

	public void toFile(File path) throws IOException {
		FileIO.serialize(path, this);
	}

	public String toSerializedString() throws IOException {
		return FileIO.serializeToString(this);
	}

	@Override public String toString() {
		return "Event [eventId=" + this.eventId + "]";
	}
}
