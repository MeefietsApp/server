package nl.hypothermic.mfsrv.obj;

import java.util.concurrent.ThreadLocalRandom;

import nl.hypothermic.mfsrv.MFServer;

public class SessionToken {
	
	private long time;
	
	public final int token;
	
	public SessionToken() {
		this.time = System.currentTimeMillis();
		this.token = ThreadLocalRandom.current().nextInt(1000000, 9999999);
	}

	public boolean isExpired() {
		System.out.println("Expired? " + (System.currentTimeMillis() > time + MFServer.SESSION_TIMEOUT));
		return System.currentTimeMillis() > time + MFServer.SESSION_TIMEOUT;
	}
	
	public void resetTime() {
		this.time = System.currentTimeMillis();
	}
}
