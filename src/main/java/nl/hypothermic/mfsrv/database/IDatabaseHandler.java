package nl.hypothermic.mfsrv.database;

import java.util.ArrayList;

import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;

public interface IDatabaseHandler {
	
	// --- Events
	
	public void eventServletStart() throws Exception;
	
	public void eventServletStop() throws Exception;
	
	// --- Authenticatie
	
	public int userLogin(TelephoneNum num, String passwdHash);
	
	public int userRegister(TelephoneNum num, String passwdHash);
	
	public int userVerify(TelephoneNum num, int verificationToken);
	
	public boolean isUserRegistered(TelephoneNum num);
	
	public boolean isUserPassword(TelephoneNum num, String passwdHash);
	
	public boolean isSessionTokenValid(TelephoneNum num, int token);
	
	public void resetSessionTimer(TelephoneNum num);
	
	// --- Account
	
	public Account getAccount(TelephoneNum num);
	
	// --- Events
	
	public Event getEvent(int eventId);

	public int createEvent(EventType type);
	
	public int registerEvent(Event event);
	
	public ArrayList<Integer> getUserEvents(Account acc);

}
