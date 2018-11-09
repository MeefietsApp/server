package nl.hypothermic.mfsrv.util;

public class UserInputCleaner {

	public static final char[] DISALLOWED_CHARS = {'<', '>', '\\', '[', ']', '*', '+', '=', '?', '^', '&'};
	
	public static String clean(String str) {
		for (char iter : DISALLOWED_CHARS) {
			str.replace(iter, ' ');
		}
		return str;
	}

	public static long formatTime(long epoch) {
		// limiteer delta-tijd tot 23h == 1380m == 82800s == 82800000 ms (domme tijdzones...)
		long systime = System.currentTimeMillis();
		if (epoch < systime - 82800000) {
			epoch = systime - 82800000;
		}
		return epoch;
	}
}
