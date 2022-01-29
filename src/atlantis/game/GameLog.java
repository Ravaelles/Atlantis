package atlantis.game;

import atlantis.util.log.Log;

public class GameLog {

    private static Log instance = new Log(60);

    public static Log get() {
        return instance;
    }
}
