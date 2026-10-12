package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVisConfig;
import atlantis.cherryvis.generic.ACherryVis_TypesNames;
import atlantis.cherryvis.ztsd.AZstdWriter;
import atlantis.util.AFile;

public class ACherryVisLogger_TraceJson {
    protected final ACherryVisConfig config;
    protected final ACherryVisUnitLogger logger;

    protected ACherryVisLogger_TraceJson(
        ACherryVisConfig config,
        ACherryVisUnitLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    // =========================================================

    private String content() {
        ACherryVis_Strings strings = new ACherryVis_Strings();
        // Generate updates first to populate strings
        String unitsUpdates = ACherryVis_UnitLogs.build(logger, strings);

        /*
         * If it's per-unit → units_updates
         * If it's global → game_values
         * If it's an event/message → logs
         * If it's a command/decision from a subsystem → tasks or upcs
         */
        return "{"
            + line("_version", 0) + ","
//            + emptyLineMap("board_updates") + ","
            + "\"board_updates\": {" + ACherryVis_GlobalStates.build() + "},"
            + "\"custom_names\": {" + customNames(strings) + "},"
            + emptyLineMap("draw_commands") + ","
            + "\"game_values\": {" + ACherryVis_GlobalValues.build(logger) + "},"
            + emptyLineArray("heatmaps") + ","
            + "\"logs\": [" + ACherryVis_GlobalLogs.build() + "],"
            + emptyLineMap("tasks") + ","
            + emptyLineMap("tensors_summaries") + ","
            + emptyLineArray("trees") + ","
            + "\"types_names\": " + typesToString(strings) + ","
            // + emptyLineMap("units_first_seen") + ","
            + "\"units_first_seen\": {" + ACherryVis_UnitFirstSeen.get() + "},"
            + "\"units_logs\": {" + ACherryVis_UnitLogs.buildUnitLogs(logger) + "},"
            // + "\"units_logs\": {" + ACherryVis_UnitLogs.build(logger) + "},"
            // + emptyLineMap("units_updates")
            + "\"units_updates\": {" + unitsUpdates + "}"
            + "}";
    }

    /**
     * The viewer resolves a unit's {@code manager} integer through this legend,
     * so it must be the runtime table - the ids in {@code units_updates} are
     * assigned at runtime in first-logged order. A hardcoded table only matches
     * if managers happen to log in one exact order, and it has no names at all
     * past its last row.
     */
    public static String customNames(ACherryVis_Strings strings) {
        return " \"manager\": {" + strings.getJson() + "}";
    }


    private String typesToString(ACherryVis_Strings strings) {
        String original = ACherryVis_TypesNames.get();
        if (strings.isEmpty()) {
            return original;
        }
        return original.substring(0, original.length() - 1) + "," + strings.getJson() + "}";
    }

    private String emptyLineMap(String key) {
        return "\"" + key + "\": {}";
    }

    private String emptyLineArray(String key) {
        return "\"" + key + "\": []";
    }

    private String line(String key, Object value) {
        String valueString = (value instanceof String) ? "\"" + value + "\"" : value.toString();

        return "\"" + key + "\": " + valueString;
    }

    // =========================================================

    protected void saveToFile() {
        String cherryVisDirPath = config.cherryVisDirReplayPath();
        String content = content();

        // trace.json is plain JSON (what the viewer opens); the zstd-compressed
        // copy lives next to it as trace.json.zst. trace_raw.json stays as the
        // grep-able artifact the E2E checks read.
        AFile.saveToFile(cherryVisDirPath + "/trace.json", content, true);

        String rawFilePath = cherryVisDirPath + "/trace_raw.json";
        AFile.saveToFile(rawFilePath, content, true);

        AZstdWriter.writeZstdFile(cherryVisDirPath + "/trace.json.zst", content);
    }
}
