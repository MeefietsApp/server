package nl.hypothermic.mfsrv.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;
import nl.hypothermic.mfsrv.obj.SessionToken;
import nl.hypothermic.mfsrv.obj.TelephoneNum;

public class TempDatabase implements IDatabaseHandler {

	private static final File dbPath = new File(ConfigHandler.dbPath, "temp/");

	private HashMap<TelephoneNum, String> userComboList = new HashMap<TelephoneNum, String>();

	@Override public void eventServletStart() {
		dbPath.mkdir();
		MFServer.threadpool.execute(new Runnable() {
			@Override public void run() {
				for (File country : dbPath.listFiles()) {
					if (country.isDirectory()) {
						if (country.getName().length() >= 1 && country.getName().matches("[0-9]+")) {
							for (File record : country.listFiles()) {
								if (!record.isDirectory()) {
									if (record.getName().length() >= 1 && record.getName().matches("[0-9]+")) {
										userComboList.put(new TelephoneNum(Integer.valueOf(country.getName()), Integer.valueOf(record.getName())), FileIO.readFileContentsUnsafe(record));
									}
								}
							}
						}
					}
				}
			}
		});
	}

	@Override public void eventServletStop() {
		for (Map.Entry<TelephoneNum, String> record : userComboList.entrySet()) {
			File recordFile = new File(new File(dbPath, record.getKey().country + "/"), record.getKey().number + "");
			try {
				if (!recordFile.exists()) {
					recordFile.createNewFile();
				}
				FileIO.writeFileContents(recordFile, record.getValue());
			} catch (IOException x) {
				System.out.println("Unable to save record for " + record.toString());
				x.printStackTrace();
			}
		}
	}

	private HashMap<TelephoneNum, SessionToken> sessionList = new HashMap<TelephoneNum, SessionToken>();

	@Override public int userLogin(TelephoneNum num, String passwdHash) {
		int i;
		if ((i = isUserLoggedIn(num)) != 0) {
			return i;
		}
		if (!isUserRegistered(num)) {
			return -3;
		}
		if (!isUserPassword(num, passwdHash)) {
			return -4;
		}
		if (sessionList.containsKey(num)) {
			sessionList.remove(num);
		}
		SessionToken token = new SessionToken();
		sessionList.put(num, token);
		return token.token;
	}
	
	@Override public int userRegister(TelephoneNum num, String passwdHash) {
		if (isUserRegistered(num)) {
			return -5;
		}
		userComboList.put(num, passwdHash);
		return 1;
	}

	@Override public boolean isUserRegistered(TelephoneNum num) {
		for (TelephoneNum iter : userComboList.keySet()) {
			if (iter.country == num.country && iter.number == num.number) {
				return true;
			}
		}
		return false;
	}

	@Override public boolean isUserPassword(TelephoneNum num, String passwdHash) {
		for (TelephoneNum iter : userComboList.keySet()) {
			if (iter.country == num.country && iter.number == num.number) {
				return userComboList.get(iter).replaceAll("\n", "").trim().equals(passwdHash.replaceAll("\n", "").trim());
			}
		}
		return false;
	}
	
	/* -- */ public int isUserLoggedIn(TelephoneNum num) {
		for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number) {
				if (iter.getValue().isExpired()) {
					sessionList.remove(iter.getKey());
					return 0;
				} else {
					iter.getValue().resetTime();
					return iter.getValue().token;
				}
			}
		}
		return 0;
	}

	@Override public boolean isSessionTokenValid(TelephoneNum num, int token) {
		for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number && iter.getValue().token == token) {
				iter.getValue().resetTime();
				return true;
			}
		}
		return false;
	}

	@Override public void resetSessionTimer(TelephoneNum num) {
		for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number) {
				iter.getValue().resetTime();
			}
		}
	}
}
