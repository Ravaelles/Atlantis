package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.game.A;
import atlantis.units.AUnit;
import cherryvis.GameTrace;

import java.util.Map;

/**
 * Per-unit logging facade: the single class responsible for unit narratives.
 *
 * <p>Guards (ours-only) and frame stamping happen here; the trace owns the
 * storage and the format. Production passes game objects, tests pass
 * explicit frames through the same methods the production path uses.
 */
public class ACherryVisUnitLogger {
    private GameTrace trace() {
        return ACherryVis.trace();
    }

    protected void managerLog(String message, AUnit unit) {
        if (!unit.isOur()) return;

        trace().logManager(A.now(), unit.id(), message);
    }

    /**
     * Free-text line for one unit (Stardust's {@code CherryVis::log(unitId)}).
     * Frame is explicit so the format is testable without running a game;
     * production callers pass {@code A.now()}.
     */
    public void log(int frame, String message, AUnit unit) {
        if (!unit.isOur()) return;

        trace().logUnitLine(frame, unit.id(), message);
    }

    /**
     * Behavior tag for one unit at one frame; change-only in the trace.
     */
    public void tooltip(int frame, String tooltip, AUnit unit) {
        if (!unit.isOur()) return;

        trace().logTooltip(frame, unit.id(), tooltip);
    }

    /**
     * Manager rows for one unit, oldest first. Used by tests and debugging
     * views that show who owned the unit and when.
     */
    public Map<Integer, String> managerRows(int unitId) {
        return trace().managerRows(unitId);
    }

    /**
     * Test seam: the trace is per-game state, never cross-test.
     */
    public void clearForTests() {
        trace().clear();
    }
}
