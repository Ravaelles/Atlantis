package cherryvis.simple;

import atlantis.game.A;
import cherryvis.ACherryVisConfig;
import cherryvis.generic.ACherryVis_TypesNames;
import cherryvis.ztsd.AZstdWriter;

public class ASimpleCherryVisLogger_TraceJson {
    protected final ACherryVisConfig config;
    protected final ASimpleCherryVisUnitLogger logger;

    protected ASimpleCherryVisLogger_TraceJson(
        ACherryVisConfig config,
        ASimpleCherryVisUnitLogger logger
    ) {
        this.config = config;
        this.logger = logger;
    }

    // =========================================================

    private String content() {
        /*
          If it's per-unit → units_updates
          If it's global → game_values
          If it's an event/message → logs
          If it's a command/decision from a subsystem → tasks or upcs
         */
        return "{"
            + line("_version", 0)
            + emptyLineMap("board_updates")
            + emptyLineMap("draw_commands")
            + "\"game_values\": {" + ASimpleCherryVis_GlobalBotValues.build(logger) + "},"
            + emptyLineArray("heatmaps")
            + "\"logs\": [" + ASimpleCherryVis_GlobalBotLogs.build() + "],"
            + emptyLineMap("tasks")
            + emptyLineMap("tensors_summaries")
            + emptyLineArray("trees")
            + "\"types_names\": " + ACherryVis_TypesNames.get() + ","
            + emptyLineMap("units_first_seen")
            + emptyLineMap("units_logs")
            + "\"units_updates\": {" + ASimpleCherryVis_UnitLogs.build(logger) + "}"
            + "}";
    }

    private String emptyLineMap(String key) {
        return "\"" + key + "\": {},";
    }

    private String emptyLineArray(String key) {
        return "\"" + key + "\": [],";
    }

    private String line(String key, Object value) {
        String valueString = (value instanceof String) ? "\"" + value + "\"" : value.toString();

        return "\"" + key + "\": " + valueString + ",";
    }

    // =========================================================

    protected void saveToFile() {
        String cherryVisDirPath = config.cherryVisDirReplayPath();
        String filePath = cherryVisDirPath + "/trace.json";
        String content = content();

        A.saveToFile("D:/last_trace.json", content, true);

        String rawFilePath = cherryVisDirPath + "/trace_raw.json";
        A.saveToFile(rawFilePath, content, true);

        AZstdWriter.writeZstdFile(filePath, content);
    }
}
