package semantic.utils;

/**
 * String utility class
 */
public class StringUtils {

	public static String removeQuotes(String stringWithQuotes) {
		String res = stringWithQuotes.replace("\"", "");
		res = res.replace("'", "");
		return res;
	}
	public static String formatError(String message){ return "[ERROR] " + message; }
	public static String formatProgram(String message){ return "[PROGR] " + message; }
	public static String formatDebug(String message){ return "[DEBUG] " + message; }
}
