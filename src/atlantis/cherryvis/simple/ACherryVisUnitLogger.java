package atlantis.cherryvis.simple;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

public class ACherryVisUnitLogger {
    /** Unit ID -> frame -> Active manager */
    public Map<Integer, Map<Integer, String>> unitsManagerLogs = new TreeMap<>();

    /** Unit ID -> free-text log entries, rendered as {@code units_logs}. */
    public final Map<Integer, List<UnitLogEntry>> unitsFreetextLogs = new TreeMap<>();

    /** One free-text line for one unit, same shape as the global log entries. */
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
}
