package nl.hypothermic.mfsrv.util;

public class UserInputCleaner {
	
	// FIXME: mensen kunnen nog steeds HTML entities (&lt;) gebruiken, Android TextView formatteert deze automatisch.
	
	public static final char[] DISALLOWED_CHARS = {'<', '>', '\\', '[', ']', '*', '+', '=', '?', '^'};
	
	public static String clean(String str) {
		for (char iter : DISALLOWED_CHARS) {
			str.replace(iter, ' ');
		}
		return str;
	}
}
