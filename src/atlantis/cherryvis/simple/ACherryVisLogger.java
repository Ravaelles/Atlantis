package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.cherryvis.ACherryVisConfig;
import atlantis.cherryvis.AbstractCherryVisLogger;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.units.AUnit;
import cherryvis.GameTrace;

/**
 * Bot-side implementation of the trace logger: stamps the current frame and
 * the unit id, then forwards primitives to the game trace. No format or file
 * knowledge lives here.
 */
public class ACherryVisLogger implements AbstractCherryVisLogger {
    private final ACherryVisUnitLogger unitLogger = new ACherryVisUnitLogger();

    private GameTrace trace() {
        return ACherryVis.trace();
    }

    @Override
    public ACherryVisConfig config() {
        return ACherryVisConfig.createDefault();
    }

    @Override
    public void onFrameStart(int frame) {
    }

    @Override
    public void onGameEnd() {
        ACherryVis.finish();
    }

    @Override
    public void log(String message) {
        trace().logGlobal(A.now(), message, "Unknown");
    }

    @Override
    public void state(String stateName, String stateValue) {
        trace().logState(A.now(), stateName, stateValue);
    }

    @Override
    public void unitManager(String message, AUnit unit) {
        unitLogger.managerLog(message, unit);
    }

    @Override
    public void unitLog(String message, AUnit unit) {
        unitLogger.log(A.now(), message, unit);
    }

    @Override
    public void unitTooltip(String tooltip, AUnit unit) {
        if (Env.cherryVisTooltips()) {
            unitLogger.tooltip(A.now(), tooltip, unit);
        }
    }

    public ACherryVisUnitLogger getUnitLogger() {
        return unitLogger;
    }
}
