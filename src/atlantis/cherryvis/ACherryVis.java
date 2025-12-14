package atlantis.cherryvis;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.cherryvis.simple.ASimpleCherryVisLogger;

public class ACherryVis {
    public static boolean isEnabled() {
        return Env.generateCherryVisReplay();
    }

    // =========================================================

    private static ACherryVisLogger instance = null;

    public static void initialize() {
        logger();
    }

    public static ACherryVisLogger logger() {
        if (instance == null) {
            instance = new ASimpleCherryVisLogger(config());
        }

        return instance;
    }

    private static ACherryVisConfig config() {
        return ACherryVisConfig.createDefault();
    }

    public static void update() {
        logger().onFrameStart(A.now());
    }

    public static void finish() {
        A.println("CherryVis finishing...");
        logger().onGameEnd();
    }
}
