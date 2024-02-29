package atlantis.util.log;

import atlantis.game.A;

import java.util.Map;
import java.util.TreeMap;

public class ErrorLog {

    private static Map<String, Integer> errors = new TreeMap<>();

    /**
     * Measured in game seconds.
     */
    private static Map<String, Integer> errorTimestamps = new TreeMap<>();

    public static void printErrorOnce(String message) {
        if (!theSameErrorHasBeenLogged(message)) {
            print(message);
        }

        increaseErrorCount(message);
    }

    public static void printMaxOncePerMinute(String message) {
        if (!theSameErrorWasLoggedLessThanMinuteAgo(message)) {
            print(message);
        }

        increaseErrorCount(message);
    }

    public static void printMaxOncePerMinutePlusPrintStackTrace(String message) {
        if (!theSameErrorWasLoggedLessThanMinuteAgo(message)) {
            print(message);
            System.err.println("-------------------");
            A.printStackTrace(message);
            System.err.println("-------------------");
        }

        increaseErrorCount(message);
    }

    private static void print(String message) {
        System.err.println(message);

        errorTimestamps.put(message, A.seconds());
    }

    private static boolean theSameErrorHasBeenLogged(String message) {
        return errors.containsKey(message);
    }

    private static boolean theSameErrorWasLoggedLessThanMinuteAgo(String message) {
        return errorTimestamps.containsKey(message) && (A.seconds() - errorTimestamps.get(message) < 60);
    }

    private static void increaseErrorCount(String message) {
        int currentCount = errors.getOrDefault(message, 0);
        errors.put(message, currentCount + 1);
    }

    public static void printPlusToFile(String message) {
        System.err.println(message);
        
        A.saveToFile("error-log.txt", message, true);
    }
}
