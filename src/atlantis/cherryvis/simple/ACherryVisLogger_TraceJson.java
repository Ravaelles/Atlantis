package atlantis.cherryvis.simple;

import atlantis.game.A;
import atlantis.cherryvis.ACherryVisConfig;
import atlantis.cherryvis.generic.ACherryVis_TypesNames;
import atlantis.cherryvis.ztsd.AZstdWriter;

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
                + emptyLineMap("board_updates") + ","
                + emptyLineMap("draw_commands") + ","
                + "\"game_values\": {" + ACherryVis_GlobalValues.build(logger) + "},"
                + emptyLineArray("heatmaps") + ","
                + "\"logs\": [" + ACherryVis_GlobalLogs.build() + "],"
                + emptyLineMap("tasks") + ","
                + emptyLineMap("tensors_summaries") + ","
                + emptyLineArray("trees") + ","
                + "\"types_names\": " + mergeTypes(strings) + ","
                // + emptyLineMap("units_first_seen") + ","
                + "\"units_first_seen\": " + ACherryVis_UnitFirstSeen.get() + ","
                + emptyLineMap("units_logs") + ","
                // + "\"units_logs\": {" + ACherryVis_UnitLogs.build(logger) + "},"
                // + emptyLineMap("units_updates")
                + "\"units_updates\": {" + unitsUpdates + "}"
                + "}";
    }

    private String mergeTypes(ACherryVis_Strings strings) {
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
        String filePath = cherryVisDirPath + "/trace.json";
        String content = content();

//        A.saveToFile("D:\\last_trace.json", content, true);

        String rawFilePath = cherryVisDirPath + "/trace_raw.json";
        A.saveToFile(rawFilePath, content, true);

        AZstdWriter.writeZstdFile(filePath, content);
    }
}
