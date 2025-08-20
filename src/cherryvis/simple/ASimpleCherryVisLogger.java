package cherryvis.simple;

import atlantis.game.A;
import atlantis.units.AUnit;
import cherryvis.ACherryVisConfig;
import cherryvis.ACherryVisLogger;
import cherryvis.generic.ACherryVis_GameSummary;

public class ASimpleCherryVisLogger implements ACherryVisLogger {
    private ACherryVisConfig config;
    private ASimpleCherryVisUnitLogger unitLogger;

    public ASimpleCherryVisLogger(ACherryVisConfig config) {
        this.config = config;
        this.unitLogger = new ASimpleCherryVisUnitLogger();
    }

    @Override
    public ACherryVisConfig config() {
        return config;
    }

    @Override
    public void onFrameStart(int frame) {
    }

    @Override
    public void onGameEnd() {
        String directoryPath = config.cherryVisDirReplayPath();
        if (!A.directoryExists(directoryPath)) {
            A.createDirectory(directoryPath);
        }

        // Get current working directory
        String cwd = System.getProperty("user.dir");

        if (!A.directoryExists(directoryPath)) {
            A.errPrintln("##################################################");
            A.errPrintln("### Could not create CherryVis dir:");
            A.errPrintln("### " + directoryPath);
            A.errPrintln("### As a result, CherryVis logs will not be saved.");
            A.errPrintln("##################################################");
        }

        (new ACherryVis_GameSummary(config)).saveToFile();
        (new ASimpleCherryVisLogger_TraceJson(config, unitLogger)).saveToFile();
    }

    @Override
    public void unitActiveManager(String message, AUnit unit) {
        unitLogger.managerLog(message, unit);
    }

    @Override
    public void unitTooltip(String tooltip, AUnit unit) {
        unitLogger.tooltip(tooltip, unit);
    }

    public ASimpleCherryVisUnitLogger getUnitLogger() {
        return unitLogger;
    }
}
