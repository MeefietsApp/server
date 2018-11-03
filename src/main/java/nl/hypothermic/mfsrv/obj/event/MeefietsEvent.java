package nl.hypothermic.mfsrv.obj.event;

import io.javalin.Context;

public class MeefietsEvent extends Event {
	
	static final long serialVersionUID = 2L;
	
	@Override public Event fromJavalinCtx(Context ctx) throws InstantiationException, IllegalAccessException, NumberFormatException {
		if (ctx.queryParam("name") == null || ctx.queryParam("loc") == null || ctx.queryParam("time") == null) {
			throw new NullPointerException();
		}
		eventName = ctx.queryParam("name");
		eventLocation = ctx.queryParam("loc");
		eventEpochTime = Long.parseLong(ctx.queryParam("time"));
		return this;
	}
	
	public String eventName;
	
	public String eventLocation;
	
	public long eventEpochTime;

	public MeefietsEvent() {
		
	}
	
	public String getIdentifier() {
		return eventName + "";
	}

	@Override public void sanitize() {
		// no-op omdat al deze info public is.
	}
	
	@Override public MeefietsEvent getSanitized() {
		try {
			MeefietsEvent clone = (MeefietsEvent) this.clone();
			clone.sanitize();
			return clone;
		} catch (CloneNotSupportedException x) {
			x.printStackTrace();
			return null;
		}
	}

	@Override public String toString() {
		return "MeefietsEvent [eventName=" + this.eventName + ", eventLocation=" + this.eventLocation + ", eventEpochTime=" + this.eventEpochTime + "]";
	}
}
