package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.cherryvis.ACherryVisConfig;
import atlantis.cherryvis.AbstractCherryVisLogger;
import atlantis.cherryvis.generic.ACherryVis_GameSummary;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.AConsole;
import atlantis.util.AFile;

public class ACherryVisLogger implements AbstractCherryVisLogger {
    private final ACherryVisConfig config;
    private final ACherryVisUnitLogger unitLogger;

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
//        System.err.println((new File(directoryPath)).getAbsolutePath());

        if (!AFile.directoryExists(directoryPath)) {
            AFile.createDirectory(directoryPath);
        }

        if (!AFile.directoryExists(directoryPath)) {
            AConsole.errPrintln("##################################################");
            AConsole.errPrintln("### Could not create CherryVis dir:");
            AConsole.errPrintln("### " + directoryPath);
            AConsole.errPrintln("### As a result, CherryVis logs will not be saved.");
            AConsole.errPrintln("##################################################");
        }

        (new ACherryVis_GameSummary(config)).saveToFile();
        (new ACherryVisLogger_TraceJson(config, unitLogger)).saveToFile();
    }

    @Override
    public void log(String message) {
        String prefix = A.minSec() + ": ";

        ACherryVis_GlobalLog.create(prefix + message, "Unknown");
    }

    @Override
    public void state(String stateName, String stateValue) {
        ACherryVis_GlobalState.logNewState(stateName, stateValue);
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
        unitLogger.tooltip(A.now(), tooltip, unit);
    }

    public ACherryVisUnitLogger getUnitLogger() {
        return unitLogger;
    }
}
