package nl.hypothermic.mfsrv.database;

import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;

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

}
