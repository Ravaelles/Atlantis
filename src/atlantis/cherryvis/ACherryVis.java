package atlantis.cherryvis;

import atlantis.cherryvis.simple.ACherryVisLogger;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.util.AConsole;

public class ACherryVis {
    public static boolean isEnabled() {
//        if (true) return false;

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
        if (isEnabled()) {
            atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.processPending(
                id -> atlantis.core.world.Worlds.units().entity(id), A.now()
            );
            atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();
        }
    }

    public static void finish() {
        if (!isEnabled()) return;
        if (A.s <= 2) return;

        AConsole.println("CherryVis finishing...");
        try {
            logger().onGameEnd();
        } catch (Throwable t) {
            // A debug artifact must never kill the game-end report: the summary,
            // the verdict and the replay flush matter more than the trace.
            // (Measured 2026-10-12: a missing zstd class in the jar crashed
            // onGameEnd with NoClassDefFoundError and took the report with it.)
            AConsole.errPrintln("CherryVis trace failed (game-end report continues): " + t);
        }
    }
}
