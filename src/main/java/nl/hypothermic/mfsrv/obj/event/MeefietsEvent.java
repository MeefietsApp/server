package nl.hypothermic.mfsrv.obj.event;

import java.io.File;
import java.io.IOException;

import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;

public class MeefietsEvent extends Event {
	
	static final long serialVersionUID = 2L;
	
	public String eventName;
	
	public String eventLocation;
	
	public long eventEpochTime;

	public MeefietsEvent() {
		
	}
	
	public String getIdentifier() {
		return eventName + "";
	}

	@Override public MeefietsEvent sanitize() {
		return new MeefietsEvent();
	}
}
