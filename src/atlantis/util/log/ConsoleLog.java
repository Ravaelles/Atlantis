package atlantis.util.log;

import atlantis.util.GameClock;


public class ConsoleLog {
    public static void message(String text) {
        System.err.println("@ " + GameClock.frames() + ":  " + text);
    }
}
