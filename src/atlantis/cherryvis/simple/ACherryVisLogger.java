package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.cherryvis.AbstractCherryVisLogger;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.cherryvis.ACherryVisConfig;
import atlantis.cherryvis.generic.ACherryVis_GameSummary;

public class ACherryVisLogger implements AbstractCherryVisLogger {
    private ACherryVisConfig config;
    private ACherryVisUnitLogger unitLogger;

    public ACherryVisLogger(ACherryVisConfig config) {
        this.config = config;
        this.unitLogger = new ACherryVisUnitLogger();
        ACherryVis_GlobalLog.all.clear();
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
        if (!ACherryVis.isEnabled()) return;

        String directoryPath = config.cherryVisDirReplayPath();
        if (!A.directoryExists(directoryPath)) {
            A.createDirectory(directoryPath);
        }

        if (!A.directoryExists(directoryPath)) {
            A.errPrintln("##################################################");
            A.errPrintln("### Could not create CherryVis dir:");
            A.errPrintln("### " + directoryPath);
            A.errPrintln("### As a result, CherryVis logs will not be saved.");
            A.errPrintln("##################################################");
        }

        (new ACherryVis_GameSummary(config)).saveToFile();
        (new ACherryVisLogger_TraceJson(config, unitLogger)).saveToFile();
    }

    @Override
    public void log(String message) {
        String prefix = A.minSec() + " (" + A.now + "): ";

        ACherryVis_GlobalLog.create(prefix + message, "Unknown");
    }

    @Override
    public void unitManager(String message, AUnit unit) {
        unitLogger.managerLog(message, unit);
    }

    @Override
    public void unitTooltip(String tooltip, AUnit unit) {
        // unitLogger.tooltip(tooltip, unit);
    }

    public ACherryVisUnitLogger getUnitLogger() {
        return unitLogger;
    }
}
