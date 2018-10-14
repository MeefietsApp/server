package nl.hypothermic.mfsrv.database;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import nl.hypothermic.mfsrv.MFLogger;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.SessionToken;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.obj.auth.UnverifiedMapEntry;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;

public class TempDatabase implements IDatabaseHandler {

	private static final File dbPath = new File(ConfigHandler.dbPath, "temp/");
	private static final File eventPath = new File(dbPath, "events/");
	private static final File eventCounter = new File(eventPath, "count");

	private HashMap<TelephoneNum, String> userComboList = new HashMap<TelephoneNum, String>();
	private HashMap<TelephoneNum, Entry<String, Integer>> unverifiedComboList = new HashMap<TelephoneNum, Entry<String, Integer>>();

	private MFServer instance;

	public TempDatabase(MFServer instance) {
		this.instance = instance;
	}

	public int getNextEventId() {
		int id = 0;
		try {
			id = Integer.valueOf(FileIO.readFileContents(eventCounter));
			FileIO.writeFileContents(eventCounter, (id + 1) + "");
		} catch (Exception x) {
			// TODO Auto-generated catch block
			x.printStackTrace();
		}
		return id;
	}

	@Override
	public void eventServletStart() {
		eventPath.mkdirs();
		if (!eventCounter.exists()) {
			try {
				if (eventCounter.createNewFile()) {
					FileIO.writeFileContents(eventCounter, "0");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		MFServer.threadpool.execute(new Runnable() {
			@Override
			public void run() {
				for (File country : dbPath.listFiles()) {
					if (country.isDirectory()) {
						if (country.getName().length() >= 1 && country.getName().matches("[0-9]+")) {
							for (File record : country.listFiles()) {
								if (!record.isDirectory()) {
									if (record.getName().length() >= 1 && record.getName().matches("[0-9]+")) {
										userComboList.put(
												new TelephoneNum(Integer.valueOf(country.getName()),
														Integer.valueOf(record.getName())),
												FileIO.readFileContentsUnsafe(record));
									}
								}
							}
						}
					}
				}
			}
		});
	}

	@Override
	public void eventServletStop() {
		for (Map.Entry<TelephoneNum, String> record : userComboList.entrySet()) {
			File recordFile = new File(new File(dbPath, record.getKey().country + "/"), record.getKey().number + "");
			try {
				if (!recordFile.exists()) {
					System.out.println(recordFile.getAbsolutePath());
					Files.setPosixFilePermissions(recordFile.toPath().getParent(),
							EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE));
					recordFile.createNewFile();
				}
				FileIO.writeFileContents(recordFile, record.getValue());
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}

	private HashMap<TelephoneNum, SessionToken> sessionList = new HashMap<TelephoneNum, SessionToken>();

	@Override
	public int userLogin(TelephoneNum num, String passwdHash) {
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

	@Override
	public int userRegister(TelephoneNum num, String passwdHash) {
		if (isUserRegistered(num)) {
			return -5;
		}
		if (isUserUnverified(num)) {
			return -6;
		}
		int verificationToken = ThreadLocalRandom.current().nextInt(10000, 99999);
		unverifiedComboList.put(num, new UnverifiedMapEntry(passwdHash, verificationToken));
		try {
			instance.nexmo.sendTextMessage("MeefietsApp", num.country + "" + num.number,
					"Uw verifieercode voor uw MeefietsApp account is: " + verificationToken);
		} catch (Exception x) {
			x.printStackTrace();
			return -7;
		}
		MFLogger.log(this, "Nieuwe gebruiker: " + num.toString() + " " + verificationToken);
		return 1;
	}

	@Override
	public int userVerify(TelephoneNum num, int verificationToken) {
		for (Entry<TelephoneNum, Entry<String, Integer>> iter : unverifiedComboList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number) {
				if (iter.getValue().getValue().equals(verificationToken)) {
					MFLogger.log(this, "Gebruiker is geverifieerd: " + num.toString());
					userComboList.put(num, iter.getValue().getKey());
					unverifiedComboList.remove(iter.getKey());
					return 1;
				}
			}
		}
		MFLogger.log(this, "Kon gebruiker niet verifieren: " + num.toString());
		return 0;
	}

	@Override
	public boolean isUserRegistered(TelephoneNum num) {
		for (TelephoneNum iter : userComboList.keySet()) {
			if (iter.country == num.country && iter.number == num.number) {
				return true;
			}
		}
		return false;
	}

	/* -- */ public boolean isUserUnverified(TelephoneNum num) {
		for (TelephoneNum iter : unverifiedComboList.keySet()) {
			if (iter.country == num.country && iter.number == num.number) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isUserPassword(TelephoneNum num, String passwdHash) {
		for (TelephoneNum iter : userComboList.keySet()) {
			if (iter.country == num.country && iter.number == num.number) {
				return userComboList.get(iter).replaceAll("\n", "").trim()
						.equals(passwdHash.replaceAll("\n", "").trim());
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

	@Override
	public boolean isSessionTokenValid(TelephoneNum num, int token) {
		if (num == null) {
			for (SessionToken iter : sessionList.values()) {
				if (iter.token == token) {
					return true;
				}
			}
		} else {
			for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
				if (iter.getKey().country == num.country && iter.getKey().number == num.number
						&& iter.getValue().token == token) {
					iter.getValue().resetTime();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void resetSessionTimer(TelephoneNum num) {
		for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number) {
				iter.getValue().resetTime();
			}
		}
	}

	@Override
	public Account getAccount(TelephoneNum num) {
		File record = new File(dbPath, num.country + "/" + num.number + ".acc");
		if (record.exists()) {
			try {
				return (Account) FileIO.deserialize(record);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			try {
				Account acc = new Account(num, "user" + ThreadLocalRandom.current().nextInt(100000, 999999));
				FileIO.serialize(record, acc);
				return acc;
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Event getEvent(int eventId) {
		File record = new File(dbPath, "events/" + eventId + ".evt");
		if (record.exists()) {
			try {
				return (Event) FileIO.deserialize(record);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public int createEvent(EventType type) {
		int id = getNextEventId();
		File record = new File(dbPath, "events/" + id + ".evt");
		if (record.exists()) {
			MFLogger.err(this, "critical: trying to create event " + id + " which already exists!");
		}
		Event event = null;
		try {
			event = Event.fromType(EventType.MEEFIETSEVENT);
		} catch (Exception e) {
			e.printStackTrace();
			return -8;
		}
		if (event == null) {
			return -7;
		}
		try {
			FileIO.serialize(record, event);
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
			return -6;
		}
	}
	
	@Override
	public int registerEvent(Event event) {
		int id = getNextEventId();
		File record = new File(dbPath, "events/" + id + ".evt");
		if (record.exists()) {
			MFLogger.err(this, "critical: trying to register event " + id + " which already exists!");
		}
		try {
			FileIO.serialize(record, event);
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
			return -6;
		}
	}

	@Override
	public ArrayList<Integer> getUserEvents(Account acc) {
		File record = new File(dbPath, acc.num.country + "/" + acc.num.number + ".evtl");
		if (record.exists()) {
			try {
				return (ArrayList<Integer>) FileIO.deserialize(record);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			try {
				ArrayList<Integer> events = new ArrayList<Integer>();
				FileIO.serialize(record, events);
				return events;
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}
}
