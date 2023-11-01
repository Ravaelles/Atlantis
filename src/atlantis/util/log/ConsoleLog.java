package atlantis.util.log;

import atlantis.game.A;

public class ConsoleLog {
    public static void message(String text) {
        System.err.println("@ " + A.now() + ":  " + text);
    }
}
