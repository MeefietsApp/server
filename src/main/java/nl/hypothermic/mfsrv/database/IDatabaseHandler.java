package nl.hypothermic.mfsrv.database;

import nl.hypothermic.mfsrv.obj.Account;
import nl.hypothermic.mfsrv.obj.TelephoneNum;

public interface IDatabaseHandler {
	
	// --- Events
	
	public void eventServletStart();
	
	public void eventServletStop();
	
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

}
