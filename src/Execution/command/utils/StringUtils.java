package Execution.command.utils;

public class StringUtils {
    public static String removeQuotes(String stringWithQuotes) {
        return stringWithQuotes.replace("\"", "");
    }
    public static String formatError(String message){ return "[ERROR] " + message; }
    public static String formatProgram(String message){ return "[PROGRAM] " + message; }
    public static String formatDebug(String message){ return "[DEBUG] " + message; }
}
