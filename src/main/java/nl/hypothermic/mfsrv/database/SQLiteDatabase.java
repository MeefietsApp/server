package nl.hypothermic.mfsrv.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import nl.hypothermic.mfsrv.MFServer;
import nl.hypothermic.mfsrv.obj.account.Account;
import nl.hypothermic.mfsrv.obj.auth.TelephoneNum;
import nl.hypothermic.mfsrv.obj.event.Event;
import nl.hypothermic.mfsrv.obj.event.EventType;

public class SQLiteDatabase implements IDatabaseHandler {

	private MFServer server;

	private static final String DB_URI = "jdbc:sqlite:H:/mfsrv.db";
	private Connection db;

	public SQLiteDatabase(MFServer server) {
		this.server = server;
	}

	@Override
	public void eventServletStart() throws SQLException {
		db = DriverManager.getConnection(DB_URI);
		DatabaseMetaData meta = db.getMetaData();
		ResultSet res = meta.getTables(null, null, "auth", new String[] { "TABLE" });
		if (!res.next()) {
		    PreparedStatement pstmt = db.prepareStatement("create table auth (id int, telCounty int, telNum int);");
		    pstmt.executeUpdate();
		}
		
		DatabaseMetaData md = db.getMetaData();
		ResultSet rs = md.getTables(null, null, "auth", null);
		if (!rs.next()) {
			throw new SQLException("MFSRV: Kan de auth tabel niet creeren");
		}
	}

	@Override
	public void eventServletStop() throws SQLException {
		if (db != null) {
			db.close();
		}
	}

	@Override
	public int userLogin(TelephoneNum num, String passwdHash) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int userRegister(TelephoneNum num, String passwdHash) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int userVerify(TelephoneNum num, int verificationToken) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUserRegistered(TelephoneNum num) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserPassword(TelephoneNum num, String passwdHash) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSessionTokenValid(TelephoneNum num, int token) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetSessionTimer(TelephoneNum num) {
		// TODO Auto-generated method stub

	}

	@Override
	public Account getAccount(TelephoneNum num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public Event getEvent(int eventId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public int createEvent(EventType type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public ArrayList<Integer> getUserEvents(Account acc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public int registerEvent(Event event) {
		// TODO Auto-generated method stub
		return 0;
	}
}
