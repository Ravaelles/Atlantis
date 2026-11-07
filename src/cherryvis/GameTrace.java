package cherryvis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * In-memory game trace: the single owner of the viewer JSON format.
 *
 * <p>Everything is appended during the game, nothing is written until the
 * caller renders {@link #toTraceJson} once at game end. That is the whole
 * performance contract: per-frame cost is a map append on change-only rows,
 * and file I/O happens exactly once.
 *
 * <p>All inputs are primitives and strings - no game, engine, or bot types -
 * so every section below is unit-testable without running a game.
 */
public class GameTrace {
    /**
     * One free-text line for one unit, same shape as the global log entries.
     */
    public static class UnitLine {
        final int frame;
        final String message;

        UnitLine(int frame, String message) {
            this.frame = frame;
            this.message = message;
        }

        String toJson() {
            return "{\"frame\":" + frame + ",\"message\":\"" + Json.escape(message) + "\"}";
        }
    }

    private static class GlobalLine {
        final int frame;
        final String message;
        final String file;

        GlobalLine(int frame, String message, String file) {
            this.frame = frame;
            this.message = message;
            this.file = file == null ? "" : file;
        }

        String toJson() {
            return "{"
                + "\"frame\":" + frame + ","
                + "\"message\":\"" + Json.escape(message) + "\","
                + "\"sev\":" + 0 + ","
                + "\"file\":\"" + Json.escape(file) + "\","
                + "\"line\":" + 0 + ","
                + "\"attachments\":[]"
                + "}";
        }
    }

    private static class StateRow {
        final int frame;
        final String key;
        final String value;

        StateRow(int frame, String key, String value) {
            this.frame = frame;
            this.key = key;
            this.value = value;
        }

        String toJson() {
            return "\"" + frame + "\": {"
                + "\"" + Json.escape(key) + "\":\"" + Json.escape(value) + "\""
                + "}";
        }
    }

    private final FirstSeen firstSeen = new FirstSeen();
    /** Unit id -> frame -> active manager. */
    private final Map<Integer, Map<Integer, String>> managersByUnit = new TreeMap<>();
    /** Unit id -> free-text lines, insertion (frame) order. */
    private final Map<Integer, List<UnitLine>> linesByUnit = new TreeMap<>();
    /** Unit id -> frame -> behavior tag, change-only. */
    private final Map<Integer, Map<Integer, String>> tooltipsByUnit = new TreeMap<>();
    /** Unit id -> last tooltip logged; repeats of the same tag land once. */
    private final Map<Integer, String> lastTooltipByUnit = new TreeMap<>();
    private final List<GlobalLine> globalLogs = new ArrayList<>();
    private final List<StateRow> boardStates = new ArrayList<>();
    private final TreeMap<Integer, LinkedHashMap<String, String>> gameValues = new TreeMap<>();

    public FirstSeen firstSeen() {
        return firstSeen;
    }

    /**
     * Manager rows for one unit, oldest first. Empty map when the unit was
     * never manager-driven (army units carry behavior tags instead).
     */
    public Map<Integer, String> managerRows(int unitId) {
        Map<Integer, String> rows = managersByUnit.get(unitId);
        return rows == null ? new TreeMap<Integer, String>() : new TreeMap<>(rows);
    }

    public void logManager(int frame, int unitId, String manager) {
        Map<Integer, String> rows = managersByUnit.get(unitId);
        if (rows == null) {
            rows = new TreeMap<>();
            managersByUnit.put(unitId, rows);
        }
        rows.put(frame, manager);
    }

    /**
     * Free-text line for one unit (Stardust's {@code CherryVis::log(unitId)}).
     * Frame is explicit so the format is testable without running a game.
     */
    public void logUnitLine(int frame, int unitId, String message) {
        List<UnitLine> lines = linesByUnit.get(unitId);
        if (lines == null) {
            lines = new ArrayList<>();
            linesByUnit.put(unitId, lines);
        }
        lines.add(new UnitLine(frame, message));
    }

    /**
     * Behavior tag for one unit at one frame, change-only: repeat sets of the
     * same tag land once, so per-frame callers cost one row per transition,
     * never one per frame.
     */
    public void logTooltip(int frame, int unitId, String tooltip) {
        if (tooltip == null) tooltip = "";
        if (tooltip.equals(lastTooltipByUnit.get(unitId))) return;
        lastTooltipByUnit.put(unitId, tooltip);
        Map<Integer, String> rows = tooltipsByUnit.get(unitId);
        if (rows == null) {
            rows = new TreeMap<>();
            tooltipsByUnit.put(unitId, rows);
        }
        rows.put(frame, tooltip);
    }

    public void logGlobal(int frame, String message, String file) {
        globalLogs.add(new GlobalLine(frame, message, file));
    }

    public void logState(int frame, String key, String value) {
        boardStates.add(new StateRow(frame, key, value));
    }

    /**
     * Sampled graph values are numeric literals, rendered unquoted.
     */
    public void sampleValue(int frame, String key, int value) {
        LinkedHashMap<String, String> rows = gameValues.get(frame);
        if (rows == null) {
            rows = new LinkedHashMap<>();
            gameValues.put(frame, rows);
        }
        rows.put(key, String.valueOf(value));
    }

    public void clear() {
        firstSeen.clear();
        managersByUnit.clear();
        linesByUnit.clear();
        tooltipsByUnit.clear();
        lastTooltipByUnit.clear();
        globalLogs.clear();
        boardStates.clear();
        gameValues.clear();
    }

    // =========================================================
    // Renderers. If it is per-unit it goes to units_updates, if it is
    // global it goes to game_values, events/messages go to logs, and a
    // command/decision from a subsystem goes to tasks or upcs.
    // =========================================================

    public String buildUnitLogs() {
        StringBuilder result = new StringBuilder();
        boolean firstUnit = true;
        for (Map.Entry<Integer, List<UnitLine>> unit : linesByUnit.entrySet()) {
            if (!firstUnit) result.append(",");
            firstUnit = false;
            result.append("\"").append(unit.getKey()).append("\":[");
            boolean firstEntry = true;
            for (UnitLine entry : unit.getValue()) {
                if (!firstEntry) result.append(",");
                firstEntry = false;
                result.append(entry.toJson());
            }
            result.append("]");
        }
        return result.toString();
    }

    public String buildBoardUpdates() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < boardStates.size(); i++) {
            result.append(boardStates.get(i).toJson());
            if (i < boardStates.size() - 1) result.append(",");
        }
        return result.toString();
    }

    public String buildGlobalLogs() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < globalLogs.size(); i++) {
            result.append(globalLogs.get(i).toJson());
            if (i < globalLogs.size() - 1) result.append(",");
        }
        return result.toString();
    }

    public String buildGameValues() {
        if (gameValues.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        boolean firstFrame = true;
        for (Map.Entry<Integer, LinkedHashMap<String, String>> frame : gameValues.entrySet()) {
            if (!firstFrame) result.append(",");
            firstFrame = false;
            result.append("\"").append(frame.getKey()).append("\":{");
            boolean firstKey = true;
            for (Map.Entry<String, String> row : frame.getValue().entrySet()) {
                if (!firstKey) result.append(",");
                firstKey = false;
                result.append("\"").append(row.getKey()).append("\":").append(row.getValue());
            }
            result.append("}");
        }
        return result.toString();
    }

    public String buildUnitsUpdates(StringTable strings, boolean withTooltips) {
        StringBuilder result = new StringBuilder();

        // Union of manager-driven units and behavior-tagged units: an army
        // unit with no manager still gets its behavior rows, so the viewer
        // can show what it does. The tooltip union is gated: flap writes
        // 100k+ rows per game.
        Set<Integer> unitIds = new TreeSet<>(managersByUnit.keySet());
        if (withTooltips) unitIds.addAll(tooltipsByUnit.keySet());

        int counterForIds = 0;
        for (int unitId : unitIds) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unitId + "\":{");

            Map<Integer, String> managerLogs = managersByUnit.get(unitId);
            Map<Integer, String> tooltips = withTooltips ? tooltipsByUnit.get(unitId) : null;

            TreeSet<Integer> frames = new TreeSet<>();
            if (managerLogs != null) frames.addAll(managerLogs.keySet());
            if (tooltips != null) frames.addAll(tooltips.keySet());

            String carriedTooltip = "";
            int counterForFrames = 0;
            for (int frame : frames) {
                String manager = managerLogs == null ? "" : managerLogs.getOrDefault(frame, "");
                if (tooltips != null && tooltips.containsKey(frame)) {
                    carriedTooltip = tooltips.get(frame);
                }

                StringBuilder row = new StringBuilder(
                    (counterForFrames == 0 ? "" : ",") + "\"" + frame + "\":{");
                boolean needsComma = false;
                if (manager != null && !manager.isEmpty()) {
                    row.append("\"manager\":").append(strings.get(manager));
                    needsComma = true;
                }
                if (!carriedTooltip.isEmpty()) {
                    if (needsComma) row.append(",");
                    row.append("\"tooltip\":").append(strings.get(carriedTooltip));
                }
                row.append("}");
                unitString.append(row);

                counterForFrames++;
            }

            unitString.append("}");
            result.append(unitString);

            counterForIds++;
        }

        return result.toString();
    }

    public String buildTooltips(StringTable strings) {
        StringBuilder result = new StringBuilder();

        int counterForIds = 0;
        for (Map.Entry<Integer, Map<Integer, String>> unit : tooltipsByUnit.entrySet()) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unit.getKey() + "\":[");

            int counterForFrames = 0;
            for (Map.Entry<Integer, String> row : unit.getValue().entrySet()) {
                String commaForFrames = counterForFrames == 0 ? "" : ",";
                unitString.append(commaForFrames
                    + "[" + row.getKey() + "," + strings.get(row.getValue()) + "]");
                counterForFrames++;
            }

            unitString.append("]");
            result.append(unitString);

            counterForIds++;
        }

        return result.toString();
    }

    /**
     * The viewer resolves a unit's {@code manager} integer through this legend,
     * so it must be the runtime table - the ids in {@code units_updates} are
     * assigned at runtime in first-logged order.
     */
    public static String customNames(StringTable strings) {
        return " \"manager\": {" + strings.getJson() + "}";
    }

    private String typesToString(StringTable strings) {
        String original = TypesNames.get();
        if (strings.isEmpty()) return original;
        return original.substring(0, original.length() - 1) + "," + strings.getJson() + "}";
    }

    private static String emptyLineMap(String key) {
        return "\"" + key + "\": {}";
    }

    private static String emptyLineArray(String key) {
        return "\"" + key + "\": []";
    }

    /**
     * The full trace document. Builds the string table first (updates, then
     * tooltips), because first-logged order defines the legend ids.
     */
    public String toTraceJson(boolean withTooltips) {
        StringTable strings = new StringTable();
        String unitsUpdates = buildUnitsUpdates(strings, withTooltips);
        String unitsTooltips = buildTooltips(strings);

        return "{"
            + "\"_version\": 0,"
            + "\"board_updates\": {" + buildBoardUpdates() + "},"
            + "\"custom_names\": {" + customNames(strings) + "},"
            + emptyLineMap("draw_commands") + ","
            + "\"game_values\": {" + buildGameValues() + "},"
            + emptyLineArray("heatmaps") + ","
            + "\"logs\": [" + buildGlobalLogs() + "],"
            + emptyLineMap("tasks") + ","
            + emptyLineMap("tensors_summaries") + ","
            + emptyLineArray("trees") + ","
            + "\"types_names\": " + typesToString(strings) + ","
            + "\"units_first_seen\": {" + firstSeen.toJson() + "},"
            + "\"unit_ids\": [" + firstSeen.engineIdsJson() + "],"
            + "\"units_logs\": {" + buildUnitLogs() + "},"
            + "\"units_updates\": {" + unitsUpdates + "},"
            + "\"units_tooltips\": {" + unitsTooltips + "}"
            + "}";
    }
}
