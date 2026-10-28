package atlantis.cherryvis.simple;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ACherryVisUnitLogger {
    /**
     * Unit ID -> free-text log entries, rendered as {@code units_logs}.
     */
    public final Map<Integer, List<UnitLogEntry>> unitsFreetextLogs = new TreeMap<>();
    /**
     * Unit ID -> frame -> Active manager
     */
    public Map<Integer, Map<Integer, String>> unitsManagerLogs = new TreeMap<>();
    /**
     * Unit ID -> frame -> behavior tag. The Task slot feed (NEXT #221):
     * `setTooltip` already fires on every behavior transition, but the
     * logger dropped it - so army units (no manager rows by design) showed
     * nothing. Change-only (see {@link #tooltip}): a transition lands once,
     * no per-frame repeats whatever the callers do.
     */
    public final Map<Integer, Map<Integer, String>> unitsTooltips = new TreeMap<>();
    /**
     * Unit ID -> last tooltip logged. Callers re-set the same tag every
     * frame; the trace keeps the transition only.
     */
    private final Map<Integer, String> lastTooltipByUnit = new TreeMap<>();

    protected void managerLog(String message, AUnit unit) {
        if (!unit.isOur()) return;

        unitsManagerLogs.computeIfAbsent(unit.id(), k -> new TreeMap<>()).put(A.now(), message);
    }

    /**
     * Free-text line for one unit (Stardust's {@code CherryVis::log(unitId)}).
     * Frame is explicit so the format is testable without running a game;
     * production callers pass {@code A.now()}.
     */
    public void log(int frame, String message, AUnit unit) {
        if (!unit.isOur()) return;

        unitsFreetextLogs
            .computeIfAbsent(unit.id(), k -> new ArrayList<>())
            .add(new UnitLogEntry(frame, message));
    }

    /**
     * Behavior tag for one unit at one frame, change-only: repeat sets of
     * the same tag land once, so per-frame `setTooltip` callers cost one
     * row per transition, never one per frame. Frame is explicit so the
     * format is testable without running a game.
     */
    public void tooltip(int frame, String tooltip, AUnit unit) {
        if (!unit.isOur()) return;
        if (tooltip == null) tooltip = "";
        if (tooltip.equals(lastTooltipByUnit.get(unit.id()))) return;

        lastTooltipByUnit.put(unit.id(), tooltip);
        unitsTooltips.computeIfAbsent(unit.id(), k -> new TreeMap<>()).put(frame, tooltip);
    }

    /**
     * Test seam: the singleton logger accumulates across tests, never cross-test.
     */
    public void clearForTests() {
        unitsManagerLogs.clear();
        unitsFreetextLogs.clear();
        unitsTooltips.clear();
        lastTooltipByUnit.clear();
    }

    /**
     * One free-text line for one unit, same shape as the global log entries.
     */
    public static class UnitLogEntry {
        final int frame;
        final String message;

        UnitLogEntry(int frame, String message) {
            this.frame = frame;
            this.message = message;
        }

        String toJson() {
            String safe = message != null ? message.replace("\\", "\\\\").replace("\"", "\\\"") : "";
            return "{\"frame\":" + frame + ",\"message\":\"" + safe + "\"}";
        }
    }
}
