package atlantis.cherryvis;

import atlantis.cherryvis.simple.ACherryVisLogger;
import atlantis.config.env.Env;
import atlantis.game.A;

public class ACherryVis {
    public static boolean isEnabled() {
        return !A.isUms() && Env.generateCherryVisReplay();
    }

    // =========================================================

    private static AbstractCherryVisLogger instance = null;

    public static void initialize() {
        logger();
    }

    public static AbstractCherryVisLogger logger() {
        if (instance == null) {
            instance = new ACherryVisLogger(config());
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
        if (!isEnabled()) return;
        if (A.s <= 2) return;

        A.println("CherryVis finishing...");
        logger().onGameEnd();
    }
}
