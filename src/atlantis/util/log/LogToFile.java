package atlantis.util.log;

import atlantis.game.A;

public class LogToFile {
    public static void info(String text) {
        A.appendToFile("debug-log.txt", text);
    }
}
