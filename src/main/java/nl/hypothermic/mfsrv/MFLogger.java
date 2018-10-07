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
		String cls = cl.getClass().getSimpleName();
		if (cls.length() != 0) {
			ps.println("I - " + cl.getClass().getSimpleName() + " - " + message);
		} else {
			ps.println("I - <anonymous> - " + message);
		}
	}
	
	public static void err(Object cl, String message) {
		err.println("E - " + cl.getClass().getSimpleName() + " - " + message);
	}
}
