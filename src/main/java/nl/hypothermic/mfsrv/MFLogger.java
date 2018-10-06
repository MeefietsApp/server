package nl.hypothermic.mfsrv;

import java.io.PrintStream;

public class MFLogger {
	
	private static final PrintStream ps = System.out;
	private static final PrintStream err = System.err;
	
	static {
		ps.println("#===============================#");
		ps.println("|    MeefietsApp Server " + MFServer.SERVER_VERSION + "    |");
		ps.println("|  www.github.com/MeefietsApp/  |");
		ps.println("#===============================#");
	}
	
	public static void log(Object cl, String message) {
		ps.println("I - " + cl.getClass().getSimpleName() + " - " + message);
	}
	
	public static void err(Object cl, String message) {
		err.println("E - " + cl.getClass().getSimpleName() + " - " + message);
	}
}
