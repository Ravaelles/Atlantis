package atlantis.util.log;

import java.util.Map;
import java.util.TreeMap;

public class ErrorLogging {

    private static Map<String, Integer> errors = new TreeMap<>();

    public static void printErrorOnce(String message) {
        if (!theSameErrorHasAlreadyBeenLogged(message)) {
            System.err.println(message);
        }

        increaseErrorCount(message);
    }

    private static boolean theSameErrorHasAlreadyBeenLogged(String message) {
        return errors.containsKey(message);
    }

    private static void increaseErrorCount(String message) {
        int currentCount = errors.getOrDefault(message, 0);
        errors.put(message, currentCount + 1);
    }
}
