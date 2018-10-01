package nl.hypothermic.mfsrv.database;

import nl.hypothermic.mfsrv.obj.LoginResult;

public interface IDatabaseHandler {
	
	// --- Events
	
	public void eventServletStart();
	
	public void eventServletStop();
	
	// --- Authenticatie
	
	public LoginResult userLogin(String tel, int pin);
	
	public boolean isUserRegistered(String tel);
	
	public boolean isUserPassword(String tel, int pin);
	
	public boolean isSessionTokenValid(String tel, String token);

}
