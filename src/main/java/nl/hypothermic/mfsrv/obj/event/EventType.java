package nl.hypothermic.mfsrv.obj.event;

public enum EventType {
	
	MEEFIETSEVENT(MeefietsEvent.class);
	
	private Class<? extends Event> cl;
	
	private EventType(Class<? extends Event> cl) {
		this.cl = cl;
	}
	
	public Class<? extends Event> getEventClass() {
		return cl;
	}
	
	public static EventType fromInt(int type) throws InvalidEventTypeException {
		switch (type) {
		case 1:
			return EventType.MEEFIETSEVENT;
		}
		throw new InvalidEventTypeException();
	}
}
