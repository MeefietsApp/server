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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import nl.hypothermic.mfsrv.MFLogger;
import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.config.ConfigHandler;
import nl.hypothermic.mfsrv.config.FileIO;
import nl.hypothermic.mfsrv.obj.NetArrayList;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.SessionToken;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.obj.auth.UnverifiedMapEntry;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;
import nl.hypothermic.mfsrv.obj.event.ParticipatableMeefietsEvent;

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
			id = Integer.valueOf(FileIO.readFileContents(eventCounter).trim());
			FileIO.writeFileContents(eventCounter, (id + 1) + "");
		} catch (Exception x) {
			// TODO Auto-generated catch block
			x.printStackTrace();
		}
		return id;
	}

	@Override public void eventServletStart() {
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
			@Override public void run() {
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

	@Override public void eventServletStop() {
		for (Map.Entry<TelephoneNum, String> record : userComboList.entrySet()) {
			File recordFile = new File(new File(dbPath, record.getKey().country + "/"), record.getKey().number + "");
			try {
				if (!recordFile.exists()) {
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
	
	private void requestModify() {
		//session lock
		/// modify
		//session unlock
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

	@Override public int userVerify(TelephoneNum num, int verificationToken) {
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

	@Override public boolean isUserRegistered(TelephoneNum num) {
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

	@Override public boolean isUserPassword(TelephoneNum num, String passwdHash) {
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

	@Override public boolean isSessionTokenValid(TelephoneNum num, int token) {
		if (num == null) {
			for (SessionToken iter : sessionList.values()) {
				if (iter.token == token) {
					iter.resetTime();
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

	@Override public void resetSessionTimer(TelephoneNum num) {
		for (Entry<TelephoneNum, SessionToken> iter : sessionList.entrySet()) {
			if (iter.getKey().country == num.country && iter.getKey().number == num.number) {
				iter.getValue().resetTime();
			}
		}
	}

	@Override public Account getAccount(TelephoneNum num) {
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

	@Override public Event getEvent(int eventId) {
		File record = new File(dbPath, "events/" + eventId + ".evt");
		if (record.exists()) {
			try {
				Event e = (Event) FileIO.deserialize(record);
				e.eventId = eventId;
				return e;
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}

	@Override public int createEvent(EventType type) {
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
	
	@Override public int registerEvent(Event event) {
		int id = getNextEventId();
		File record = new File(dbPath, "events/" + id + ".evt");
		if (record.exists()) {
			MFLogger.err(this, "critical: trying to register event " + id + " which already exists!");
		}
		event.eventId = id;
		try {
			FileIO.serialize(record, event);
			return id;
		} catch (IOException e) {
			e.printStackTrace();
			return -6;
		}
	}

	@Override public NetArrayList<Integer> getUserEvents(TelephoneNum num) {
		File record = new File(dbPath, num.country + "/" + num.number + ".etl");
		if (record.exists()) {
			try {
				return (NetArrayList<Integer>) FileIO.deserialize(record);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			try {
				NetArrayList<Integer> events = new NetArrayList<Integer>();
				FileIO.serialize(record, events);
				return events;
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}
	
	@Override public int addUserEvent(int eventId, TelephoneNum dest) {
		NetArrayList<Integer> events = this.getUserEvents(dest);
		for (Iterator<Integer> it = events.iterator(); it.hasNext(); ) {
		    Integer iter = it.next();
		    if (iter != null && iter == eventId) {
		    	return -6;
		    }
		}
		events.add(eventId);
		try {
			FileIO.serialize(new File(dbPath, dest.country + "/" + dest.number + ".etl"), events);
		} catch (IOException x) {
			x.printStackTrace();
			return -7;
		}
		return 1;
	}
	
	@Override public int deleteUserEvent(int eventId, TelephoneNum dest) {
		boolean res = false;
		NetArrayList<Integer> events = this.getUserEvents(dest);
		for (Iterator<Integer> it = events.iterator(); it.hasNext(); ) {
		    Integer iter = it.next();
		    if (iter != null && iter == eventId) {
		    	it.remove();
		    	res = true;
		    }
		}
		if (res) {
			try {
				FileIO.serialize(new File(dbPath, dest.country + "/" + dest.number + ".etl"), events);
				return 1;
			} catch (IOException x) {
				x.printStackTrace();
				return -7;
			}
		}
		return 0;
	}

	@Override public NetArrayList<TelephoneNum> getContacts(TelephoneNum num) {
		File record = new File(dbPath, num.country + "/" + num.number + ".ctl");
		if (record.exists()) {
			try {
				return (NetArrayList<TelephoneNum>) FileIO.deserialize(record);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else {
			try {
				NetArrayList<TelephoneNum> events = new NetArrayList<TelephoneNum>();
				FileIO.serialize(record, events);
				return events;
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return null;
	}

	@Override public int addContact(TelephoneNum num, TelephoneNum dest) {
		NetArrayList<TelephoneNum> contacts = this.getContacts(num);
		for (Iterator<TelephoneNum> it = contacts.iterator(); it.hasNext(); ) {
		    TelephoneNum iter = it.next();
		    if (iter.country == dest.country && iter.number == dest.number) {
				return -7;
			}
		}
		contacts.add(dest);
		try {
			FileIO.serialize(new File(dbPath, num.country + "/" + num.number + ".ctl"), contacts);
			return 1;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return -6;
	}

	@Override public int deleteContact(TelephoneNum num, TelephoneNum dest) {
		NetArrayList<TelephoneNum> contacts = this.getContacts(num);
		boolean ret = false;
		for (Iterator<TelephoneNum> it = contacts.iterator(); it.hasNext(); ) {
		    TelephoneNum iter = it.next();
		    if (iter.country == dest.country && iter.number == dest.number) {
				it.remove();
				ret = true;
				break;
			}
		}
		try {
			FileIO.serialize(new File(dbPath, num.country + "/" + num.number + ".ctl"), contacts);
			return ret ? 1 : 0;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return -6;
	}
	
	@Override public int eventIsParticipated(int eventId, TelephoneNum num) {
		NetArrayList<Integer> events = this.getUserEvents(num);
		for (Iterator<Integer> it = events.iterator(); it.hasNext(); ) {
		    Integer iter = it.next();
		    if (iter != null && iter == eventId) {
		    	Event e = this.getEvent(iter);
		    	if (e instanceof ParticipatableMeefietsEvent) {
		    		for (TelephoneNum iter2 : ((ParticipatableMeefietsEvent) e).participants) {
		    			if (iter2 != null && iter2.country == num.country && iter2.number == num.number) {
		    				return 1;
		    			}
		    		}
		    	} else {
		    		return -5;
		    	}
		    }
		}
		return 0;
	}

	@Override public int eventParticipate(int eventId, TelephoneNum num) {
		NetArrayList<Integer> events = this.getUserEvents(num);
		for (Iterator<Integer> it = events.iterator(); it.hasNext(); ) {
		    Integer iter = it.next();
		    if (iter != null && iter == eventId) {
		    	Event e = this.getEvent(iter);
		    	if (e instanceof ParticipatableMeefietsEvent) {
		    		if (!((ParticipatableMeefietsEvent) e).isPrivate) {
		    			for (TelephoneNum iter2 : ((ParticipatableMeefietsEvent) e).participants) {
		    				if (iter2 != null && iter2.country == num.country && iter2.number == num.number) {
		    					return -1;
		    				}
		    			}
		    			((ParticipatableMeefietsEvent) e).participants.add(num);
		    			try {
		    				FileIO.serialize(new File(dbPath, "events/" + e.eventId + ".evt"), e);
		    				return 1;
		    			} catch (IOException x) {
		    				x.printStackTrace();
		    				return -6;
		    			}
		    		}
		    		return -2;
		    	}
		    	return -5;
		    }
		}
		return 0;
	}

	@Override public int eventUnparticipate(int eventId, TelephoneNum num) {
		NetArrayList<Integer> events = this.getUserEvents(num);
		for (Iterator<Integer> it = events.iterator(); it.hasNext(); ) {
		    Integer iter = it.next();
		    if (iter != null && iter == eventId) {
		    	Event e = this.getEvent(iter);
		    	if (e instanceof ParticipatableMeefietsEvent) {
		    		for (Iterator<TelephoneNum> it2 = ((ParticipatableMeefietsEvent) e).participants.iterator(); it2.hasNext(); ) {
		    		    TelephoneNum iter2 = it2.next();
		    		    if (iter2.country == num.country && iter2.number == num.number) {
		    				it2.remove();
		    				try {
			    				FileIO.serialize(new File(dbPath, "events/" + e.eventId + ".evt"), e);
			    				return 1;
			    			} catch (IOException x) {
			    				x.printStackTrace();
			    				return -6;
			    			}
		    			}
		    		}
		    		return -1;
		    	}
		    	return -5;
		    }
		}
		return 0;
	}
}
