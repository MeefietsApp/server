package nl.hypothermic.mfsrv.database;

import java.util.HashMap;

import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.LoginResult;

public class TempDatabase implements IDatabaseHandler {
	
	@Override public void eventServletStart() {
		// TODO Auto-generated method stub
		
	}

	@Override public void eventServletStop() {
		// TODO Auto-generated method stub
		
	}
	
	private HashMap<String, Long> sessionList = new HashMap<String, Long>();
	
	@Override public LoginResult userLogin(String tel, int pin) {
		if (!isUserRegistered(tel)) {
			return LoginResult.NOT_REGISTERED;
		}
		if (!isUserPassword(tel, pin)) {
			return LoginResult.INVALID_CREDS;
		}
		if (sessionList.containsKey(tel)) {
			sessionList.remove(tel);
		}
		sessionList.put("", System.currentTimeMillis());
		return LoginResult.SUCCESS;
	}
	
	@Override public boolean isUserRegistered(String tel) {
		// TODO: check hier of user bestaat in database
		return false; // temp return
	}
	
	@Override public boolean isUserPassword(String tel, int pin) {
		// TODO: check hier of user's wachtwoord hetzelfde is als pin
		return false; // temp return
	}

	@Override public boolean isSessionTokenValid(String tel, String token) {
		if (sessionList.containsKey(tel)) {
			return sessionList.get(tel) + MFServer.SESSION_TIMEOUT > System.currentTimeMillis();
		} else {
			return false;
		}
	}
}
