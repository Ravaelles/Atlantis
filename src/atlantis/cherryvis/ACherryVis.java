package atlantis.cherryvis;

import atlantis.cherryvis.simple.ACherryVisLogger;
import atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen;
import atlantis.cherryvis.ztsd.AZstdWriter;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.util.AConsole;
import atlantis.util.AFile;
import cherryvis.GameTrace;
import cherryvis.TraceSink;

/**
 * Singleton facade over the dependency-free {@code cherryvis} trace library.
 *
 * <p>This class is the only CherryVis entry point the bot talks to. It owns
 * one {@link GameTrace} per game (in memory only), translates game objects to
 * primitives at the boundary, and writes files exactly once, at game end.
 * The library itself never touches the filesystem, the engine, or bot state.
 */
public class ACherryVis {
    private static GameTrace trace = new GameTrace();
    private static AbstractCherryVisLogger instance = null;

    private static final TraceSink FILE_SINK = new TraceSink() {
        @Override
        public void writeTextFile(String path, String content) {
            AFile.saveToFile(path, content, true);
        }
    };

    // =========================================================

    public static boolean isEnabled() {
        return !A.isUms() && Env.cherryVisEnabled();
    }

    /**
     * The current game's trace. Adapter classes log here with primitives;
     * tests read the rendered sections back through it.
     */
    public static GameTrace trace() {
        return trace;
    }

    public static void initialize() {
        trace = new GameTrace();
        atlantis.cherryvis.simple.ACherryVis_OrderWatcher.clear();
        logger();
    }

    public static AbstractCherryVisLogger logger() {
        if (instance == null) {
            instance = new ACherryVisLogger();
        }

        return instance;
    }

    public static ACherryVisConfig config() {
        return ACherryVisConfig.createDefault();
    }

    public static void update() {
        if (isEnabled()) {
            ACherryVis_UnitFirstSeen.processPending(
                id -> atlantis.core.world.Worlds.units().entity(id), A.now()
            );
            atlantis.cherryvis.simple.ACherryVis_OrderWatcher.update();
        }
    }

    public static void finish() {
        if (!isEnabled()) return;
        if (A.s <= 2) return;

        AConsole.println(atlantis.util.log.LogLevel.LOW, "CherryVis finishing...");
        try {
            String directoryPath = config().cherryVisDirReplayPath();

            if (!AFile.directoryExists(directoryPath)) {
                AConsole.errPrintln("##################################################");
                AConsole.errPrintln("### Could not create CherryVis dir:");
                AConsole.errPrintln("### " + directoryPath);
                AConsole.errPrintln("### As a result, CherryVis logs will not be saved.");
                AConsole.errPrintln("##################################################");
                return;
            }

            String content = trace.toTraceJson(Env.cherryVisTooltips());

            FILE_SINK.writeTextFile(directoryPath + "/trace.json", content);
            FILE_SINK.writeTextFile(directoryPath + "/trace_raw.json", content);
            AZstdWriter.writeZstdFile(directoryPath + "/trace.json.zst", content);

            new ACherryVis_GameSummary().saveToFile(directoryPath);
        } catch (Throwable t) {
            // A debug artifact must never kill the game-end report: the summary,
            // the verdict and the replay flush matter more than the trace.
            // (Measured 2026-10-12: a missing zstd class in the jar crashed
            // onGameEnd with NoClassDefFoundError and took the report with it.)
            AConsole.errPrintln("CherryVis trace failed (game-end report continues): " + t);
        }
    }
}
