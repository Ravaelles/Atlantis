package atlantis.util.log;


import atlantis.util.AConsole;
import atlantis.util.GameClock;
import atlantis.util.AFile;
import java.util.Map;
import java.util.TreeMap;

public class ErrorLog {

    private static Map<String, Integer> errors = new TreeMap<>();

    /**
     * Measured in game seconds.
     */
    private static Map<String, Integer> errorTimestamps = new TreeMap<>();

    public static void debug(String message) {
        AConsole.errPrintln(LogLevel.LOW, message);
    }

    /**
     * Throttled error reporting. The throttle keys on {@code key}, NEVER on
     * the rendered message: any live value in the message (frame, unit id,
     * minerals, coordinates) makes every row unique and silently disables
     * the throttle (measured 2026-10-26: QUEUE_BASE twin rows every refresh
     * despite printMaxOncePerMinute). Pass a stable slug; put the variance
     * in the message. The unkeyed overloads keep the message as the key
     * and are only correct for static text.
     */
    public static void printErrorOnce(String key, String message) {
        if (!theSameErrorHasBeenLogged(key)) {
            print(message, key);
        }

        increaseErrorCount(key);
    }

    public static void printErrorOnce(String message) {
        printErrorOnce(message, message);
    }

    public static void printMaxOncePerMinute(String key, String message) {
        if (!theSameErrorWasLoggedLessThanMinuteAgo(key)) {
            print(message, key);
        }

        increaseErrorCount(key);
    }

    public static void printMaxOncePerMinute(String message) {
        printMaxOncePerMinute(message, message);
    }

    public static void printMaxOncePerMinutePlusPrintStackTrace(String key, String message) {
        if (!theSameErrorWasLoggedLessThanMinuteAgo(key)) {
            print(message, key);
            System.err.println("-------------------");
            AConsole.printStackTrace(message);
            System.err.println("-------------------");
        }

        increaseErrorCount(key);
    }

    public static void printMaxOncePerMinutePlusPrintStackTrace(String message) {
        printMaxOncePerMinutePlusPrintStackTrace(message, message);
    }

    private static void print(String message, String key) {
        AConsole.errPrintln(LogLevel.HIGH, message);

        errorTimestamps.put(key, GameClock.seconds());
    }

    private static boolean theSameErrorHasBeenLogged(String message) {
        return errors.containsKey(message);
    }

    private static boolean theSameErrorWasLoggedLessThanMinuteAgo(String message) {
        return errorTimestamps.containsKey(message)
            && (GameClock.seconds() - errorTimestamps.get(message) < 60);
    }

    private static void increaseErrorCount(String message) {
        int currentCount = errors.getOrDefault(message, 0);
        errors.put(message, currentCount + 1);
    }

    public static void printPlusToFile(String message) {
        System.err.println(message);

        AFile.saveToFile("error-log.txt", message, true);
    }
}
