package atlantis.game;

import atlantis.util.log.Log;

public class GameLog {

    private static Log instance = new Log(60, 4);

    public static Log get() {
        return instance;
    }
}
