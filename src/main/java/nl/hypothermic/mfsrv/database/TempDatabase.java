package nl.hypothermic.mfsrv.database;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;
import nl.hypothermic.mfsrv.obj.LoginResult;
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

	private HashMap<TelephoneNum, Long> sessionList = new HashMap<TelephoneNum, Long>();

	@Override public LoginResult userLogin(TelephoneNum num, String passwdHash) {
		if (!isUserRegistered(num)) {
			return LoginResult.NOT_REGISTERED;
		}
		if (!isUserPassword(num, passwdHash)) {
			return LoginResult.INVALID_CREDS;
		}
		if (sessionList.containsKey(num)) {
			sessionList.remove(num);
		}
		sessionList.put(num, System.currentTimeMillis());
		return LoginResult.SUCCESS;
	}

	@Override public boolean isUserRegistered(TelephoneNum num) {
		return userComboList.containsKey(num);
	}

	@Override public boolean isUserPassword(TelephoneNum num, String passwdHash) {
		return userComboList.get(num) == passwdHash;
	}

	@Override public boolean isSessionTokenValid(TelephoneNum num, String token) {
		if (sessionList.containsKey(num)) {
			return sessionList.get(num) + MFServer.SESSION_TIMEOUT > System.currentTimeMillis();
		} else {
			return false;
		}
	}
}
